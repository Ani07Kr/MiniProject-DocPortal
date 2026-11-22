import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import conn.DBConnection;

@WebServlet("/AdminApprovalServlet")
public class AdminApprovalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public AdminApprovalServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Check if Admin
        HttpSession session = request.getSession(false);
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        if (role == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("docportal_role".equals(c.getName())) role = c.getValue();
                }
            }
        }

        DBConnection dbc = new DBConnection();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbc.dbconnection();
            String sql = "SELECT username, email, role, status, created_at FROM loginone WHERE role = 'Doctor' ORDER BY status DESC, created_at DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=5.0'><title>Admin - Doctor Authorization</title><style>"
                + "*, *::before, *::after { box-sizing: border-box; }"
                + "body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f2f5; margin: 0; padding: 20px 14px; display: flex; justify-content: center; min-height: 100vh; }"
                + ".container { width: 100%; max-width: 950px; background: #fff; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); padding: clamp(20px, 4vw, 32px); margin: auto; }"
                + "h1 { color: #0f172a; margin-top: 0; font-size: clamp(1.25rem, 4vw, 1.6rem); text-align: center; margin-bottom: 20px; font-weight: 800; }"
                + ".table-responsive { width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; margin-bottom: 25px; border-radius: 8px; border: 1px solid #e2e8f0; }"
                + "table { width: 100%; border-collapse: collapse; font-size: 14px; min-width: 600px; }"
                + "th, td { padding: 12px 14px; text-align: left; border-bottom: 1px solid #e0e0e0; }"
                + "th { background: #0f172a; color: #fff; font-weight: 600; text-transform: uppercase; font-size: 12px; letter-spacing: 0.5px; }"
                + "tr:hover { background-color: #f8fafc; }"
                + ".badge-pending { background: #fff3e0; color: #e65100; padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 12px; border: 1px solid #ffe0b2; display: inline-block; }"
                + ".badge-approved { background: #e8f5e9; color: #2e7d32; padding: 4px 10px; border-radius: 12px; font-weight: bold; font-size: 12px; border: 1px solid #c8e6c9; display: inline-block; }"
                + ".btn-approve { background: #16a34a; color: #fff; border: none; padding: 8px 14px; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 13px; transition: background 0.2s; white-space: nowrap; }"
                + ".btn-approve:hover { background: #15803d; }"
                + ".btn-back { display: inline-flex; align-items: center; justify-content: center; padding: 11px 22px; background: #0284c7; color: #fff; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 14px; min-height: 42px; }"
                + ".btn-back:hover { background: #0369a1; }"
                + ".no-pending { text-align: center; padding: 20px; color: #64748b; font-style: italic; }"
                + "</style></head><body><div class='container'>"
                + "<h1>🛡️ Doctor Verification & Authorization</h1>"
                + "<div class='table-responsive'>"
                + "<table><thead><tr>"
                + "<th>Username</th><th>Email</th><th>Role</th><th>Status</th><th>Registration Date</th><th>Action</th>"
                + "</tr></thead><tbody>"
            );

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                String user = rs.getString("username");
                String email = rs.getString("email");
                String rRole = rs.getString("role");
                String status = rs.getString("status");
                String createdAt = rs.getString("created_at");
                if (status == null) status = "APPROVED";

                boolean isPending = "PENDING".equalsIgnoreCase(status);

                out.println("<tr>");
                out.println("<td><strong>Dr. " + escapeHtml(user) + "</strong></td>");
                out.println("<td>" + escapeHtml(email) + "</td>");
                out.println("<td>" + escapeHtml(rRole) + "</td>");
                out.println("<td><span class='" + (isPending ? "badge-pending" : "badge-approved") + "'>" + status + "</span></td>");
                out.println("<td>" + (createdAt != null ? createdAt : "N/A") + "</td>");
                out.println("<td>");
                if (isPending) {
                    out.println("<form action='AdminApprovalServlet' method='post' style='display:inline;'>");
                    out.println("<input type='hidden' name='username' value='" + escapeHtml(user) + "'>");
                    out.println("<input type='hidden' name='action' value='approve'>");
                    out.println("<input type='submit' value='✓ Authorize Access' class='btn-approve'>");
                    out.println("</form>");
                } else {
                    out.println("<span style='color:#2e7d32; font-weight:bold;'>✓ Authorized</span>");
                }
                out.println("</td></tr>");
            }

            if (!hasRows) {
                out.println("<tr><td colspan='6' class='no-pending'>No registered doctors found.</td></tr>");
            }

            out.println("</tbody></table></div>");
            out.println("<div style='text-align:center;'><a href='dashboard.html' class='btn-back'>← Return to Dashboard</a></div>");
            out.println("</div></body></html>");

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Database error: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ignored) {}
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String action = request.getParameter("action");

        if (username != null && "approve".equalsIgnoreCase(action)) {
            DBConnection dbc = new DBConnection();
            try (Connection conn = dbc.dbconnection()) {
                if (conn != null) {
                    String updateSql = "UPDATE loginone SET status = 'APPROVED' WHERE username = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                        pstmt.setString(1, username);
                        pstmt.executeUpdate();
                    }

                    // Also make sure doctor exists in doctors directory
                    String checkDoc = "SELECT COUNT(*) FROM doctors WHERE LOWER(name) LIKE ?";
                    boolean exists = false;
                    try (PreparedStatement check = conn.prepareStatement(checkDoc)) {
                        check.setString(1, "%" + username.replace("dr_", "").toLowerCase() + "%");
                        try (ResultSet rs = check.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) exists = true;
                        }
                    }

                    if (!exists) {
                        String insertDoc = "INSERT INTO doctors (name, specialty, contact) VALUES (?, ?, ?)";
                        try (PreparedStatement ins = conn.prepareStatement(insertDoc)) {
                            ins.setString(1, "Dr. " + username);
                            ins.setString(2, "General Medicine");
                            ins.setString(3, "+91-9876543210");
                            ins.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect("AdminApprovalServlet");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
