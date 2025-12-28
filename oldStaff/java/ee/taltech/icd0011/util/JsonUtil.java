package ee.taltech.icd0011.util;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;

import java.util.ArrayList;
import java.util.List;

public final class JsonUtil {

    private static final String JSON_QUOTE_COMMA = "\",";

    private JsonUtil() {
    }

    public static String extractOrderNumber(String json) {
        if (json == null) {
            return "";
        }
        String s = json.replaceAll("\\s+", "");

        int keyPos = s.indexOf("\"orderNumber\":\"");
        if (keyPos < 0) {
            return "";
        }

        int start = keyPos + "\"orderNumber\":\"".length();
        int end = findJsonStringEnd(s, start);

        if (end < 0) {
            return "";
        }

        return unescapeJson(s.substring(start, end));
    }

    private static int findJsonStringEnd(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }

    public static String unescapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    public static List<OrderLine> extractOrderLines(String json) {
        List<OrderLine> orderLines = new ArrayList<>();

        if (json == null) {
            return new ArrayList<>();
        }

        String s = json.replaceAll("\\s+", "");

        int linePos = s.indexOf("\"orderRows\":[");

        if (linePos < 0) {
            return new ArrayList<>();
        }

        int start = linePos + "\"orderRows\":[".length();
        int end = s.indexOf(']', start);
        if (end < 0) {
            return new ArrayList<>();
        }

        String lineContent = s.substring(start, end);

        if (lineContent.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] lines = lineContent.split("\\},\\{");

        for (String line : lines) {
            String cleanLine = line.replace("}", "").replace("{", "");
            String itemName = extractValue(cleanLine, "itemName");
            String quantityStr = extractValue(cleanLine, "quantity");
            String priceStr = extractValue(cleanLine, "price");

            if (!itemName.isEmpty() && !quantityStr.isEmpty() && !priceStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    int price = Integer.parseInt(priceStr);
                    orderLines.add(new OrderLine(itemName, quantity, price));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format: " + e.getMessage());
                }
            }
        }

        return orderLines;
    }

    public static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int keyPos = json.indexOf(search);
        if (keyPos < 0) {
            return "";
        }

        int start = keyPos + search.length();

        if (start >= json.length()) {
            return "";
        }

        if (json.charAt(start) == '"') {
            start++;
            int end = findJsonStringEnd(json, start);
            if (end < 0) {
                return "";
            }
            return unescapeJson(json.substring(start, end));
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(start, end);
        }
    }

    public static String ordersToJson(List<Order> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < orders.size(); i++) {
            sb.append(orderToJson(orders.get(i)));
            if (i < orders.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    public static String orderToJson(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":").append(order.getId()).append(",");
        sb.append("\"orderNumber\":\"").append(escapeJson(order.getOrderNumber())).append(JSON_QUOTE_COMMA);

        List<OrderLine> orderLines = order.getOrderLines();
        if (orderLines == null || orderLines.isEmpty()) {
            sb.append("\"orderRows\":[]");
        } else {
            sb.append("\"orderRows\":[");
            for (int i = 0; i < orderLines.size(); i++) {
                sb.append(orderRowToJson(orderLines.get(i)));
                if (i < orderLines.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }

        return sb.append("}").toString();
    }

    public static String orderRowToJson(OrderLine orderLine) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"itemName\":\"").append(escapeJson(orderLine.getItemName())).append(JSON_QUOTE_COMMA);
        sb.append("\"quantity\":").append(orderLine.getQuantity()).append(",");
        sb.append("\"price\":").append(orderLine.getPrice()).append("}");

        return sb.toString();
    }

    public static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
