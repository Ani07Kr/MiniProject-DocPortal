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

@WebServlet("/downloadSlip")
public class DownloadSlipServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public DownloadSlipServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String appointmentId = request.getParameter("appointmentId");
        DBConnection dbc = new DBConnection();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbc.dbconnection();
            String query = "SELECT * FROM appointments WHERE appointmentId = ?";
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, appointmentId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String doctorName = rs.getString("doctorName");
                String patientId = rs.getString("patientId");
                String patientName = rs.getString("patientName");
                String appointmentDate = rs.getString("appointmentDate");

                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"appointment-slip-" + appointmentId + ".pdf\"");

                // Generate PDF (simplified example, use a library like iText or Apache PDFBox for real PDF generation)
                PrintWriter out = response.getWriter();
                out.println("<html><body>");
                out.println("<h1>Appointment Slip</h1>");
                out.println("<p><strong>Appointment ID:</strong> " + appointmentId + "</p>");
                out.println("<p><strong>Doctor Name:</strong> " + doctorName + "</p>");
                out.println("<p><strong>Patient ID:</strong> " + patientId + "</p>");
                out.println("<p><strong>Patient Name:</strong> " + patientName + "</p>");
                out.println("<p><strong>Appointment Date:</strong> " + appointmentDate + "</p>");
                out.println("</body></html>");
                out.close();
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Appointment not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error! Please try again later.");
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
                if (rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
