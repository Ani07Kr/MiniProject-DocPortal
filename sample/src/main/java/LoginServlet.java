import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import conn.DBConnection;

public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String flag = "false";
        String username = request.getParameter("t1");
        String password = request.getParameter("t2");
        String role = null;
        String email = null;
        String status = "APPROVED";

        DBConnection dbc = new DBConnection();
        Connection conn = dbc.dbconnection();

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            if (conn == null) {
                throw new SQLException("Unable to establish a database connection.");
            }

            String sql = "SELECT * FROM loginone WHERE username=? AND password=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
                email = rs.getString("email");
                status = rs.getString("status");
                if (status == null || status.trim().isEmpty()) {
                    status = "APPROVED";
                }

                // CHECK DOCTOR AUTHORIZATION STATUS
                if ("Doctor".equalsIgnoreCase(role) && "PENDING".equalsIgnoreCase(status)) {
                    flag = "pending_authorization";
                } else {
                    flag = "true";

                    // Establish Session & Cookies
                    HttpSession session = request.getSession(true);
                    session.setAttribute("user", username);
                    session.setAttribute("role", role);
                    session.setAttribute("email", email);

                    Cookie userCookie = new Cookie("docportal_user", username);
                    userCookie.setPath("/");
                    response.addCookie(userCookie);

                    Cookie roleCookie = new Cookie("docportal_role", role);
                    roleCookie.setPath("/");
                    response.addCookie(roleCookie);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<html><body>");
            out.println("<h2>Database Connection Error</h2>");
            out.println("<p>There was an error connecting to the database. Please try again later.</p>");
            out.println("<p>Error details: " + e.getMessage() + "</p>");
            out.println("</body></html>");
            return;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Response UI
        out.print("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Login Status</title><style>");
        out.print("body { ");
        out.print("    font-family: 'Segoe UI', Arial, sans-serif; ");
        out.print("    background-image: url('images/log_in.png'); ");
        out.print("    background-size: cover; ");
        out.print("    background-position: center; ");
        out.print("    background-repeat: no-repeat; ");
        out.print("    background-attachment: fixed; ");
        out.print("    color: #fff; ");
        out.print("    display: flex; ");
        out.print("    justify-content: center; ");
        out.print("    align-items: center; ");
        out.print("    height: 100vh; ");
        out.print("    margin: 0; ");
        out.print("    text-align: center; ");
        out.print("} ");
        out.print(".container { ");
        out.print("    background: rgba(0, 0, 0, 0.78); ");
        out.print("    border-radius: 14px; ");
        out.print("    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5); ");
        out.print("    padding: 35px; ");
        out.print("    max-width: 480px; ");
        out.print("    width: 90%; ");
        out.print("    box-sizing: border-box; ");
        out.print("} ");
        out.print("h1 { margin-top: 0; margin-bottom: 15px; font-size: 24px; }");
        out.print(".pending-box { background: rgba(255, 152, 0, 0.2); border: 2px solid #ffa726; border-radius: 10px; padding: 20px; margin-bottom: 25px; text-align: left; }");
        out.print(".pending-box p { font-size: 15px; color: #ffe0b2; margin: 8px 0; line-height: 1.5; }");
        out.print(".pending-icon { font-size: 40px; margin-bottom: 10px; }");
        out.print(".role-badge { display: inline-block; background: #0288d1; color: #fff; padding: 4px 12px; border-radius: 15px; font-size: 13px; font-weight: bold; margin-bottom: 15px; }");
        out.print("p.msg { font-size: 16px; margin-bottom: 25px; line-height: 1.5; } ");
        out.print(".btn { ");
        out.print("    text-decoration: none; ");
        out.print("    color: #ffffff; ");
        out.print("    background-color: #007bff; ");
        out.print("    padding: 12px 28px; ");
        out.print("    border-radius: 6px; ");
        out.print("    font-weight: bold; ");
        out.print("    display: inline-block; ");
        out.print("    font-size: 15px; ");
        out.print("    transition: background-color 0.25s; ");
        out.print("} ");
        out.print(".btn:hover { background-color: #0056b3; } ");
        out.print(".btn-orange { background-color: #f57c00; }");
        out.print(".btn-orange:hover { background-color: #e65100; }");
        out.print("</style></head><body>");
        
        out.print("<div class='container'>");

        if ("true".equals(flag)) {
            out.print("<h1 style='color:#4fc3f7;'>Login Successful</h1>");
            out.print("<div class='role-badge'>Role: " + escapeHtml(role) + "</div>");
            out.print("<p class='msg'>Welcome back, <strong>" + escapeHtml(username) + "</strong>! Click below to access your dashboard.</p>");
            out.print("<a href='dashboard.html' class='btn'>Proceed to Dashboard →</a>");
        } else if ("pending_authorization".equals(flag)) {
            // DR NOT YET AUTHORIZED BY ADMIN
            out.print("<div class='pending-icon'>⏳</div>");
            out.print("<h1 style='color:#ffa726;'>Admin Yet to Authorize Your Access</h1>");
            out.print("<div class='pending-box'>");
            out.print("<p><strong>Doctor ID:</strong> " + escapeHtml(username) + "</p>");
            out.print("<p>⚠️ Your doctor account is currently <strong>pending administrator verification and authorization</strong>.</p>");
            out.print("<p style='font-size:13px; color:#ffcc80;'>For hospital safety and medical licensing compliance, only verified doctors can access the portal. Please contact the administrator or check back shortly.</p>");
            out.print("</div>");
            out.print("<a href='Login.html' class='btn btn-orange'>Back to Login</a>");
        } else {
            out.print("<h1 style='color:#e57373;'>Login Failed</h1>");
            out.print("<p class='msg'>Invalid username or password. Please try again.</p>");
            out.print("<a href='Login.html' class='btn'>Go Back to Login</a>");
        }

        out.print("</div>");
        out.print("</body></html>");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
