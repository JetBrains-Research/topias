package jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseInitialization {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseInitialization.class);
    public static void createNewDatabase(String path) {
        final String url = "jdbc:sqlite:" + path;

        final String initialSql = "create table methodsDictionary (\n" +
                "  id integer not null primary key,\n" +
                "  fullSignature varchar(1024),\n" +
                "  startOffset integer not null\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE methodsChangeLog (\n" +
                "  dtChanged timestamp primary key not null,\n" +
                "  authorName varchar(512) not null,\n" +
                "  branchName varchar(512) not null,\n" +
                "  signatureId integer not null,\n" +
                "  CONSTRAINT fk_sig_id\n" +
                "    FOREIGN KEY (signatureId)\n" +
                "    REFERENCES methodsDictionary(id)\n" +
                ");\n" +
                "\n" +
                "create view methodsChangeLogView\n" +
                "  as select * from methodsChangeLog join methodsDictionary sI on methodsChangeLog.signatureId = sI.id;";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                final DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
                final Statement statement = conn.createStatement();
                statement.execute(initialSql);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
