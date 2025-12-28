package ee.taltech.icd0011.controller;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import ee.taltech.icd0011.dao.OrderDao;
import ee.taltech.icd0011.util.JsonUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/orders/bulk")
public class BulkOrderController {

    private final OrderDao orderDao;

    public BulkOrderController(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @PostMapping
    public ResponseEntity<String> createBulkOrders(@RequestBody String body) {
        List<Order> orders = extractOrders(body);

        // Validate all orders
        for (Order order : orders) {
            for (OrderLine line : order.getOrderLines()) {
                if (line.getQuantity() == null || line.getQuantity() < 1) {
                    return ResponseEntity.status(400)
                            .body("{\"errors\":[{\"code\":\"invalid_quantity\"}]}");
                }
                if (line.getPrice() == null || line.getPrice() < 1) {
                    return ResponseEntity.status(400)
                            .body("{\"errors\":[{\"code\":\"invalid_price\"}]}");
                }
            }
        }

        List<Order> savedOrders = orderDao.saveBatch(orders);

        return ResponseEntity.ok(JsonUtil.ordersToJson(savedOrders));
    }

    private static List<Order> extractOrders(String json) {
        List<Order> orders = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            return orders;
        }

        String s = json.replaceAll("\\s+", "");

        if (!s.startsWith("[") || !s.endsWith("]")) {
            return orders;
        }

        String content = s.substring(1, s.length() - 1);

        List<String> orderJsons = splitOrders(content);

        for (String orderJson : orderJsons) {
            String orderNumber = JsonUtil.extractOrderNumber(orderJson);
            List<OrderLine> orderLines = JsonUtil.extractOrderLines(orderJson);

            Order order = new Order();
            order.setOrderNumber(orderNumber);
            order.setOrderLines(orderLines);
            orders.add(order);
        }

        return orders;
    }

    private static List<String> splitOrders(String content) {
        List<String> result = new ArrayList<>();
        int braceCount = 0;
        int start = 0;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    result.add(content.substring(start, i + 1));
                    start = i + 2;
                }
            }
        }

        return result;
    }
}
