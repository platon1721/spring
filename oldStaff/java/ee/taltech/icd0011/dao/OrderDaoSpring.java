package ee.taltech.icd0011.dao;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OrderDaoSpring implements OrderDao {

    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ORDER_LINES = "order_lines";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ORDER_NUMBER = "order_number";
    private static final String COLUMN_ORDER_ID = "order_id";
    private static final String COLUMN_LINE_ID = "line_id";
    private static final String COLUMN_ITEM_NAME = "item_name";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_PRICE = "price";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertOrder;
    private final SimpleJdbcInsert insertOrderLine;

    public OrderDaoSpring(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertOrder = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_ORDERS)
                .usingGeneratedKeyColumns(COLUMN_ID);
        this.insertOrderLine = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_ORDER_LINES)
                .usingGeneratedKeyColumns(COLUMN_ID);
    }

    @Override
    public Order save(Order order) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(COLUMN_ORDER_NUMBER, order.getOrderNumber());

        Number key = insertOrder.executeAndReturnKey(parameters);
        order.setId(key.longValue());

        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            saveOrderLines(order.getId(), order.getOrderLines());
        }

        return order;
    }

    private void saveOrderLines(Long orderId, List<OrderLine> orderLines) {
        for (OrderLine line : orderLines) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(COLUMN_ORDER_ID, orderId);
            parameters.put(COLUMN_ITEM_NAME, line.getItemName());
            parameters.put(COLUMN_QUANTITY, line.getQuantity() == null ? 0 : line.getQuantity());
            parameters.put(COLUMN_PRICE, line.getPrice() == null ? 0 : line.getPrice());

            insertOrderLine.execute(parameters);
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

        return jdbcTemplate.query(sql, new Object[]{id}, rs -> {
            Order order = null;
            List<OrderLine> orderLines = new ArrayList<>();

            while (rs.next()) {
                if (order == null) {
                    order = new Order();
                    order.setId(rs.getLong(COLUMN_ID));
                    order.setOrderNumber(rs.getString(COLUMN_ORDER_NUMBER));
                }

                long lineId = rs.getLong(COLUMN_LINE_ID);
                if (!rs.wasNull()) {
                    OrderLine line = new OrderLine();
                    line.setItemName(rs.getString(COLUMN_ITEM_NAME));
                    line.setQuantity(rs.getInt(COLUMN_QUANTITY));
                    line.setPrice(rs.getInt(COLUMN_PRICE));
                    orderLines.add(line);
                }
            }

            if (order != null) {
                order.setOrderLines(orderLines);
            }

            return order;
        });
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT id, order_number FROM orders ORDER BY id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Order order = new Order();
            order.setId(rs.getLong(COLUMN_ID));
            order.setOrderNumber(rs.getString(COLUMN_ORDER_NUMBER));
            order.setOrderLines(new ArrayList<>());
            return order;
        });
    }

    @Override
    public List<Order> findAllWithLines() {
        String sql = "SELECT o.id, o.order_number, "
                + "ol.id as line_id, ol.item_name, ol.quantity, ol.price "
                + "FROM orders o "
                + "LEFT JOIN order_lines ol ON o.id = ol.order_id "
                + "ORDER BY o.id, ol.id";

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Order> orderMap = new LinkedHashMap<>();

            while (rs.next()) {
                Long orderId = rs.getLong(COLUMN_ID);

                Order order = orderMap.get(orderId);
                if (order == null) {
                    order = new Order();
                    order.setId(orderId);
                    order.setOrderNumber(rs.getString(COLUMN_ORDER_NUMBER));
                    order.setOrderLines(new ArrayList<>());
                    orderMap.put(orderId, order);
                }

                long lineId = rs.getLong(COLUMN_LINE_ID);
                if (!rs.wasNull()) {
                    OrderLine line = new OrderLine();
                    line.setItemName(rs.getString(COLUMN_ITEM_NAME));
                    line.setQuantity(rs.getInt(COLUMN_QUANTITY));
                    line.setPrice(rs.getInt(COLUMN_PRICE));
                    order.getOrderLines().add(line);
                }
            }

            return new ArrayList<>(orderMap.values());
        });
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM orders WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0;
    }

    @Override
    public List<Order> saveBatch(List<Order> orders) {
        List<Order> savedOrders = new ArrayList<>();

        for (Order order : orders) {
            Order saved = save(order);
            savedOrders.add(saved);
        }

        return savedOrders;
    }
}
