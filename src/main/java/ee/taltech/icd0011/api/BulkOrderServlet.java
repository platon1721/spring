package ee.taltech.icd0011.api;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import ee.taltech.icd0011.dao.OrderDao;
import ee.taltech.icd0011.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/orders/bulk")
public class BulkOrderServlet extends HttpServlet {

    private OrderDao orderDao;

    @Override
    public void init() {
        ApplicationContext springContext =
                (ApplicationContext) getServletContext().getAttribute("springContext");
        this.orderDao = springContext.getBean(OrderDao.class);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        String body = readBody(request);
        List<Order> orders = extractOrders(body);

        List<Order> savedOrders = orderDao.saveBatch(orders);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().write(JsonUtil.ordersToJson(savedOrders));
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