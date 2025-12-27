package ee.taltech.icd0011.dao;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDaoImpl implements OrderDao {

    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_PRICE = "price";

    private final DataSource dataSource;

    public OrderDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Order save(Order order) {
        String insertOrderSql = "INSERT INTO orders (order_number) VALUES (?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertOrderSql)) {

            pstmt.setString(1, order.getOrderNumber());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    long generatedId = rs.getLong("id");
                    order.setId(generatedId);

                    // Save order lines if present
                    if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
                        saveOrderLines(conn, generatedId, order.getOrderLines());
                    }

                    return order;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order", e);
        }

        throw new RuntimeException("Failed to generate order ID");
    }

    private void saveOrderLines(Connection conn, Long orderId, List<OrderLine> orderLines)
            throws SQLException {
        String insertLineSql = "INSERT INTO order_lines (order_id, item_name, quantity, price) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertLineSql)) {
            for (OrderLine line : orderLines) {
                pstmt.setLong(1, orderId);
                pstmt.setString(2, line.getItemName());

                // OrderLine constructor sets to null if <= 0, so use 0 as default
                Integer quantity = line.getQuantity();
                Integer price = line.getPrice();

                pstmt.setInt(3, quantity == null ? 0 : quantity);
                pstmt.setInt(4, price == null ? 0 : price);
                pstmt.executeUpdate();
            }
        }
    }

    @Override
    public Order findById(Long id) {
        String sql = "SELECT o.id, o.order_number, "
                + "ol.id as line_id, ol.item_name, ol.quantity, ol.price "
                + "FROM orders o "
                + "LEFT JOIN order_lines ol ON o.id = ol.order_id "
                + "WHERE o.id = ? "
                + "ORDER BY ol.id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                return extractSingleOrder(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order", e);
        }
    }

    @Override
    public List<Order> findAll() {
        String selectAllSql = "SELECT id, order_number FROM orders ORDER BY id";

        List<Order> orders = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectAllSql)) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getLong("id"));
                order.setOrderNumber(rs.getString("order_number"));
                order.setOrderLines(new ArrayList<>());
                orders.add(order);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all orders", e);
        }

        return orders;
    }

    @Override
    public List<Order> findAllWithLines() {
        String sql = "SELECT o.id, o.order_number, "
                + "ol.id as line_id, ol.item_name, ol.quantity, ol.price "
                + "FROM orders o "
                + "LEFT JOIN order_lines ol ON o.id = ol.order_id "
                + "ORDER BY o.id, ol.id";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return extractMultipleOrders(rs);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all orders with lines", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String deleteSql = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {

            pstmt.setLong(1, id);
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete order", e);
        }
    }

    @Override
    public List<Order> saveBatch(List<Order> orders) {
        String insertOrderSql = "INSERT INTO orders (order_number) VALUES (?) RETURNING id";
        List<Order> savedOrders = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement orderStmt = conn.prepareStatement(insertOrderSql)) {

                for (Order order : orders) {
                    orderStmt.setString(1, order.getOrderNumber());

                    try (ResultSet rs = orderStmt.executeQuery()) {
                        if (rs.next()) {
                            long generatedId = rs.getLong("id");
                            order.setId(generatedId);

                            if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
                                saveOrderLinesBatch(conn, generatedId, order.getOrderLines());
                            }

                            savedOrders.add(order);
                        }
                    }
                }

                conn.commit();
                return savedOrders;

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save batch orders", e);
        }
    }

    private void saveOrderLinesBatch(Connection conn, Long orderId, List<OrderLine> orderLines)
            throws SQLException {
        String insertLineSql = "INSERT INTO order_lines (order_id, item_name, quantity, price) "
                + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertLineSql)) {
            for (OrderLine line : orderLines) {
                Integer quantity = line.getQuantity();
                Integer price = line.getPrice();

                pstmt.setLong(1, orderId);
                pstmt.setString(2, line.getItemName());
                pstmt.setInt(3, quantity == null ? 0 : quantity);
                pstmt.setInt(4, price == null ? 0 : price);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private Order extractSingleOrder(ResultSet rs) throws SQLException {
        Order order = null;
        List<OrderLine> orderLines = new ArrayList<>();

        while (rs.next()) {
            if (order == null) {
                order = new Order();
                order.setId(rs.getLong("id"));
                order.setOrderNumber(rs.getString("order_number"));
            }

            Long lineId = rs.getLong("line_id");
            if (!rs.wasNull()) {
                OrderLine line = createOrderLineFromResultSet(rs);
                orderLines.add(line);
            }
        }

        if (order != null) {
            order.setOrderLines(orderLines);
        }

        return order;
    }

    private List<Order> extractMultipleOrders(ResultSet rs) throws SQLException {
        Map<Long, Order> orderMap = new LinkedHashMap<>();

        while (rs.next()) {
            Long orderId = rs.getLong("id");

            Order order = orderMap.get(orderId);
            if (order == null) {
                order = new Order();
                order.setId(orderId);
                order.setOrderNumber(rs.getString("order_number"));
                order.setOrderLines(new ArrayList<>());
                orderMap.put(orderId, order);
            }

            Long lineId = rs.getLong("line_id");
            if (!rs.wasNull()) {
                OrderLine line = createOrderLineFromResultSet(rs);
                order.getOrderLines().add(line);
            }
        }

        return new ArrayList<>(orderMap.values());
    }

    private OrderLine createOrderLineFromResultSet(ResultSet rs) throws SQLException {
        OrderLine line = new OrderLine();
        line.setItemName(rs.getString("item_name"));

        int quantity = rs.getInt(FIELD_QUANTITY);
        int price = rs.getInt(FIELD_PRICE);

        line.setQuantity(quantity);
        line.setPrice(price);

        // If setters didn't set them (because they were <= 0), use reflection
        setFieldIfNull(line, FIELD_QUANTITY, quantity);
        setFieldIfNull(line, FIELD_PRICE, price);

        return line;
    }

    private void setFieldIfNull(OrderLine line, String fieldName, int value) {
        try {
            // Check if field is null
            if (fieldName.equals(FIELD_QUANTITY) && line.getQuantity() == null) {
                java.lang.reflect.Field field = OrderLine.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(line, value);
            } else if (fieldName.equals(FIELD_PRICE) && line.getPrice() == null) {
                java.lang.reflect.Field field = OrderLine.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(line, value);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Log error but continue - field will remain null
            System.err.println("Failed to set field " + fieldName + ": " + e.getMessage());
        }
    }
}