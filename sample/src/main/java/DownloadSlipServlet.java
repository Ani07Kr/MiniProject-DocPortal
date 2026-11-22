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
                out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=5.0'><title>Appointment Slip - " + escapeHtml(appointmentId) + "</title><style>"
                    + "*, *::before, *::after { box-sizing: border-box; }"
                    + "body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f2f5; margin: 0; padding: 20px 14px; display: flex; justify-content: center; align-items: flex-start; min-height: 100vh; }"
                    + ".slip-card { width: 100%; max-width: 550px; background: #fff; border-radius: 16px; box-shadow: 0 10px 30px rgba(0,0,0,0.12); border: 1.5px solid #e2e8f0; overflow: hidden; margin: auto; }"
                    + ".header { background: #0f172a; color: #fff; padding: 20px; text-align: center; }"
                    + ".header h1 { margin: 0; font-size: clamp(18px, 4vw, 22px); letter-spacing: 1px; }"
                    + ".header p { margin: 5px 0 0; font-size: 13px; opacity: 0.85; }"
                    + ".token-container { background: #f0fdf4; padding: 18px 14px; text-align: center; border-bottom: 2px dashed #86efac; }"
                    + ".token-label { font-size: 12px; text-transform: uppercase; font-weight: bold; color: #166534; letter-spacing: 1px; }"
                    + ".token-num { font-size: clamp(28px, 6vw, 38px); font-weight: 900; color: #14532d; margin: 4px 0; }"
                    + ".token-time { font-size: 13px; color: #166534; font-weight: 600; }"
                    + ".body-content { padding: clamp(16px, 4vw, 25px); }"
                    + ".detail-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #f1f5f9; font-size: 14px; flex-wrap: wrap; gap: 6px; }"
                    + ".detail-row .label { color: #64748b; font-weight: 600; }"
                    + ".detail-row .val { color: #0f172a; font-weight: bold; text-align: right; word-break: break-word; }"
                    + ".notice { background: #fffbeb; border: 1px solid #fde68a; color: #92400e; padding: 12px; border-radius: 8px; font-size: 12px; margin-top: 18px; text-align: center; line-height: 1.4; }"
                    + ".actions { padding: 18px; text-align: center; background: #f8fafc; border-top: 1px solid #e2e8f0; display: flex; flex-wrap: wrap; gap: 8px; justify-content: center; }"
                    + ".btn { display: inline-flex; align-items: center; justify-content: center; padding: 10px 20px; background: #0f172a; color: #fff; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 14px; cursor: pointer; border: none; min-height: 42px; }"
                    + ".btn.print { background: #0284c7; }"
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
