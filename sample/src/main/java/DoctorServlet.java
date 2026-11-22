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

@WebServlet("/DoctorServlet")
public class DoctorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public DoctorServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String name = request.getParameter("name");
        String specialty = request.getParameter("specialty");
        String contact = request.getParameter("contact");

        DBConnection dbc = new DBConnection();
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = dbc.dbconnection();
            String insertQuery = "INSERT INTO doctors (name, specialty, contact) VALUES (?, ?, ?)";
            pstmt = conn.prepareStatement(insertQuery);
            pstmt.setString(1, name);
            pstmt.setString(2, specialty);
            pstmt.setString(3, contact);

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted > 0) {
                response.sendRedirect("DoctorServlet?action=view");
            } else {
                out.println("<html><head><title>Failed</title><style>"
                    + "body { background-image: url('images/Dr.jpeg'); background-size: cover; background-position: center; background-repeat: no-repeat; background-attachment: fixed; font-family: Arial, sans-serif; margin: 0; padding: 0; color: #fff; display: flex; justify-content: center; align-items: center; height: 100vh; text-align: center; }"
                    + "body::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.5); z-index: -1; }"
                    + ".container { background: rgba(255, 255, 255, 0.2); border-radius: 10px; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3); padding: 30px; max-width: 500px; width: 90%; position: relative; z-index: 1; backdrop-filter: blur(10px); }"
                    + "h1 { color: #fff; }"
                    + ".button { display: inline-block; padding: 10px 20px; color: #fff; background-color: #007bff; border: none; border-radius: 4px; text-decoration: none; }"
                    + ".button:hover { background-color: #0056b3; }"
                    + "</style></head><body>");
                out.println("<div class='container'><h1>Failed to Insert Doctor</h1>");
                out.println("<p>Failed to insert doctor information. Please try again.</p>");
                out.println("<a href='InsertDoctor.html' class='button'>Back to Form</a></div>");
                out.println("</body></html>");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error! Please try again later.");
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // JSON API for dropdowns
        if ("json".equals(action) || "list".equals(action)) {
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            DBConnection dbc = new DBConnection();
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                conn = dbc.dbconnection();
                String query = "SELECT DISTINCT name, specialty, contact FROM doctors ORDER BY name ASC";
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();

                StringBuilder json = new StringBuilder("[");
                boolean first = true;
                while (rs.next()) {
                    if (!first) json.append(",");
                    first = false;
                    json.append("{\"name\": \"").append(escapeJson(rs.getString("name"))).append("\",")
                        .append("\"specialty\": \"").append(escapeJson(rs.getString("specialty"))).append("\",")
                        .append("\"contact\": \"").append(escapeJson(rs.getString("contact"))).append("\"}");
                }
                json.append("]");
                out.print(json.toString());
                out.flush();
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error!");
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
        }

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if ("view".equals(action)) {
            DBConnection dbc = new DBConnection();
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                conn = dbc.dbconnection();
                String query = "SELECT * FROM doctors";
                pstmt = conn.prepareStatement(query);
                rs = pstmt.executeQuery();

                out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=5.0'><title>Doctors List</title><style>"
                    + "*, *::before, *::after { box-sizing: border-box; }"
                    + "body { background: linear-gradient(135deg, rgba(15, 23, 42, 0.75), rgba(13, 148, 136, 0.65)), url('images/Dr.jpeg'); background-size: cover; background-position: center; background-repeat: no-repeat; background-attachment: fixed; font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px 14px; color: #333; display: flex; justify-content: center; align-items: center; min-height: 100vh; }"
                    + ".container { background: rgba(255, 255, 255, 0.98); border-radius: 16px; box-shadow: 0 15px 35px rgba(0, 0, 0, 0.35); padding: clamp(20px, 4vw, 36px); max-width: 800px; width: 100%; position: relative; z-index: 1; }"
                    + "h1 { color: #0f172a; margin-top: 0; margin-bottom: 20px; font-size: clamp(1.3rem, 4vw, 1.7rem); text-align: center; font-weight: 800; }"
                    + ".table-responsive { width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; margin-bottom: 20px; border-radius: 8px; border: 1px solid #e2e8f0; }"
                    + "table { width: 100%; border-collapse: collapse; min-width: 500px; }"
                    + "table th, table td { padding: 12px 14px; border-bottom: 1px solid #e2e8f0; text-align: left; font-size: 14px; }"
                    + "table th { background: #0f172a; color: #fff; font-weight: 600; text-transform: uppercase; font-size: 12px; letter-spacing: 0.5px; }"
                    + "table tr:hover { background-color: #f8fafc; }"
                    + ".actions { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }"
                    + ".button { display: inline-flex; align-items: center; justify-content: center; padding: 11px 20px; color: #fff; background-color: #0284c7; border: none; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 14px; min-height: 42px; }"
                    + ".button:hover { background-color: #0369a1; }"
                    + ".button.secondary { background-color: #0d9488; }"
                    + "</style></head><body>");
                out.println("<div class='container'><h1>👨‍⚕️ Medical Specialist Directory</h1>");
                out.println("<div class='table-responsive'><table><thead><tr><th>Doctor Name</th><th>Specialty</th><th>Contact Number</th></tr></thead><tbody>");

                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td><strong>Dr. " + escapeHtml(rs.getString("name")) + "</strong></td>");
                    out.println("<td>" + escapeHtml(rs.getString("specialty")) + "</td>");
                    out.println("<td>" + escapeHtml(rs.getString("contact")) + "</td>");
                    out.println("</tr>");
                }

                out.println("</tbody></table></div>");
                out.println("<div class='actions'><a href='InsertDoctor.html' class='button secondary'>➕ Add New Doctor</a>");
                out.println("<a href='dashboard.html' class='button'>← Return to Dashboard</a></div></div>");
                out.println("</body></html>");
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error! Please try again later.");
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Show form
            out.println("<html><head><title>Insert Doctor</title><style>"
                + "body { background-image: url('images/Sign.jpg'); background-size: cover; background-position: center; background-repeat: no-repeat; background-attachment: fixed; font-family: Arial, sans-serif; margin: 0; padding: 0; color: #fff; display: flex; justify-content: center; align-items: center; height: 100vh; text-align: center; }"
                + "body::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.5); z-index: -1; }"
                + ".container { background: rgba(255, 255, 255, 0.2); border-radius: 10px; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3); padding: 30px; max-width: 500px; width: 90%; position: relative; z-index: 1; backdrop-filter: blur(10px); }"
                + "h1 { color: #fff; }"
                + ".button { display: inline-block; padding: 10px 20px; color: #fff; background-color: #007bff; border: none; border-radius: 4px; text-decoration: none; }"
                + ".button:hover { background-color: #0056b3; }"
                + "</style></head><body>");
            out.println("<div class='container'><h1>Insert Doctor Information</h1>");
            out.println("<form action='DoctorServlet' method='post'>");
            out.println("<label for='name'>Doctor Name:</label>");
            out.println("<input type='text' id='name' name='name' required><br>");
            out.println("<label for='specialty'>Specialty:</label>");
            out.println("<input type='text' id='specialty' name='specialty' required><br>");
            out.println("<label for='contact'>Contact Number:</label>");
            out.println("<input type='text' id='contact' name='contact' required><br>");
            out.println("<input type='submit' value='Insert Doctor' class='button'>");
            out.println("</form>");
            out.println("<a href='DoctorServlet?action=view' class='button'>View Doctor List</a>");
            out.println("</div></body></html>");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
