package ee.taltech.icd0011.util;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import ee.taltech.icd0011.validation.ValidationError;
import ee.taltech.icd0011.validation.ValidationErrors;

import java.util.List;

public final class JsonUtil {

    private JsonUtil() {
    }

    public static String orderToJson(Order order) {
        if (order == null) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(order.getId()).append(",");
        sb.append("\"orderNumber\":\"").append(escape(order.getOrderNumber())).append("\"");

        if (order.getOrderLines() != null) {
            sb.append(",\"orderLines\":[");
            boolean first = true;
            for (OrderLine line : order.getOrderLines()) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("{");
                sb.append("\"quantity\":").append(line.getQuantity()).append(",");
                sb.append("\"price\":").append(line.getPrice());
                sb.append("}");
            }
            sb.append("]");
        }

        sb.append("}");
        return sb.toString();
    }

    public static String ordersToJson(List<Order> orders) {
        if (orders == null) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Order order : orders) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(orderToJson(order));
        }
        sb.append("]");
        return sb.toString();
    }

    public static String validationErrorsToJson(ValidationErrors errors) {
        if (errors == null || errors.getErrors().isEmpty()) {
            return "{\"errors\":[]}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\"errors\":[");
        boolean first = true;

        for (ValidationError e : errors.getErrors()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(e.getCode()).append("\"");
        }
        sb.append("]}");
        return sb.toString();
    }


    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
