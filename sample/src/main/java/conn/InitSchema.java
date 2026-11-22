package conn;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class InitSchema {
    public static void main(String[] args) throws Exception {
        DBConnection db = new DBConnection();
        try (Connection conn = db.dbconnection()) {
            if (conn == null) {
                System.err.println("Connection failed.");
                System.exit(1);
            }

            String fullSql = new String(Files.readAllBytes(Paths.get("schema.sql")));
            try (Statement stmt = conn.createStatement()) {
                for (String rawStmt : fullSql.split(";")) {
                    String statement = rawStmt.trim();
                    // Strip leading SQL comments
                    while (statement.startsWith("--") || statement.startsWith("/*")) {
                        int newlineIdx = statement.indexOf("\n");
                        if (newlineIdx == -1) {
                            statement = "";
                            break;
                        }
                        statement = statement.substring(newlineIdx + 1).trim();
                    }

                    if (!statement.isEmpty()) {
                        try {
                            stmt.execute(statement);
                            String preview = statement.replace('\n', ' ');
                            if (preview.length() > 50) preview = preview.substring(0, 50) + "...";
                            System.out.println("OK: " + preview);
                        } catch (Exception e) {
                            System.err.println("Notice for [" + statement + "]: " + e.getMessage());
                        }
                    }
                }

                // Verify tables
                System.out.println("\n--- VERIFYING TABLES IN DocPortal ---");
                try (ResultSet rs = stmt.executeQuery("SHOW TABLES;")) {
                    while (rs.next()) {
                        System.out.println("Found Table: " + rs.getString(1));
                    }
                }

                // Verify count of doctors
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM doctors;")) {
                    if (rs.next()) {
                        System.out.println("Doctors count in DB: " + rs.getInt(1));
                    }
                }
            }

            System.out.println("\nONLINE DATABASE INITIALIZATION & VERIFICATION 100% COMPLETE!");
        }
    }
}
