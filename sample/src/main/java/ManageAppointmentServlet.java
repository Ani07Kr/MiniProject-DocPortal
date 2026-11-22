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
    private static final int MAX_PATIENTS_PER_SLOT = 3; // Max 3 patients per 30 minutes

    public ManageAppointmentServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String appointmentId = request.getParameter("appointmentId");
        String doctorName = request.getParameter("doctorName");
        String patientId = request.getParameter("patientId");
        String patientName = request.getParameter("patientName");
        String appointmentDate = request.getParameter("appointmentDate");
        String timeSlot = request.getParameter("timeSlot");

        DBConnection dbc = new DBConnection();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        LocalDate currentDate = LocalDate.now();
        LocalDate selectedDate = (appointmentDate != null && !appointmentDate.isEmpty()) 
                                 ? LocalDate.parse(appointmentDate, DateTimeFormatter.ISO_LOCAL_DATE) 
                                 : null;

        try {
            conn = dbc.dbconnection();
            String title = "Appointment Management";
            StringBuilder body = new StringBuilder();

            if ("book".equals(action)) {
                if (selectedDate != null && selectedDate.isBefore(currentDate)) {
                    body.append("<div class='alert error'>⚠️ <strong>Invalid Appointment Date:</strong> The selected date is in the past. Please select today or a future date.</div>")
                        .append("<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Back</a></p>");
                } else if (timeSlot == null || timeSlot.trim().isEmpty()) {
                    body.append("<div class='alert error'>⚠️ <strong>Missing Time Slot:</strong> Please choose a 30-minute time window for your consultation.</div>")
                        .append("<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Back</a></p>");
                } else {
                    // 1. Verify doctor exists
                    String doctorCheckQuery = "SELECT COUNT(*) FROM doctors WHERE name = ?";
                    pstmt = conn.prepareStatement(doctorCheckQuery);
                    pstmt.setString(1, doctorName);
                    rs = pstmt.executeQuery();
                    rs.next();
                    int doctorCount = rs.getInt(1);
                    rs.close();
                    pstmt.close();

                    if (doctorCount == 0) {
                        body.append("<div class='alert error'>⚠️ <strong>Doctor Not Found:</strong> No doctor registered under the name <strong>")
                            .append(escapeHtml(doctorName)).append("</strong>. Please verify doctor name or add doctor first.</div>")
                            .append("<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Back</a> <a href='InsertDoctor.html' class='btn secondary'>Add Doctor</a></p>");
                    } else {
                        // 2. CRITICAL RULE: Check how many patients already booked this 30-minute window
                        String slotCountQuery = "SELECT COUNT(*) FROM appointments WHERE doctorName = ? AND appointmentDate = ? AND time_slot = ?";
                        pstmt = conn.prepareStatement(slotCountQuery);
                        pstmt.setString(1, doctorName);
                        pstmt.setString(2, appointmentDate);
                        pstmt.setString(3, timeSlot);
                        rs = pstmt.executeQuery();
                        int currentSlotCount = 0;
                        if (rs.next()) {
                            currentSlotCount = rs.getInt(1);
                        }
                        rs.close();
                        pstmt.close();

                        if (currentSlotCount >= MAX_PATIENTS_PER_SLOT) {
                            // Slot limit reached! Reject booking for this slot
                            body.append("<div class='alert error'>")
                                .append("⛔ <strong>30-Minute Slot Full:</strong><br>")
                                .append("The time slot <strong>").append(escapeHtml(timeSlot)).append("</strong> on <strong>")
                                .append(escapeHtml(appointmentDate)).append("</strong> for Dr. <strong>").append(escapeHtml(doctorName))
                                .append("</strong> has reached its maximum capacity of <strong>3 patients</strong>.<br><br>")
                                .append("Please select another 30-minute slot or choose another date.")
                                .append("</div>")
                                .append("<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Choose Another Time Slot</a></p>");
                        } else {
                            // 3. Slot has space! Calculate Slot Token (1, 2, or 3) and Day Token
                            int slotToken = currentSlotCount + 1; // 1, 2, or 3 in this 30-min window

                            String dayTokenQuery = "SELECT COALESCE(MAX(token_number), 0) + 1 FROM appointments WHERE doctorName = ? AND appointmentDate = ?";
                            pstmt = conn.prepareStatement(dayTokenQuery);
                            pstmt.setString(1, doctorName);
                            pstmt.setString(2, appointmentDate);
                            rs = pstmt.executeQuery();
                            int overallDayToken = 1;
                            if (rs.next()) {
                                overallDayToken = rs.getInt(1);
                            }
                            rs.close();
                            pstmt.close();

                            // 4. Insert appointment with time_slot and token_number
                            String insertQuery = "INSERT INTO appointments (appointmentId, doctorName, patientId, patientName, appointmentDate, time_slot, token_number) VALUES (?, ?, ?, ?, ?, ?, ?)";
                            pstmt = conn.prepareStatement(insertQuery);
                            pstmt.setString(1, appointmentId);
                            pstmt.setString(2, doctorName);
                            pstmt.setString(3, patientId);
                            pstmt.setString(4, patientName);
                            pstmt.setString(5, appointmentDate);
                            pstmt.setString(6, timeSlot);
                            pstmt.setInt(7, overallDayToken);
                            int rowsInserted = pstmt.executeUpdate();

                            if (rowsInserted > 0) {
                                body.append("<div class='alert success'>✅ <strong>Appointment Booked Successfully!</strong></div>")
                                    .append("<div class='token-badge'>")
                                    .append("<div class='token-title'>Your 30-Minute Window Token</div>")
                                    .append("<div class='token-number'>Token #").append(slotToken).append(" of 3</div>")
                                    .append("<div class='token-note'>Window: <strong>").append(escapeHtml(timeSlot)).append("</strong> (Day Queue #").append(overallDayToken).append(")</div>")
                                    .append("</div>")
                                    .append("<div class='info-box'>")
                                    .append("<p><strong>Appointment ID:</strong> ").append(escapeHtml(appointmentId)).append("</p>")
                                    .append("<p><strong>Doctor:</strong> Dr. ").append(escapeHtml(doctorName)).append("</p>")
                                    .append("<p><strong>Patient:</strong> ").append(escapeHtml(patientName)).append(" (Ph: ").append(escapeHtml(patientId)).append(")</p>")
                                    .append("<p><strong>Date:</strong> ").append(escapeHtml(appointmentDate)).append("</p>")
                                    .append("<p><strong>Allotted 30-Min Window:</strong> ").append(escapeHtml(timeSlot)).append("</p>")
                                    .append("<p><strong>Queue Rule:</strong> You hold position <strong>#").append(slotToken).append("</strong> out of max 3 patients in this 30-minute block.</p>")
                                    .append("</div>")
                                    .append("<p style='text-align:center;'>")
                                    .append("<a href='downloadSlip?appointmentId=").append(escapeHtml(appointmentId)).append("' class='btn'>📥 Download Appointment Slip</a> ")
                                    .append("<a href='ManageAppointment.html' class='btn secondary'>New Booking</a> ")
                                    .append("<a href='dashboard.html' class='btn secondary'>Dashboard</a>")
                                    .append("</p>");
                            } else {
                                body.append("<div class='alert error'>Failed to book appointment. Please try again.</div>")
                                    .append("<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Back</a></p>");
                            }
                        }
                    }
                }
            } else if ("update".equals(action)) {
                String updateQuery = "UPDATE appointments SET doctorName = ?, patientId = ?, patientName = ?, appointmentDate = ?, time_slot = ? WHERE appointmentId = ?";
                pstmt = conn.prepareStatement(updateQuery);
                pstmt.setString(1, doctorName);
                pstmt.setString(2, patientId);
                pstmt.setString(3, patientName);
                pstmt.setString(4, appointmentDate);
                pstmt.setString(5, timeSlot);
                pstmt.setString(6, appointmentId);
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated > 0) {
                    body.append("<div class='alert success'>✅ Appointment updated successfully for ID: <strong>").append(escapeHtml(appointmentId)).append("</strong></div>");
                } else {
                    body.append("<div class='alert error'>Failed to update appointment. Please check if Appointment ID is correct.</div>");
                }
                body.append("<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Back</a> <a href='dashboard.html' class='btn secondary'>Dashboard</a></p>");
            } else if ("cancel".equals(action)) {
                if (appointmentId == null || appointmentId.trim().isEmpty()) {
                    body.append("<div class='alert error'>Please provide a valid Appointment ID.</div>");
                } else {
                    String deleteQuery = "DELETE FROM appointments WHERE appointmentId = ?";
                    pstmt = conn.prepareStatement(deleteQuery);
                    pstmt.setString(1, appointmentId.trim());
                    int rowsDeleted = pstmt.executeUpdate();

                    if (rowsDeleted > 0) {
                        body.append("<div class='alert success'>🗑️ Appointment <strong>").append(escapeHtml(appointmentId)).append("</strong> has been canceled successfully.</div>");
                    } else {
                        body.append("<div class='alert error'>Failed to cancel appointment. Appointment ID not found.</div>");
                    }
                }
                body.append("<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Back</a> <a href='dashboard.html' class='btn secondary'>Dashboard</a></p>");
            }

            renderHtml(out, title, body.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            renderHtml(out, "Database Error",
                "<div class='alert error'>Database Error: " + escapeHtml(e.getMessage()) + "</div>"
                + "<p style='text-align:center;'><a href='ManageAppointment.html' class='btn'>Back</a></p>"
            );
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

    private void renderHtml(PrintWriter out, String title, String bodyContent) {
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title><style>"
            + "body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 0; background-image: url('images/Dc2.jpg'); background-size: cover; background-position: center; background-attachment: fixed; min-height: 100vh; display: flex; justify-content: center; align-items: center; box-sizing: border-box; padding: 20px; }"
            + ".card { width: 100%; max-width: 650px; background: rgba(255, 255, 255, 0.95); border-radius: 12px; box-shadow: 0 10px 30px rgba(0,0,0,0.22); padding: 30px 35px; box-sizing: border-box; backdrop-filter: blur(6px); }"
            + "h1 { text-align: center; color: #1e3a5f; margin-top: 0; margin-bottom: 20px; font-size: 24px; }"
            + ".alert { padding: 14px 18px; border-radius: 6px; margin-bottom: 20px; font-size: 15px; }"
            + ".alert.success { background-color: #e8f5e9; color: #2e7d32; border: 1px solid #c8e6c9; }"
            + ".alert.error { background-color: #ffebee; color: #c62828; border: 1px solid #ffcdd2; }"
            + ".token-badge { background: linear-gradient(135deg, #1e3a5f, #0d47a1); color: #fff; border-radius: 10px; padding: 18px; text-align: center; margin-bottom: 20px; box-shadow: 0 4px 15px rgba(13, 71, 161, 0.3); }"
            + ".token-title { font-size: 13px; text-transform: uppercase; letter-spacing: 1.5px; opacity: 0.9; }"
            + ".token-number { font-size: 38px; font-weight: 900; margin: 6px 0; color: #ffeb3b; letter-spacing: 1px; }"
            + ".token-note { font-size: 13px; opacity: 0.95; }"
            + ".info-box { background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 8px; padding: 18px; margin-bottom: 20px; font-size: 15px; }"
            + ".info-box p { margin: 8px 0; color: #333; }"
            + ".btn { display: inline-block; padding: 10px 20px; background-color: #2e7d32; color: #fff; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 14px; margin: 4px; transition: background 0.2s; }"
            + ".btn:hover { background-color: #1b5e20; }"
            + ".btn.secondary { background-color: #007bff; }"
            + ".btn.secondary:hover { background-color: #0056b3; }"
            + "</style></head><body><div class='card'>"
            + "<h1>Appointment Confirmation</h1>"
            + bodyContent
            + "</div></body></html>"
        );
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
