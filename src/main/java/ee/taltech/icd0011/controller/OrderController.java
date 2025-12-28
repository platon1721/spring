package ee.taltech.icd0011.controller;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.dao.OrderDao;
import ee.taltech.icd0011.util.JsonUtil;
import ee.taltech.icd0011.validation.ValidationErrors;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderDao orderDao;

    public OrderController(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@Valid @RequestBody Order order,
                                              BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ValidationErrors errors = new ValidationErrors();

            for (FieldError error : bindingResult.getFieldErrors()) {
                String f = error.getField();
                if (f != null && f.endsWith("quantity")) {
                    errors.addError("invalid_quantity");
                } else if (f != null && f.endsWith("price")) {
                    errors.addError("invalid_price");
                }
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonUtil.validationErrorsToJson(errors));
        }

        String orderNumber = order.getOrderNumber();
        if (orderNumber == null || orderNumber.length() < 2) {
            ValidationErrors errors = new ValidationErrors();
            errors.addError("too_short_number");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonUtil.validationErrorsToJson(errors));
        }

        Order saved = orderDao.save(order);
        return ResponseEntity.ok(JsonUtil.orderToJson(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getOrderById(@PathVariable("id") Long id) {
        Order order = orderDao.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(JsonUtil.orderToJson(order));
    }

    @GetMapping
    public ResponseEntity<String> getAllOrders() {
        List<Order> orders = orderDao.findAllWithLines();
        return ResponseEntity.ok(JsonUtil.ordersToJson(orders));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable("id") Long id){
        if (orderDao.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
