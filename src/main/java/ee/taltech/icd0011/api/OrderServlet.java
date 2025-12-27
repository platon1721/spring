package ee.taltech.icd0011.api;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import ee.taltech.icd0011.helpers.IdGenerator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;


@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {

    private static final AtomicLong NEXT_ID = new AtomicLong(1);
    @Override
    protected void doPost(HttpServletRequest request,
    HttpServletResponse response) throws IOException {
        String body = readBody(request);
        String orderNumber = extractOrderNumber(body);
        List<OrderLine> orderLines = extractOrderLines(body);
        long id = IdGenerator.nextId();


        Order newOrder = new Order(id, orderNumber, orderLines);

        ee.taltech.icd0011.api.OrderRepository.getOrders(getServletContext()).put(id, newOrder);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().write(orderToJson(newOrder));
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        Long id = Long.parseLong(idParam);
        Order order = ee.taltech.icd0011.api.OrderRepository.getOrders(getServletContext()).get(id);

        if (order == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(orderToJson(order));
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
        //"orderRows":[
        //       { "itemName" : "CPU","quantity":2,"price":100},
        //       {"itemName": "Motherboard", "quantity":3, "price":60}
        //     ]
        if (json == null) {
            return new ArrayList<>();
        }

        String s = json.replaceAll("\\s+", "");

        //"orderRows":[
        //       {"itemName":"CPU","quantity":2,"price":100},
        //       {"itemName":"Motherboard","quantity":3,"price":60}
        //     ]

        int linePos = s.indexOf("\"orderRows\":[");

        if (linePos < 0) {
            return new ArrayList<>();
        }

        int start = linePos + "\"orderRows\":[".length();
        int end = s.indexOf(']', start);
        if (end < 0) {
            return new ArrayList<>();
        }

        String lineContent =  s.substring(start, end);
        // String
//       {"itemName":"CPU","quantity":2,"price":100},
//       {"itemName":"Motherboard","quantity":3,"price":60}

        String[] lines = lineContent.split("\\},\\{");

        // Array
        // ["{"itemName":"CPU","quantity":2,"price":100}", "{"itemName":"Motherboard","quantity":3,"price":60}"]

        for (String line : lines) {
            String cleanLine = line.replace("}", "").replace("{", "");
            // Array
            // [""itemName":"CPU","quantity":2,"price":100", ""itemName":"Motherboard","quantity":3,"price":60"]
            String itemName = extractValue(cleanLine, "itemName");
            String quantityStr = extractValue(cleanLine, "quantity");
            String priceStr = extractValue(cleanLine, "price");

            if(!itemName.isEmpty() && !quantityStr.isEmpty() && !priceStr.isEmpty()) {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    int price = Integer.parseInt(priceStr);
                    orderLines.add(new OrderLine(itemName, quantity, price));
                } catch (NumberFormatException e) {

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

    private static String orderToJson(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"id\":\"").append(order.getId()).append("\",");
        sb.append("\"orderNumber\":\"").append(order.getOrderNumber()).append("\",");
        sb.append("\"orderRows\":[");

        for (int i = 0; i < order.getOrderLines().size(); i++) {
            sb.append(orderRowToJson(order.getOrderLines().get(i)));
            if (i < order.getOrderLines().size() - 1) {
                sb.append(",");
            }
        }



        return sb.append("]}").toString();
    }

    private static String orderRowToJson(OrderLine orderLine) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"itemName\":\"").append(orderLine.getItemName()).append("\",");
        sb.append("\"quantity\":\"").append(orderLine.getQuantity()).append("\",");
        sb.append("\"price\":\"").append(orderLine.getPrice()).append("\"}");

        return sb.toString();
    }
}
