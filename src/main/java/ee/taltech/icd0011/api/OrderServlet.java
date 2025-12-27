package ee.taltech.icd0011.api;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;


@WebServlet("/api/orders")
public class OrderServlet extends HttpServlet {

    private static final AtomicLong NEXT_ID = new AtomicLong(1);
    @Override
    protected void doPost(HttpServletRequest request,
    HttpServletResponse response) throws IOException {
        String body = readBody(request);
        String orderNumber = extractOrderNumber(body);

        long id = NEXT_ID.getAndIncrement();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter().write("{\"id\":\"" + id + "\",\"orderNumber\":\"" + escape(orderNumber) + "\"}");
    }

    private static String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = req.getReader()) {
            String line = br.readLine();
            while (line != null) {
                line = br.readLine();
                sb.append(line);
            }

        }
        return sb.toString().trim();
    }

    private static String extractOrderNumber(String json) {
        int keyPos = json.indexOf("\"orderNumber\"");
        if (keyPos < 0) {
            return "";
        }

        int colonPos = json.indexOf(':', keyPos);
        if (colonPos < 0) {
            return "";
        }

        int firstQuote = json.indexOf('"', colonPos + 1);
        if (firstQuote < 0) {
            return "";
        }

        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) {
            return "";
        }

        return json.substring(firstQuote + 1, secondQuote);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }


}
