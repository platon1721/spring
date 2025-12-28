package ee.taltech.icd0011.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.classes.OrderLine;
import ee.taltech.icd0011.dao.OrderDao;
import ee.taltech.icd0011.util.JsonUtil;
import ee.taltech.icd0011.validation.ValidationErrors;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {

    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CHARSET_UTF8 = "UTF-8";

    private OrderDao orderDao;
    private ObjectMapper objectMapper;

    @Override
    public void init() {
        ApplicationContext springContext =
                (ApplicationContext) getServletContext().getAttribute("springContext");
        this.orderDao = springContext.getBean(OrderDao.class);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        try {
            String body = readBody(request);
            String orderNumber = JsonUtil.extractOrderNumber(body);

            // Valideerimine
            ValidationErrors validationErrors = new ValidationErrors();
            if (orderNumber == null || orderNumber.length() < 2) {
                validationErrors.addError("too_short_number");
            }

            if (validationErrors.hasErrors()) {
                response.setStatus(400);
                response.setContentType(CONTENT_TYPE_JSON);
                response.setCharacterEncoding(CHARSET_UTF8);
                objectMapper.writeValue(response.getWriter(), validationErrors);
                return;
            }

            List<OrderLine> orderLines = JsonUtil.extractOrderLines(body);

            Order newOrder = new Order();
            newOrder.setOrderNumber(orderNumber);
            newOrder.setOrderLines(orderLines);

            Order savedOrder = orderDao.save(newOrder);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(CHARSET_UTF8);
            response.setContentType(CONTENT_TYPE_JSON);

            response.getWriter().write(JsonUtil.orderToJson(savedOrder));
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType(CONTENT_TYPE_JSON);
            response.setCharacterEncoding(CHARSET_UTF8);
            response.getWriter().write("{\"errors\":[]}");
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
                response.setContentType(CONTENT_TYPE_JSON);
                response.setCharacterEncoding(CHARSET_UTF8);

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
            response.setContentType(CONTENT_TYPE_JSON);
            response.setCharacterEncoding(CHARSET_UTF8);

            response.getWriter().write(JsonUtil.orderToJson(order));
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