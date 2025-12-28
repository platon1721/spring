package ee.taltech.icd0011.helpers;

import ee.taltech.icd0011.classes.Order;
import jakarta.servlet.ServletContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OrderRepository {
    private static final String ORDERS_KEY = "orders";

    private OrderRepository() {
    }

    @SuppressWarnings("unchecked")
    public static Map<Long, Order> getOrders(ServletContext context) {
        Map<Long, Order> orders = (Map<Long, Order>) context.getAttribute(ORDERS_KEY);

        if (orders == null) {
            orders = new ConcurrentHashMap<>();
            context.setAttribute(ORDERS_KEY, orders);
        }

        return orders;
    }
}