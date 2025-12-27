package ee.taltech.icd0011.api;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import ee.taltech.icd0011.dao.OrderDao;
import ee.taltech.icd0011.dao.OrderDaoImpl;
import ee.taltech.icd0011.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {

    private OrderDao orderDao;

    @Override
    public void init() {
        DataSource dataSource = (DataSource) getServletContext().getAttribute("dataSource");
        this.orderDao = new OrderDaoImpl(dataSource);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        try {
            String body = readBody(request);
            String orderNumber = JsonUtil.extractOrderNumber(body);
            List<OrderLine> orderLines = JsonUtil.extractOrderLines(body);

            Order newOrder = new Order();
            newOrder.setOrderNumber(orderNumber);
            newOrder.setOrderLines(orderLines);

            Order savedOrder = orderDao.save(newOrder);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");

            response.getWriter().write(JsonUtil.orderToJson(savedOrder));
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            try {
                List<Order> orders = orderDao.findAllWithLines();

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(JsonUtil.ordersToJson(orders));
            } catch (RuntimeException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
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

            response.getWriter().write(JsonUtil.orderToJson(order));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            boolean deleted = orderDao.deleteById(id);

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (RuntimeException e) {
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
}