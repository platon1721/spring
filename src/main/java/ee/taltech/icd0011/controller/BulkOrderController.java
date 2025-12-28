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
@RequestMapping("/api/orders/bulk")
public class BulkOrderController {

    private final OrderDao orderDao;

    public BulkOrderController(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @PostMapping
    public ResponseEntity<String> createBulkOrders(
            @RequestBody List<@Valid Order> orders,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ValidationErrors errors = new ValidationErrors();

            for (FieldError error : bindingResult.getFieldErrors()) {
                String field = error.getField();
                if (field.endsWith("quantity")) {
                    errors.addError("invalid_quantity");
                } else if (field.endsWith("price")) {
                    errors.addError("invalid_price");
                }
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonUtil.validationErrorsToJson(errors));
        }

        List<Order> saved = orderDao.saveBatch(orders);
        return ResponseEntity.ok(JsonUtil.ordersToJson(saved));
    }
}
