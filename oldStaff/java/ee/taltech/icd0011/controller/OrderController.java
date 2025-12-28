package ee.taltech.icd0011.controller;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.dao.OrderDao;
import ee.taltech.icd0011.util.JsonUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderDao orderDao;

    public OrderController(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody String body) {
        String orderNumber = JsonUtil.extractOrderNumber(body);

        if (orderNumber == null || orderNumber.length() < 2) {
            return ResponseEntity.status(400)
                    .body("{\"errors\":[{\"code\":\"too_short_number\"}]}");
        }

        try {
            List<ee.taltech.icd0011.classes.OrderLine> orderLines = JsonUtil.extractOrderLines(body);

            // Validate order lines
            for (ee.taltech.icd0011.classes.OrderLine line : orderLines) {
                if (line.getQuantity() == null || line.getQuantity() < 1) {
                    return ResponseEntity.status(400)
                            .body("{\"errors\":[{\"code\":\"invalid_quantity\"}]}");
                }
                if (line.getPrice() == null || line.getPrice() < 1) {
                    return ResponseEntity.status(400)
                            .body("{\"errors\":[{\"code\":\"invalid_price\"}]}");
                }
            }

            Order newOrder = new Order();
            newOrder.setOrderNumber(orderNumber);
            newOrder.setOrderLines(orderLines);

            Order savedOrder = orderDao.save(newOrder);

            return ResponseEntity.ok(JsonUtil.orderToJson(savedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body("{\"errors\":[]}");
        }
    }

    @GetMapping
    public ResponseEntity<String> getAllOrders() {
        try {
            List<Order> orders = orderDao.findAllWithLines();
            return ResponseEntity.ok(JsonUtil.ordersToJson(orders));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderDao.findById(id);

            if (order == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(JsonUtil.orderToJson(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            boolean deleted = orderDao.deleteById(id);

            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
