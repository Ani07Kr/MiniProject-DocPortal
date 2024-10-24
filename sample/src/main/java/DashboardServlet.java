import java.io.IOException;

import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public DashboardServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        // Generate the dashboard page
        out.println("<html><head><title>Dashboard</title></head><body>");
        out.println("<h1>Welcome to the Dashboard!</h1>");
        out.println("<p>Here you can find your account details and other information.</p>");
        out.println("<a href='logout'>Logout</a>");
        out.println("</body></html>");
    }
}
