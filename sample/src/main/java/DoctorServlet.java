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
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String action = request.getParameter("action");

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

                out.println("<html><head><title>Doctors List</title><style>"
                    + "body { background-image: url('images/Dr.jpeg'); background-size: cover; background-position: center; background-repeat: no-repeat; background-attachment: fixed; font-family: Arial, sans-serif; margin: 0; padding: 0; color: #fff; display: flex; justify-content: center; align-items: center; height: 100vh; text-align: center; }"
                    + "body::before { content: ''; position: absolute; top: 0; left: 0; width: 100%; height: 100%; background-color: rgba(0, 0, 0, 0.5); z-index: -1; }"
                    + ".container { background: rgba(255, 255, 255, 0.2); border-radius: 10px; box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3); padding: 30px; max-width: 800px; width: 90%; position: relative; z-index: 1; backdrop-filter: blur(10px); }"
                    + "h1 { color: #fff; }"
                    + "table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }"
                    + "table th, table td { padding: 10px; border: 1px solid rgba(255, 255, 255, 0.6); color: #fff; }"
                    + "table th { background: rgba(255, 255, 255, 0.3); }"
                    + ".button { display: inline-block; padding: 10px 20px; color: #fff; background-color: #007bff; border: none; border-radius: 4px; text-decoration: none; }"
                    + ".button:hover { background-color: #0056b3; }"
                    + "</style></head><body>");
                out.println("<div class='container'><h1>Doctors List</h1>");
                out.println("<table><thead><tr><th>Doctor Name</th><th>Specialty</th><th>Contact Number</th></tr></thead><tbody>");

                while (rs.next()) {
                    out.println("<tr>");
                    out.println("<td>" + rs.getString("name") + "</td>");
                    out.println("<td>" + rs.getString("specialty") + "</td>");
                    out.println("<td>" + rs.getString("contact") + "</td>");
                    out.println("</tr>");
                }

                out.println("</tbody></table>");
                out.println("<a href='InsertDoctor.html' class='button'>Add New Doctor</a>");
                out.println("<a href='dashboard.html' class='button'>Go to Dashboard</a></div>");
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
            // Show the form if no action or invalid action is provided
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
}
