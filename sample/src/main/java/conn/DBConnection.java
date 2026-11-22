package conn;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    // Default fallback values
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/DocPortal?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "@Ani9576";

    private String driver;
    private String url;
    private String user;
    private String password;

    public DBConnection() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("Notice: db.properties could not be loaded, using defaults/env vars: " + e.getMessage());
        }

        // Priority 1: Environment Variables
        // Priority 2: db.properties file
        // Priority 3: Hardcoded defaults
        this.driver = getEnvOrProp("DB_DRIVER", props.getProperty("db.driver"), DEFAULT_DRIVER);
        this.url = getEnvOrProp("DB_URL", props.getProperty("db.url"), DEFAULT_URL);
        this.user = getEnvOrProp("DB_USER", props.getProperty("db.user"), DEFAULT_USER);
        this.password = getEnvOrProp("DB_PASSWORD", props.getProperty("db.password"), DEFAULT_PASSWORD);
    }

    private String getEnvOrProp(String envKey, String propValue, String defaultValue) {
        String envVal = System.getenv(envKey);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        if (propValue != null && !propValue.trim().isEmpty()) {
            return propValue.trim();
        }
        return defaultValue;
    }

    public Connection dbconnection() {
        Connection conn = null;
        try {
            Class.forName(driver);
            System.out.println("JDBC Driver [" + driver + "] loaded successfully.");

            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database connection established successfully to: " + getSanitizedUrl(url));
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Exception connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
        return conn;
    }

    private String getSanitizedUrl(String rawUrl) {
        if (rawUrl == null) return "null";
        // Remove password if embedded in query or URL for clean logging
        return rawUrl.replaceAll("(?i)(password=)[^&;]*", "$1*****");
    }

    public static void main(String[] args) {
        DBConnection db = new DBConnection();
        Connection conn = db.dbconnection();
        if (conn != null) {
            System.out.println("Connection test: SUCCESS!");
            try {
                conn.close();
            } catch (SQLException ignored) {}
        } else {
            System.out.println("Connection test: FAILED!");
        }
    }
}
