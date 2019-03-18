package jdbc;

import java.sql.*;

public class DatabaseInitialization {
    public static void createNewDatabase(String path) {
        final String url = "jdbc:sqlite:" + path;

        final String methodsChangeLog = "create table signatureIds (\n" +
                "\tid integer not null primary key,\n" +
                "  \tfullSignature varchar(1024)\n" +
                ")\n" +
                "\n" +
                "CREATE TABLE methodsChangeLog (\n" +
                "  dtChanged timestamp primary key not null,\n" +
                "  authorName varchar(512) not null,\n" +
                "  signatureId integer not null,\n" +
                "  branchName varchar(512) not null,\n" +
                "  CONSTRAINT fk_sig_id\n" +
                "    FOREIGN KEY (signatureId)\n" +
                "    REFERENCES signatureIds(id)\n" +
                ")";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                final DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                final Statement statement = conn.createStatement();
                statement.execute(methodsChangeLog);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
