import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import conn.DBConnection;

@WebServlet("/PrescriptionServlet")
public class PrescriptionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public PrescriptionServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String action = request.getParameter("action");
        String requestedDoctorName = request.getParameter("doctorName");
        String patientId = request.getParameter("patientId");
        String prescription = request.getParameter("prescription");
        String validUntilStr = request.getParameter("validUntil");
        String fetchPatientId = request.getParameter("fetchPatientId");

        // Role & Identity Resolution
        HttpSession session = request.getSession(false);
        String sessionUser = (session != null) ? (String) session.getAttribute("user") : null;
        String sessionRole = (session != null) ? (String) session.getAttribute("role") : null;

        if (sessionUser == null || sessionRole == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("docportal_user".equals(c.getName())) sessionUser = c.getValue();
                    if ("docportal_role".equals(c.getName())) sessionRole = c.getValue();
                }
            }
        }

        DBConnection dbc = new DBConnection();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dbc.dbconnection();

            if ("write".equals(action)) {
                // =========================================================================
                // SECURITY ENFORCEMENT: Only Doctors / Admins can write prescriptions!
                // =========================================================================
                if (sessionRole != null && "Patient".equalsIgnoreCase(sessionRole)) {
                    renderPage(out, "Access Forbidden",
                        "<div class='alert error'>⛔ <strong>Access Denied:</strong> Patients do not have access to write prescriptions.<br>"
                        + "Only verified doctors are authorized to prescribe medications.</div>"
                        + "<p style='text-align:center;'><a href='Prescription.html' class='btn'>Back</a> <a href='dashboard.html' class='btn secondary'>Dashboard</a></p>"
                    );
                    return;
                }

                // =========================================================================
                // IDENTITY LOCK: Resolve doctor's true name from their login session!
                // A doctor cannot write under another doctor's identity.
                // =========================================================================
                String doctorName = resolveDoctorName(conn, sessionUser, requestedDoctorName);

                // 1. Validate appointment exists for this doctor and patient
                String validateQuery = "SELECT COUNT(*) FROM appointments WHERE doctorName = ? AND patientId = ?";
                pstmt = conn.prepareStatement(validateQuery);
                pstmt.setString(1, doctorName);
                pstmt.setString(2, patientId);
                rs = pstmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    renderPage(out, "Prescription Error",
                        "<div class='alert error'>⚠️ <strong>Validation Failed:</strong> No scheduled appointment found for Dr. <strong>"
                        + escapeHtml(doctorName) + "</strong> with Patient ID <strong>" + escapeHtml(patientId) + "</strong>.<br>"
                        + "Doctors can only prescribe to patients who have a confirmed appointment with them.</div>"
                        + "<p style='text-align:center;'><a href='Prescription.html' class='btn'>Back</a> <a href='dashboard.html' class='btn secondary'>Dashboard</a></p>"
                    );
                } else {
                    pstmt.close();

                    // 2. Parse time limit
                    Timestamp validUntilTimestamp = null;
                    if (validUntilStr != null && !validUntilStr.trim().isEmpty()) {
                        try {
                            String normalized = validUntilStr.trim().replace("T", " ");
                            if (normalized.length() == 16) normalized += ":00";
                            validUntilTimestamp = Timestamp.valueOf(normalized);
                        } catch (Exception e) {
                            validUntilTimestamp = new Timestamp(System.currentTimeMillis() + (7L * 24 * 3600 * 1000));
                        }
                    }

                    // 3. Insert prescription with verified doctorName
                    String insertQuery = "INSERT INTO prescriptions (doctorName, patientId, prescription, valid_until) VALUES (?, ?, ?, ?)";
                    pstmt = conn.prepareStatement(insertQuery);
                    pstmt.setString(1, doctorName);
                    pstmt.setString(2, patientId);
                    pstmt.setString(3, prescription);
                    pstmt.setTimestamp(4, validUntilTimestamp);
                    int rowsInserted = pstmt.executeUpdate();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
                    String expiryDisplay = (validUntilTimestamp != null) ? sdf.format(validUntilTimestamp) : "No expiration";

                    if (rowsInserted > 0) {
                        renderPage(out, "Prescription Created",
                            "<div class='alert success'>✅ <strong>Prescription Successfully Issued!</strong></div>"
                            + "<div class='info-box'>"
                            + "<p><strong>Prescribed by:</strong> Dr. " + escapeHtml(doctorName) + " (Verified Doctor ID: " + escapeHtml(sessionUser != null ? sessionUser : "System") + ")</p>"
                            + "<p><strong>Patient ID:</strong> " + escapeHtml(patientId) + "</p>"
                            + "<p><strong>Prescription:</strong> " + escapeHtml(prescription) + "</p>"
                            + "<p><strong>Dosage Time Limit:</strong> " + expiryDisplay + "</p>"
                            + "<p class='hint'>Patient will be able to view this prescription until the dosage deadline. Afterward, it will display blurred with the completed stamp.</p>"
                            + "</div>"
                            + "<p style='text-align:center;'><a href='Prescription.html' class='btn'>New Prescription</a> <a href='dashboard.html' class='btn secondary'>Dashboard</a></p>"
                        );
                    } else {
                        renderPage(out, "Error",
                            "<div class='alert error'>Failed to save prescription. Please try again.</div>"
                            + "<p style='text-align:center;'><a href='Prescription.html' class='btn'>Try Again</a></p>"
                        );
                    }
                }
            } else if ("fetch".equals(action)) {
                // =========================================================================
                // PRIVACY ENFORCEMENT: Patient can ONLY see their own prescriptions!
                // =========================================================================
                String activePatientFilter = fetchPatientId;
                if ("Patient".equalsIgnoreCase(sessionRole) && sessionUser != null) {
                    // Lock patient to their own identifier
                    activePatientFilter = sessionUser;
                }

                String selectQuery = "SELECT p.*, a.patientName FROM prescriptions p JOIN appointments a ON p.patientId = a.patientId "
                                   + "WHERE (p.patientId = ? OR a.patientName LIKE ? OR p.patientId LIKE ?) ORDER BY p.id DESC";
                pstmt = conn.prepareStatement(selectQuery);
                pstmt.setString(1, activePatientFilter);
                pstmt.setString(2, "%" + activePatientFilter.replace("patient_", "") + "%");
                pstmt.setString(3, "%" + activePatientFilter + "%");
                rs = pstmt.executeQuery();

                StringBuilder content = new StringBuilder();
                if ("Patient".equalsIgnoreCase(sessionRole)) {
                    content.append("<div class='role-notice'>👤 <strong>My Personal Prescriptions</strong> (Account: ")
                           .append(escapeHtml(sessionUser)).append(")</div>");
                } else {
                    content.append("<h2 style='text-align:center; color:#2c3e50; margin-bottom:20px;'>Prescriptions for Patient: ")
                           .append(escapeHtml(activePatientFilter)).append("</h2>");
                }

                boolean found = false;
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
                Date now = new Date();

                while (rs.next()) {
                    found = true;
                    String patientName = rs.getString("patientName");
                    String docName = rs.getString("doctorName");
                    String rxText = rs.getString("prescription");
                    Timestamp validUntil = rs.getTimestamp("valid_until");

                    boolean isExpired = (validUntil != null && validUntil.before(now));
                    String validUntilStrFormatted = (validUntil != null) ? sdf.format(validUntil) : "Indefinite";

                    content.append("<div class='rx-card ").append(isExpired ? "rx-expired" : "rx-active").append("'>");

                    // Status Header
                    if (isExpired) {
                        content.append("<div class='rx-status-banner expired-banner'>")
                               .append("⚠️ <strong>ALLOWED DOSE TIME HAS COMPLETED</strong> (Expired on: ")
                               .append(validUntilStrFormatted).append(")")
                               .append("</div>");
                    } else {
                        content.append("<div class='rx-status-banner active-banner'>")
                               .append("🟢 <strong>ACTIVE PRESCRIPTION</strong> (Allowed dosage until: ")
                               .append(validUntilStrFormatted).append(")")
                               .append("</div>");
                    }

                    // Metadata
                    content.append("<div class='rx-meta'>")
                           .append("<div><strong>Patient Name:</strong> ").append(escapeHtml(patientName)).append("</div>")
                           .append("<div><strong>Prescribing Doctor:</strong> Dr. ").append(escapeHtml(docName)).append("</div>")
                           .append("<div><strong>Time Limit:</strong> ").append(validUntilStrFormatted).append("</div>")
                           .append("</div>");

                    // Body with Blur + Stamp if expired
                    content.append("<div class='rx-body-wrapper'>");
                    if (isExpired) {
                        content.append("<div class='stamp-watermark'>")
                               .append("ALLOWED DOSE TIME HAS COMPLETED<br>")
                               .append("<span class='stamp-date-badge'>EXPIRED: ").append(validUntilStrFormatted).append("</span>")
                               .append("</div>");
                    }
                    content.append("<div class='rx-text ").append(isExpired ? "blurred-content" : "").append("'>")
                           .append("<strong style='display:block; margin-bottom:6px;'>Medication & Instructions:</strong>")
                           .append(escapeHtml(rxText).replace("\n", "<br>"))
                           .append("</div>");
                    content.append("</div>");

                    if (isExpired) {
                        content.append("<div class='rx-expired-notice'>")
                               .append("ℹ️ <em>The dosage duration set by Dr. ").append(escapeHtml(docName))
                               .append(" has completed. This record is archived. Do not consume without a new medical review.</em>")
                               .append("</div>");
                    }

                    content.append("</div>");
                }

                if (!found) {
                    content.append("<div class='alert info'>No prescription found for: <strong>")
                           .append(escapeHtml(activePatientFilter)).append("</strong>.</div>");
                }

                content.append("<p style='text-align:center; margin-top:25px;'>")
                       .append("<a href='Prescription.html' class='btn'>Back</a> ")
                       .append("<a href='dashboard.html' class='btn secondary'>Dashboard</a>")
                       .append("</p>");

                renderPage(out, "Prescription History", content.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            renderPage(out, "Database Error",
                "<div class='alert error'>Database Error: " + escapeHtml(e.getMessage()) + "</div>"
                + "<p style='text-align:center;'><a href='Prescription.html' class='btn'>Back</a></p>"
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

    private String resolveDoctorName(Connection conn, String sessionUser, String fallback) {
        if (sessionUser != null) {
            try {
                String sql = "SELECT name FROM doctors WHERE LOWER(name) LIKE ? LIMIT 1";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    String key = "%" + sessionUser.replace("dr_", "").replace("_", " ") + "%";
                    pstmt.setString(1, key.toLowerCase());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) return rs.getString("name");
                    }
                }
            } catch (Exception ignored) {}
            if (sessionUser.toLowerCase().startsWith("dr_")) {
                String raw = sessionUser.substring(3).replace("_", " ");
                return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
            }
            return sessionUser;
        }
        return (fallback != null && !fallback.trim().isEmpty()) ? fallback.trim() : "Authorized Physician";
    }

    private void renderPage(PrintWriter out, String title, String bodyContent) {
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=5.0'><title>" + title + "</title><style>"
            + "*, *::before, *::after { box-sizing: border-box; }"
            + "body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px 14px; background: linear-gradient(135deg, rgba(15, 23, 42, 0.75), rgba(13, 148, 136, 0.65)), url('images/DC3.jpg'); background-size: cover; background-position: center; background-attachment: fixed; min-height: 100vh; display: flex; justify-content: center; align-items: flex-start; }"
            + ".page-container { width: 100%; max-width: 850px; background: rgba(255, 255, 255, 0.98); border-radius: 16px; box-shadow: 0 15px 35px rgba(0,0,0,0.35); padding: clamp(18px, 4vw, 36px); backdrop-filter: blur(8px); }"
            + "h1 { text-align: center; color: #0f172a; margin-top: 0; margin-bottom: 20px; font-size: clamp(1.3rem, 4vw, 1.65rem); font-weight: 800; }"
            + ".role-notice { background: #e0f2fe; color: #0369a1; padding: 10px 16px; border-radius: 10px; margin-bottom: 20px; font-size: 14px; text-align: center; font-weight: 600; }"
            + ".alert { padding: 14px 18px; border-radius: 10px; margin-bottom: 20px; font-size: 15px; line-height: 1.5; }"
            + ".alert.success { background-color: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }"
            + ".alert.error { background-color: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }"
            + ".alert.info { background-color: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; text-align: center; }"
            + ".info-box { background: #f8fafc; border: 1.5px solid #e2e8f0; border-radius: 12px; padding: 18px; margin-bottom: 20px; }"
            + ".info-box p { margin: 8px 0; font-size: 15px; color: #334155; word-break: break-word; }"
            + ".info-box .hint { font-size: 13px; color: #64748b; font-style: italic; border-top: 1px solid #e2e8f0; padding-top: 10px; margin-top: 12px; }"
            + ".action-group { display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }"
            + ".btn { display: inline-flex; align-items: center; justify-content: center; padding: 11px 20px; background-color: #0284c7; color: #fff; text-decoration: none; border-radius: 10px; font-weight: bold; font-size: 14px; min-height: 44px; transition: all 0.2s; text-align: center; }"
            + ".btn:hover { background-color: #0369a1; transform: translateY(-2px); }"
            + ".btn.secondary { background-color: #0d9488; }"
            + ".btn.secondary:hover { background-color: #0f766e; }"
            + ".rx-card { background: #ffffff; border-radius: 12px; border: 1.5px solid #e2e8f0; margin-bottom: 24px; box-shadow: 0 4px 14px rgba(0,0,0,0.06); overflow: hidden; }"
            + ".rx-status-banner { padding: 10px 16px; font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; font-weight: 700; }"
            + ".active-banner { background-color: #f0fdf4; color: #166534; border-bottom: 2px solid #86efac; }"
            + ".expired-banner { background-color: #fef2f2; color: #b91c1c; border-bottom: 2px solid #fca5a5; }"
            + ".rx-meta { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 10px; padding: 14px 18px; background: #f8fafc; border-bottom: 1px solid #e2e8f0; font-size: 13px; color: #475569; }"
            + ".rx-body-wrapper { position: relative; padding: 25px 20px; min-height: 120px; overflow: hidden; background: #fff; }"
            + ".rx-text { font-size: 15px; line-height: 1.6; color: #1e293b; }"
            + ".blurred-content { filter: blur(7px); -webkit-filter: blur(7px); opacity: 0.35; user-select: none; pointer-events: none; }"
            + ".stamp-watermark {"
            + "    position: absolute;"
            + "    top: 50%;"
            + "    left: 50%;"
            + "    transform: translate(-50%, -50%) rotate(-10deg);"
            + "    border: 3.5px solid #dc2626;"
            + "    color: #dc2626;"
            + "    font-family: 'Arial Black', Impact, sans-serif;"
            + "    font-size: clamp(13px, 3.8vw, 19px);"
            + "    font-weight: 900;"
            + "    text-transform: uppercase;"
            + "    padding: 10px clamp(12px, 3.5vw, 24px);"
            + "    border-radius: 10px;"
            + "    background-color: rgba(255, 255, 255, 0.96);"
            + "    box-shadow: 0 0 20px rgba(220, 38, 38, 0.4), inset 0 0 10px rgba(220, 38, 38, 0.1);"
            + "    letter-spacing: 1.5px;"
            + "    text-align: center;"
            + "    z-index: 10;"
            + "    pointer-events: none;"
            + "    max-width: 92%;"
            + "    box-sizing: border-box;"
            + "    line-height: 1.3;"
            + "}"
            + ".stamp-date-badge {"
            + "    display: inline-block;"
            + "    margin-top: 6px;"
            + "    padding: 3px 10px;"
            + "    background: #fef2f2;"
            + "    color: #b91c1c;"
            + "    border: 1.5px solid #f87171;"
            + "    border-radius: 6px;"
            + "    font-size: 0.65em;"
            + "    font-weight: 800;"
            + "    letter-spacing: 0.8px;"
            + "}"
            + ".rx-expired-notice { padding: 10px 18px; background: #fff7ed; color: #c2410c; font-size: 13px; border-top: 1px solid #ffedd5; line-height: 1.4; }"
            + "</style></head><body><div class='page-container'>"
            + "<h1>Prescription Record Management</h1>"
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
