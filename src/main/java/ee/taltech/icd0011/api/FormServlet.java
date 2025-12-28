package ee.taltech.icd0011.api;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.dao.OrderDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;


import java.io.IOException;

@WebServlet("/orders/form")
public class FormServlet extends HttpServlet {

    private OrderDao orderDao;

    @Override
    public void init() {
        ApplicationContext springContext =
                (ApplicationContext) getServletContext().getAttribute("springContext");
        this.orderDao = springContext.getBean(OrderDao.class);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderNumber = request.getParameter("orderNumber");

        Order order = new Order();
        order.setOrderNumber(orderNumber);

        Order savedOrder = orderDao.save(order);

        String accept = request.getHeader("Accept");
        boolean wantsJson = accept != null && accept.contains("application/json");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");

        if (wantsJson) {
            response.setContentType("application/json");
            response.getWriter().write("{\"id\":" + savedOrder.getId()
                    + ",\"orderNumber\":\"" + escape(orderNumber) + "\"}");
        } else {
            response.setContentType("application/x-www-form-urlencoded");
            response.getWriter().write("id=" + savedOrder.getId() + "&orderNumber=" + orderNumber);
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}