import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import conn.DBConnection;

@WebServlet("/ManageAppointmentServlet")
public class ManageAppointmentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ManageAppointmentServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String appointmentId = request.getParameter("appointmentId");
        String doctorName = request.getParameter("doctorName");
        String patientId = request.getParameter("patientId");
        String patientName = request.getParameter("patientName");
        String appointmentDate = request.getParameter("appointmentDate");

        DBConnection dbc = new DBConnection();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // Get current date
        LocalDate currentDate = LocalDate.now();
        LocalDate selectedDate = (appointmentDate != null && !appointmentDate.isEmpty()) 
                                 ? LocalDate.parse(appointmentDate, DateTimeFormatter.ISO_LOCAL_DATE) 
                                 : null;

        try {
            conn = dbc.dbconnection();
            String message = "";
            String resultStyle = "color: green;";

            if ("book".equals(action)) {
                if (selectedDate != null && selectedDate.isBefore(currentDate)) {
                    message = "<h2>Invalid Appointment Date</h2><p>The selected date is in the past. Please select a future date.</p>";
                    resultStyle = "color: red;";
                } else {
                    String doctorCheckQuery = "SELECT COUNT(*) FROM doctors WHERE name = ?";
                    pstmt = conn.prepareStatement(doctorCheckQuery);
                    pstmt.setString(1, doctorName);
                    rs = pstmt.executeQuery();
                    rs.next();
                    int doctorCount = rs.getInt(1);
                    if (doctorCount == 0) {
                        message = "<h2>Invalid Doctor Name</h2><p>The doctor name you entered does not exist. Please try again.</p>";
                        resultStyle = "color: red;";
                    } else {
                        String insertQuery = "INSERT INTO appointments (appointmentId, doctorName, patientId, patientName, appointmentDate) VALUES (?, ?, ?, ?, ?)";
                        pstmt = conn.prepareStatement(insertQuery);
                        pstmt.setString(1, appointmentId);
                        pstmt.setString(2, doctorName);
                        pstmt.setString(3, patientId);
                        pstmt.setString(4, patientName);
                        pstmt.setString(5, appointmentDate);
                        int rowsInserted = pstmt.executeUpdate();

                        if (rowsInserted > 0) {
                            message = "<h2>Appointment Booked Successfully</h2>"
                                    + "<p><strong>Appointment ID:</strong> " + appointmentId + "</p>"
                                    + "<p><strong>Doctor Name:</strong> " + doctorName + "</p>"
                                    + "<p><strong>Patient ID:</strong> " + patientId + "</p>"
                                    + "<p><strong>Patient Name:</strong> " + patientName + "</p>"
                                    + "<p><strong>Appointment Date:</strong> " + appointmentDate + "</p>";
                        } else {
                            message = "<h2>Failed to Book Appointment</h2><p>Please try again.</p>";
                            resultStyle = "color: red;";
                        }
                    }
                }
            } else if ("update".equals(action)) {
                String updateQuery = "UPDATE appointments SET doctorName = ?, patientId = ?, patientName = ?, appointmentDate = ? WHERE appointmentId = ?";
                pstmt = conn.prepareStatement(updateQuery);
                pstmt.setString(1, doctorName);
                pstmt.setString(2, patientId);
                pstmt.setString(3, patientName);
                pstmt.setString(4, appointmentDate);
                pstmt.setString(5, appointmentId);
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    message = "<h2>Appointment Updated Successfully</h2>";
                } else {
                    message = "<h2>Failed to Update Appointment</h2><p>Please try again.</p>";
                    resultStyle = "color: red;";
                }
            } else if ("cancel".equals(action)) {
                if (appointmentId == null || appointmentId.isEmpty()) {
                    message = "<h2>Invalid Appointment ID</h2><p>Please provide a valid appointment ID.</p>";
                    resultStyle = "color: red;";
                } else {
                    String deleteQuery = "DELETE FROM appointments WHERE appointmentId = ?";
                    pstmt = conn.prepareStatement(deleteQuery);
                    pstmt.setString(1, appointmentId);
                    int rowsDeleted = pstmt.executeUpdate();

                    if (rowsDeleted > 0) {
                        message = "<h2>Appointment Canceled Successfully</h2>";
                    } else {
                        message = "<h2>Failed to Cancel Appointment</h2><p>Please check if the appointment ID is correct and try again.</p>";
                        resultStyle = "color: red;";
                    }
                }
            }

            out.println("<html><head><title>Appointment Management</title>"
                    + "<style>"
                    + "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-image: url('background.jpg'); background-size: cover; background-position: center; }"
                    + ".container { width: 70%; margin: auto; padding: 20px; background: rgba(255, 255, 255, 0.9); border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); margin-top: 30px; }"
                    + "h2 { color: #333; }"
                    + "p { color: " + resultStyle + " }"
                    + "a { text-decoration: none; color: #007BFF; }"
                    + "a:hover { text-decoration: underline; }"
                    + "</style></head><body>"
                    + "<div class='container'>"
                    + message
                    + "<p><a href='dashboard.html'>Go to Dashboard</a></p>"
                    + "</div>"
                    + "</body></html>");
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
