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
import java.util.List;

public class OrderDaoImpl implements OrderDao {

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
                pstmt.setInt(3, line.getQuantity());
                pstmt.setInt(4, line.getPrice());
                pstmt.executeUpdate();
            }
        }
    }

    @Override
    public Order findById(Long id) {
        String selectOrderSql = "SELECT id, order_number FROM orders WHERE id = ?";
        String selectLinesSql = "SELECT item_name, quantity, price FROM order_lines WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement orderStmt = conn.prepareStatement(selectOrderSql);
             PreparedStatement linesStmt = conn.prepareStatement(selectLinesSql)) {

            orderStmt.setLong(1, id);

            try (ResultSet rs = orderStmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getLong("id"));
                    order.setOrderNumber(rs.getString("order_number"));

                    linesStmt.setLong(1, id);
                    try (ResultSet linesRs = linesStmt.executeQuery()) {
                        List<OrderLine> orderLines = new ArrayList<>();
                        while (linesRs.next()) {
                            OrderLine line = new OrderLine(
                                    linesRs.getString("item_name"),
                                    linesRs.getInt("quantity"),
                                    linesRs.getInt("price")
                            );
                            orderLines.add(line);
                        }
                        order.setOrderLines(orderLines);
                    }

                    return order;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order", e);
        }

        return null;
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
                order.setOrderLines(null);
                orders.add(order);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all orders", e);
        }

        return orders;
    }
}