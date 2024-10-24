import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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

@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RegisterServlet() {
        super();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("t1");
        String email = request.getParameter("t2");
        String password = request.getParameter("t3");
        String confirmPassword = request.getParameter("t4");
        String role = request.getParameter("role");

        // Validate input
        if (username == null || email == null || password == null || confirmPassword == null || role == null) {
            sendResponse(out, "All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            sendResponse(out, "Passwords do not match!");
            return;
        }

        DBConnection dbc = new DBConnection();
        Connection conn = null;

        try {
            conn = dbc.dbconnection();

            // Check if the username already exists
            PreparedStatement checkUserStmt = conn.prepareStatement("SELECT * FROM loginone WHERE username = ?");
            checkUserStmt.setString(1, username);
            ResultSet rs = checkUserStmt.executeQuery();

            if (rs.next()) {
                sendResponse(out, "Username already exists! Please choose another username.");
            } else {
                // Insert the new user into the database
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO loginone (username, email, password, role) VALUES (?, ?, ?, ?)");
                pstmt.setString(1, username);
                pstmt.setString(2, email);
                pstmt.setString(3, password);
                pstmt.setString(4, role);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    sendResponse(out, "Registration successful!<br><a href='Login.html' style='display:inline-block; padding:10px 20px; margin-top:10px; background-color:#007bff; color:#fff; text-decoration:none; border-radius:5px;'>Go to Login</a>");
                } else {
                    sendResponse(out, "Registration failed! Please try again.");
                }
            }
        } catch (SQLException e) {
            // Print the exception stack trace directly to the response
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            sendResponse(out, "Database error! Details: " + sw.toString());
        } finally {
            // Ensure the connection is closed
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Print the exception stack trace if closing the connection fails
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    sendResponse(out, "Connection close error! Details: " + sw.toString());
                }
            }
        }
    }

    private void sendResponse(PrintWriter out, String message) {
        out.print("<html><head><style>");
        out.print("body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; text-align: center; padding: 50px; }");
        out.print("h1 { color: #007bff; }");
        out.print("a { text-decoration: none; color: #ffffff; background-color: #007bff; padding: 10px 20px; border-radius: 5px; transition: background-color 0.3s; }");
        out.print("a:hover { background-color: #0056b3; }");
        out.print("</style></head><body>");
        out.print("<h1>Registration Status</h1>");
        out.print("<p>" + message + "</p>");
        out.print("</body></html>");
    }
}