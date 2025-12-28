package ee.taltech.icd0011.dao;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderDaoSpring implements OrderDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertOrder;
    private final SimpleJdbcInsert insertOrderLine;

    public OrderDaoSpring(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertOrder = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("orders")
                .usingGeneratedKeyColumns("id");
        this.insertOrderLine = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("order_lines")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Order save(Order order) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("order_number", order.getOrderNumber());

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
            parameters.put("order_id", orderId);
            parameters.put("item_name", line.getItemName());
            parameters.put("quantity", line.getQuantity() == null ? 0 : line.getQuantity());
            parameters.put("price", line.getPrice() == null ? 0 : line.getPrice());

            insertOrderLine.execute(parameters);
        }
    }

    @Override
    public Order findById(Long id) {
        String sql = "SELECT o.id, o.order_number, " +
                "ol.id as line_id, ol.item_name, ol.quantity, ol.price " +
                "FROM orders o " +
                "LEFT JOIN order_lines ol ON o.id = ol.order_id " +
                "WHERE o.id = ? " +
                "ORDER BY ol.id";

        return jdbcTemplate.query(sql, new Object[]{id}, rs -> {
            Order order = null;
            List<OrderLine> orderLines = new ArrayList<>();

            while (rs.next()) {
                if (order == null) {
                    order = new Order();
                    order.setId(rs.getLong("id"));
                    order.setOrderNumber(rs.getString("order_number"));
                }

                long lineId = rs.getLong("line_id");
                if (!rs.wasNull()) {
                    OrderLine line = new OrderLine();
                    line.setItemName(rs.getString("item_name"));

                    int quantity = rs.getInt("quantity");
                    int price = rs.getInt("price");

                    line.setQuantity(quantity);
                    line.setPrice(price);

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
            order.setId(rs.getLong("id"));
            order.setOrderNumber(rs.getString("order_number"));
            order.setOrderLines(new ArrayList<>());
            return order;
        });
    }

    @Override
    public List<Order> findAllWithLines() {
        String sql = "SELECT o.id, o.order_number, " +
                "ol.id as line_id, ol.item_name, ol.quantity, ol.price " +
                "FROM orders o " +
                "LEFT JOIN order_lines ol ON o.id = ol.order_id " +
                "ORDER BY o.id, ol.id";

        return jdbcTemplate.query(sql, rs -> {
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

                long lineId = rs.getLong("line_id");
                if (!rs.wasNull()) {
                    OrderLine line = new OrderLine();
                    line.setItemName(rs.getString("item_name"));

                    int quantity = rs.getInt("quantity");
                    int price = rs.getInt("price");

                    line.setQuantity(quantity);
                    line.setPrice(price);

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