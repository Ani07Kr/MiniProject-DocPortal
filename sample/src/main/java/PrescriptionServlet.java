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

@WebServlet("/PrescriptionServlet")
public class PrescriptionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public PrescriptionServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String doctorName = request.getParameter("doctorName");
        String patientId = request.getParameter("patientId");
        String prescription = request.getParameter("prescription");
        String fetchPatientId = request.getParameter("fetchPatientId");

        DBConnection dbc = new DBConnection();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbc.dbconnection();

            if ("write".equals(action)) {
                // Validate doctorName and patientId
                String validateQuery = "SELECT COUNT(*) FROM appointments WHERE doctorName = ? AND patientId = ?";
                pstmt = conn.prepareStatement(validateQuery);
                pstmt.setString(1, doctorName);
                pstmt.setString(2, patientId);
                rs = pstmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    out.println("<html><head><style>"
                        + "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-image: url('images/background.jpg'); background-size: cover; background-position: center; background-repeat: no-repeat; }"
                        + ".container { width: 70%; margin: auto; padding: 20px; background: rgba(255, 255, 255, 0.8); border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); margin-top: 30px; }"
                        + "h1 { text-align: center; color: #333; }"
                        + "p { font-size: 18px; color: red; }"
                        + "a { text-decoration: none; color: #007BFF; font-weight: bold; }"
                        + "a:hover { text-decoration: underline; }"
                        + "</style></head><body>"
                        + "<div class='container'>"
                        + "<h1>Prescription Management</h1>"
                        + "<p>Invalid doctor name or patient ID. Please ensure both are correct.</p>"
                        + "<p><a href='dashboard.html'>Go To The Dashboard</a></p>"
                        + "</div></body></html>");
                } else {
                    // Proceed with inserting prescription
                    pstmt.close(); // Close previous statement

                    String insertQuery = "INSERT INTO prescriptions (doctorName, patientId, prescription) VALUES (?, ?, ?)";
                    pstmt = conn.prepareStatement(insertQuery);
                    pstmt.setString(1, doctorName);
                    pstmt.setString(2, patientId);
                    pstmt.setString(3, prescription);
                    int rowsInserted = pstmt.executeUpdate();

                    if (rowsInserted > 0) {
                        out.println("<html><head><style>"
                            + "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-image: url('images/background.jpg'); background-size: cover; background-position: center; background-repeat: no-repeat; }"
                            + ".container { width: 70%; margin: auto; padding: 20px; background: rgba(255, 255, 255, 0.8); border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); margin-top: 30px; }"
                            + "h1 { text-align: center; color: #333; }"
                            + "p { font-size: 18px; color: #333; }"
                            + "a { text-decoration: none; color: #007BFF; font-weight: bold; }"
                            + "a:hover { text-decoration: underline; }"
                            + "</style></head><body>"
                            + "<div class='container'>"
                            + "<h1>Prescription Management</h1>"
                            + "<p>Prescription written successfully.</p>"
                            + "<p><a href='dashboard.html'>Go To The Dashboard</a></p>"
                            + "</div></body></html>");
                    } else {
                        out.println("<html><head><style>"
                            + "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-image: url('images/background.jpg'); background-size: cover; background-position: center; background-repeat: no-repeat; }"
                            + ".container { width: 70%; margin: auto; padding: 20px; background: rgba(255, 255, 255, 0.8); border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); margin-top: 30px; }"
                            + "h1 { text-align: center; color: #333; }"
                            + "p { font-size: 18px; color: #333; }"
                            + "a { text-decoration: none; color: #007BFF; font-weight: bold; }"
                            + "a:hover { text-decoration: underline; }"
                            + "</style></head><body>"
                            + "<div class='container'>"
                            + "<h1>Prescription Management</h1>"
                            + "<p>Failed to write prescription. Please try again.</p>"
                            + "<p><a href='dashboard.html'>Go To The Dashboard</a></p>"
                            + "</div></body></html>");
                    }
                }
            } else if ("fetch".equals(action)) {
                String selectQuery = "SELECT p.*, a.patientName FROM prescriptions p JOIN appointments a ON p.patientId = a.patientId WHERE p.patientId = ?";
                pstmt = conn.prepareStatement(selectQuery);
                pstmt.setString(1, fetchPatientId);
                rs = pstmt.executeQuery();

                out.println("<html><head><style>"
                    + "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-image: url('images/DC3.jpg'); background-size: cover; background-position: center; background-repeat: no-repeat; }"
                    + ".container { width: 70%; margin: auto; padding: 20px; background: rgba(255, 255, 255, 0.8); border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); margin-top: 30px; }"
                    + "h1 { text-align: center; color: #333; }"
                    + "h2 { color: #4CAF50; }"
                    + "p { font-size: 18px; color: #333; }"
                    + "a { text-decoration: none; color: #007BFF; font-weight: bold; }"
                    + "a:hover { text-decoration: underline; }"
                    + "</style></head><body>"
                    + "<div class='container'>"
                    + "<h1>Prescription Management</h1>"
                    + "<h2>Prescription Details</h2>");

                if (rs.next()) {
                    out.println("<p><strong>Patient Name:</strong> " + rs.getString("patientName") + "</p>");
                    out.println("<p><strong>Doctor Name:</strong> " + rs.getString("doctorName") + "</p>");
                    out.println("<p><strong>Prescription:</strong> " + rs.getString("prescription") + "</p>");
                } else {
                    out.println("<p>No prescription found for this patient ID.</p>");
                }

                out.println("<p><a href='dashboard.html'>Go To The Dashboard</a></p>"
                    + "</div></body></html>");
            }
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
    }
}
