import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import conn.DBConnection;

public class LoginServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String flag = "false";
        String username = request.getParameter("t1");
        String password = request.getParameter("t2");

        DBConnection dbc = new DBConnection();
        Connection conn = dbc.dbconnection();

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            if (conn == null) {
                throw new SQLException("Unable to establish a database connection.");
            }

            String sql = "SELECT * FROM loginone WHERE username=? AND password=?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                flag = "true";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("<html><body>");
            out.println("<h2>Database Connection Error</h2>");
            out.println("<p>There was an error connecting to the database. Please try again later.</p>");
            out.println("<p>Error details: " + e.getMessage() + "</p>");
            out.println("</body></html>");
            return; // Exit the method to avoid further processing
        } finally {
            // Close resources in the finally block to ensure they are closed even if an exception occurs
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Inline CSS for response
        out.print("<html><head><style>");
        out.print("body { ");
        out.print("    font-family: Arial, sans-serif; ");
        out.print("    background-image: url('images/log_in.png'); ");
        out.print("    background-size: cover; ");
        out.print("    background-position: center; ");
        out.print("    background-repeat: no-repeat; ");
        out.print("    background-attachment: fixed; ");
        out.print("    color: #fff; ");
        out.print("    display: flex; ");
        out.print("    justify-content: center; ");
        out.print("    align-items: center; ");
        out.print("    height: 100vh; ");
        out.print("    margin: 0; ");
        out.print("    text-align: center; ");
        out.print("} ");
        out.print("div.container { ");
        out.print("    background: rgba(0, 0, 0, 0.6); ");
        out.print("    border-radius: 8px; ");
        out.print("    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3); ");
        out.print("    padding: 20px; ");
        out.print("    max-width: 400px; ");
        out.print("    width: 90%; ");
        out.print("} ");
        out.print("h1 { ");
        out.print("    color: #007bff; ");
        out.print("    margin-bottom: 20px; ");
        out.print("} ");
        out.print("p { ");
        out.print("    font-size: 18px; ");
        out.print("    margin-bottom: 20px; ");
        out.print("} ");
        out.print("a { ");
        out.print("    text-decoration: none; ");
        out.print("    color: #ffffff; ");
        out.print("    background-color: #007bff; ");
        out.print("    padding: 10px 20px; ");
        out.print("    border-radius: 5px; ");
        out.print("    transition: background-color 0.3s; ");
        out.print("} ");
        out.print("a:hover { ");
        out.print("    background-color: #0056b3; ");
        out.print("} ");
        out.print("</style></head><body>");
        
        out.print("<div class='container'>");

        if (flag.equals("true")) {
            out.print("<h1>Login Successful</h1>");
            out.print("<p>Welcome back! Click the button below to proceed to your dashboard.</p>");
            out.print("<a href='dashboard.html'>Go to Dashboard</a>");
        } else {
            out.print("<h1>Login Failed</h1>");
            out.print("<p>Invalid username or password. Please try again.</p>");
            out.print("<a href='Login.html'>Go Back to Login</a>");
        }

        out.print("</div>");
        out.print("</body></html>");
    }
}
