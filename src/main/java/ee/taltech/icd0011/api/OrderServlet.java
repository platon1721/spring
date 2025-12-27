package ee.taltech.icd0011.api;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import ee.taltech.icd0011.dao.OrderDao;
import ee.taltech.icd0011.dao.OrderDaoImpl;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {

    private static final String JSON_QUOTE_COMMA = "\",";
    private OrderDao orderDao;

    @Override
    public void init() {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        this.orderDao = new OrderDaoImpl(dataSource);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        String body = readBody(request);
        String orderNumber = extractOrderNumber(body);
        List<OrderLine> orderLines = extractOrderLines(body);

        Order newOrder = new Order();
        newOrder.setOrderNumber(orderNumber);
        newOrder.setOrderLines(orderLines);

        Order savedOrder = orderDao.save(newOrder);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().write(orderToJson(savedOrder));
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            List<Order> orders = orderDao.findAll();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(ordersToJson(orders));
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            Order order = orderDao.findById(id);

            if (order == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            response.getWriter().write(orderToJson(order));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private static String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
        }
        return sb.toString().trim();
    }

    private static String extractOrderNumber(String json) {
        if (json == null) {
            return "";
        }
        String s = json.replaceAll("\\s+", "");

        int keyPos = s.indexOf("\"orderNumber\":\"");
        if (keyPos < 0) {
            return "";
        }

        int start = keyPos + "\"orderNumber\":\"".length();
        int end = s.indexOf('"', start);

        if (end < 0) {
            return "";
        }

        return s.substring(start, end);
    }

    private static List<OrderLine> extractOrderLines(String json) {
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

    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int keyPos = json.indexOf(search);
        if (keyPos < 0) {
            return "";
        }

        int start = keyPos + search.length();

        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf('"', start);
            if (end < 0) {
                return "";
            }
            return json.substring(start, end);
        } else {
            int end = start;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(start, end);
        }
    }

    private static String ordersToJson(List<Order> orders) {
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

    private static String orderToJson(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(order.getId()).append(JSON_QUOTE_COMMA);
        sb.append("\"orderNumber\":\"").append(order.getOrderNumber()).append(JSON_QUOTE_COMMA);

        List<OrderLine> orderLines = order.getOrderLines();
        if (orderLines == null || orderLines.isEmpty()) {
            sb.append("\"orderRows\":null");
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

    private static String orderRowToJson(OrderLine orderLine) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"itemName\":\"").append(orderLine.getItemName()).append(JSON_QUOTE_COMMA);
        sb.append("\"quantity\":\"").append(orderLine.getQuantity()).append(JSON_QUOTE_COMMA);
        sb.append("\"price\":\"").append(orderLine.getPrice()).append("\"}");

        return sb.toString();
    }
}