package ee.taltech.icd0011.controller;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.dao.OrderDao;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders/form")
public class FormController {

    private final OrderDao orderDao;

    public FormController(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> createOrderFromForm(
            @RequestParam String orderNumber,
            @RequestHeader(value = "Accept", required = false) String accept) {

        Order order = new Order();
        order.setOrderNumber(orderNumber);

        Order savedOrder = orderDao.save(order);

        boolean wantsJson = accept != null && accept.contains("application/json");

        if (wantsJson) {
            String json = "{\"id\":" + savedOrder.getId()
                    + ",\"orderNumber\":\"" + escape(orderNumber) + "\"}";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        } else {
            String formData = "id=" + savedOrder.getId() + "&orderNumber=" + orderNumber;
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData);
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
