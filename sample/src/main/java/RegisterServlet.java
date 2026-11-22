import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import conn.DBConnection;

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RegisterServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("t1");
        String email = request.getParameter("t2");
        String password = request.getParameter("t3");
        String confirmPassword = request.getParameter("t4");
        String role = request.getParameter("role");
        String phone = request.getParameter("phone");
        String doctorFullName = request.getParameter("doctorFullName");
        String specialty = request.getParameter("specialty");

        if (role == null || role.trim().isEmpty()) {
            role = "Patient";
        } else {
            role = role.trim();
        }

        // Validate basic fields
        if (username == null || email == null || password == null || confirmPassword == null) {
            renderResponse(out, "Missing Information", "All required fields must be filled out.", false);
            return;
        }

        if (!password.equals(confirmPassword)) {
            renderResponse(out, "Password Mismatch", "Passwords do not match. Please verify and try again.", false);
            return;
        }

        DBConnection dbc = new DBConnection();
        Connection conn = null;

        try {
            conn = dbc.dbconnection();

            // 1. Check if username already exists
            String checkUserSql = "SELECT username FROM loginone WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setString(1, username.trim());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        renderResponse(out, "Username Taken", "The username <strong>" + escapeHtml(username) + "</strong> is already taken. Please choose another username.", false);
                        return;
                    }
                }
            }

            // 2. Determine initial status:
            // Doctor requires Admin authorization -> PENDING
            // Patient is auto-approved -> APPROVED
            String status = "Doctor".equalsIgnoreCase(role) ? "PENDING" : "APPROVED";

            // 3. Insert into loginone with status
            String insertUserSql = "INSERT INTO loginone (username, email, password, role, status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql)) {
                pstmt.setString(1, username.trim());
                pstmt.setString(2, email.trim());
                pstmt.setString(3, password);
                pstmt.setString(4, role);
                pstmt.setString(5, status);
                pstmt.executeUpdate();
            }

            // 4. If Doctor, also register into doctors directory
            if ("Doctor".equalsIgnoreCase(role)) {
                String docName = (doctorFullName != null && !doctorFullName.trim().isEmpty()) ? doctorFullName.trim() : username.trim();
                if (!docName.toLowerCase().startsWith("dr.") && !docName.toLowerCase().startsWith("dr ")) {
                    docName = "Dr. " + docName;
                }
                String docSpecialty = (specialty != null && !specialty.trim().isEmpty()) ? specialty.trim() : "General Medicine";
                String docContact = (phone != null && !phone.trim().isEmpty()) ? phone.trim() : "+91-0000000000";

                String checkDocSql = "SELECT COUNT(*) FROM doctors WHERE name = ?";
                boolean exists = false;
                try (PreparedStatement checkDoc = conn.prepareStatement(checkDocSql)) {
                    checkDoc.setString(1, docName);
                    try (ResultSet rsDoc = checkDoc.executeQuery()) {
                        if (rsDoc.next() && rsDoc.getInt(1) > 0) {
                            exists = true;
                        }
                    }
                }

                if (!exists) {
                    String insertDocSql = "INSERT INTO doctors (name, specialty, contact) VALUES (?, ?, ?)";
                    try (PreparedStatement docStmt = conn.prepareStatement(insertDocSql)) {
                        docStmt.setString(1, docName);
                        docStmt.setString(2, docSpecialty);
                        docStmt.setString(3, docContact);
                        docStmt.executeUpdate();
                    }
                }
            }

            // 5. Success Response
            if ("Doctor".equalsIgnoreCase(role)) {
                String msg = "Doctor registration submitted successfully for <strong>Dr. " + escapeHtml(username) + "</strong>!<br><br>"
                           + "<div style='background:#fff3e0; border:1px solid #ffe0b2; color:#e65100; padding:12px; border-radius:6px; margin:15px 0; font-size:14px;'>"
                           + "⏳ <strong>Pending Admin Verification:</strong><br>"
                           + "Your account has been placed under review. The administrator must verify and authorize your doctor credentials before you can log in."
                           + "</div>";
                renderResponse(out, "Registration Under Review", msg, true);
            } else {
                String msg = "Welcome to DocPortal, <strong>" + escapeHtml(username) + "</strong>!<br>"
                           + "Your patient account is active. You can now log in to book appointments and view prescriptions.";
                renderResponse(out, "Registration Successful", msg, true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            renderResponse(out, "Database Error", "Registration failed due to database error: " + escapeHtml(e.getMessage()), false);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    private void renderResponse(PrintWriter out, String title, String message, boolean isSuccess) {
        out.print("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title><style>");
        out.print("body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f2f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; padding: 20px; box-sizing: border-box; }");
        out.print(".card { background: #fff; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.12); padding: 35px; max-width: 480px; width: 100%; text-align: center; box-sizing: border-box; }");
        out.print("h1 { margin-top: 0; color: " + (isSuccess ? "#1e3a5f" : "#c62828") + "; font-size: 24px; }");
        out.print("p { font-size: 15px; color: #444; line-height: 1.6; margin: 15px 0 25px; }");
        out.print(".btn { display: inline-block; padding: 12px 28px; background-color: #1e3a5f; color: #fff; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 15px; transition: background-color 0.2s; }");
        out.print(".btn:hover { background-color: #0d47a1; }");
        out.print("</style></head><body>");
        out.print("<div class='card'>");
        out.print("<h1>" + (isSuccess ? "📋 " : "⚠️ ") + title + "</h1>");
        out.print("<p>" + message + "</p>");
        out.print("<a href='" + (isSuccess ? "Login.html" : "register.html") + "' class='btn'>" + (isSuccess ? "Go to Login Page →" : "Try Again") + "</a>");
        out.print("</div></body></html>");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
