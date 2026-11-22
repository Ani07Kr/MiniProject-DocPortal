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
        response.setContentType("text/html;charset=UTF-8");
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
                String timeSlot = rs.getString("time_slot");
                if (timeSlot == null || timeSlot.trim().isEmpty()) {
                    timeSlot = "Scheduled OPD Session";
                }
                int tokenNumber = rs.getInt("token_number");
                if (tokenNumber <= 0) tokenNumber = 1;

                PrintWriter out = response.getWriter();
                out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Appointment Slip - " + escapeHtml(appointmentId) + "</title><style>"
                    + "body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f2f5; margin: 0; padding: 30px; display: flex; justify-content: center; }"
                    + ".slip-card { width: 100%; max-width: 550px; background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); border: 1px solid #e0e0e0; overflow: hidden; }"
                    + ".header { background: #1e3a5f; color: #fff; padding: 20px; text-align: center; }"
                    + ".header h1 { margin: 0; font-size: 22px; letter-spacing: 1px; }"
                    + ".header p { margin: 5px 0 0; font-size: 13px; opacity: 0.85; }"
                    + ".token-container { background: #e8f5e9; padding: 18px; text-align: center; border-bottom: 2px dashed #81c784; }"
                    + ".token-label { font-size: 12px; text-transform: uppercase; font-weight: bold; color: #2e7d32; letter-spacing: 1px; }"
                    + ".token-num { font-size: 38px; font-weight: 900; color: #1b5e20; margin: 4px 0; }"
                    + ".token-time { font-size: 14px; color: #388e3c; font-weight: 600; }"
                    + ".body-content { padding: 25px; }"
                    + ".detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f0f0f0; font-size: 15px; }"
                    + ".detail-row .label { color: #666; font-weight: 600; }"
                    + ".detail-row .val { color: #222; font-weight: bold; text-align: right; }"
                    + ".notice { background: #fffde7; border: 1px solid #fff59d; color: #f57f17; padding: 12px; border-radius: 6px; font-size: 13px; margin-top: 20px; text-align: center; }"
                    + ".actions { padding: 20px; text-align: center; background: #fafafa; border-top: 1px solid #eee; }"
                    + ".btn { display: inline-block; padding: 10px 22px; background: #1e3a5f; color: #fff; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 14px; margin: 4px; cursor: pointer; border: none; }"
                    + ".btn.print { background: #2e7d32; }"
                    + "@media print { body { padding: 0; background: #fff; } .slip-card { box-shadow: none; border: 1px solid #ccc; } .actions { display: none; } }"
                    + "</style></head><body>"
                    + "<div class='slip-card'>"
                    + "<div class='header'>"
                    + "<h1>DOCTOR-PATIENT PORTAL</h1>"
                    + "<p>Official Appointment Confirmation Slip</p>"
                    + "</div>"
                    + "<div class='token-container'>"
                    + "<div class='token-label'>Queue Token</div>"
                    + "<div class='token-num'>TOKEN #" + tokenNumber + "</div>"
                    + "<div class='token-time'>Allotted Time Window: " + escapeHtml(timeSlot) + "</div>"
                    + "</div>"
                    + "<div class='body-content'>"
                    + "<div class='detail-row'><span class='label'>Appointment ID:</span><span class='val'>" + escapeHtml(appointmentId) + "</span></div>"
                    + "<div class='detail-row'><span class='label'>Doctor Name:</span><span class='val'>Dr. " + escapeHtml(doctorName) + "</span></div>"
                    + "<div class='detail-row'><span class='label'>Patient Name:</span><span class='val'>" + escapeHtml(patientName) + "</span></div>"
                    + "<div class='detail-row'><span class='label'>Patient Contact:</span><span class='val'>" + escapeHtml(patientId) + "</span></div>"
                    + "<div class='detail-row'><span class='label'>Appointment Date:</span><span class='val'>" + escapeHtml(appointmentDate) + "</span></div>"
                    + "<div class='detail-row'><span class='label'>30-Min Time Slot:</span><span class='val'>" + escapeHtml(timeSlot) + "</span></div>"
                    + "<div class='notice'>ℹ️ <strong>Limit Rule:</strong> A maximum of 3 patients are allotted per 30-minute consultation window. Please arrive 10 minutes prior to your time window.</div>"
                    + "</div>"
                    + "<div class='actions'>"
                    + "<button onclick='window.print()' class='btn print'>🖨️ Print Slip</button> "
                    + "<a href='ManageAppointment.html' class='btn'>Back</a> "
                    + "<a href='dashboard.html' class='btn'>Dashboard</a>"
                    + "</div>"
                    + "</div></body></html>"
                );
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

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#x27;");
    }
}
