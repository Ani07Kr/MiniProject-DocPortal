import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import conn.DBConnection;

@WebServlet("/AuthServlet")
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        String user = (session != null) ? (String) session.getAttribute("user") : null;
        String role = (session != null) ? (String) session.getAttribute("role") : null;

        // Fallback to cookies if session is empty
        if (user == null || role == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("docportal_user".equals(c.getName())) user = c.getValue();
                    if ("docportal_role".equals(c.getName())) role = c.getValue();
                }
            }
        }

        if (user == null || role == null) {
            out.print("{\"loggedIn\": false}");
            return;
        }

        String displayName = user;
        // If Doctor, resolve doctor's formal name from doctors table if available
        if ("Doctor".equalsIgnoreCase(role)) {
            displayName = resolveDoctorName(user);
        }

        out.print("{"
            + "\"loggedIn\": true,"
            + "\"user\": \"" + escapeJson(user) + "\","
            + "\"role\": \"" + escapeJson(role) + "\","
            + "\"displayName\": \"" + escapeJson(displayName) + "\""
            + "}");
    }

    private String resolveDoctorName(String username) {
        DBConnection dbc = new DBConnection();
        try (Connection conn = dbc.dbconnection()) {
            if (conn != null) {
                // Try matching name by username or prefix
                String sql = "SELECT name FROM doctors WHERE LOWER(name) LIKE ? LIMIT 1";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    String searchKey = "%" + username.replace("dr_", "").replace("_", " ") + "%";
                    pstmt.setString(1, searchKey.toLowerCase());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString("name");
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        if (!username.startsWith("Dr.")) {
            return "Dr. " + username;
        }
        return username;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
