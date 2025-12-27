package ee.taltech.icd0011.api;

import ee.taltech.icd0011.classes.Order;
import ee.taltech.icd0011.helpers.IdGenerator;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/orders/form")
public class FormServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String orderNumber = request.getParameter("orderNumber");

        long id = IdGenerator.nextId();
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber(orderNumber);

        ee.taltech.icd0011.api.OrderRepository.getOrders(getServletContext()).put(id, order);

        String accept = request.getHeader("Accept");
        boolean wantsJson = accept != null && accept.contains("application/json");

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");

        if (wantsJson) {
            response.setContentType("application/json");
            response.getWriter().write("{\"id\":" + id + ",\"orderNumber\":\"" + escape(orderNumber) + "\"}");
        } else {
            response.setContentType("application/x-www-form-urlencoded");
            response.getWriter().write("id=" + id + "&orderNumber=" + orderNumber);
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}