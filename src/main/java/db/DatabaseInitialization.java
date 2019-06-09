package db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseInitialization {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseInitialization.class);
    private static Connection connection = null;

    public static void createNewDatabase(String path) {
        final String url = "jdbc:sqlite:" + path;

        final String methodsDictionary = "create table methodsDictionary \n" +
                "(\n" +
                "  id integer primary key,\n" +
                "  fullSignature varchar(1024) unique ,\n" +
                "  startOffset integer not null,\n" +
                "  fileName varchar(1024) not null,\n" +
                "  unique (fullSignature, fileName)" +
                ");";

        final String methodDictInd = "create index fullSignatureInd\n" +
                "  on methodsDictionary(fullSignature);";

        final String methodsChangeLog = "CREATE TABLE methodsChangeLog (\n" +
                "  dtChanged timestamp not null,\n" +
                "  authorName varchar(512) not null,\n" +
                "  branchName varchar(512) not null,\n" +
                "  signatureId integer not null,\n" +
                "  CONSTRAINT fk_sig_id\n" +
                "    FOREIGN KEY (signatureId)\n" +
                "    REFERENCES methodsDictionary(id)\n" +
                ");";

        final String statsTable = "create table statsData \n" +
                "(\n" +
                "  dtDateTime   timestamp not null,\n" +
                "  discrType    integer   not null,\n" +
                "  signatureId  integer   not null,\n" +
                "  branchName varchar(512) not null,\n" +
                "  changesCount integer   not null,\n" +
                "  unique (dtDateTime, discrType, signatureId)\n" +
                ");";

        final String tempStatsTable = "create table tempStatsData \n" +
                "(\n" +
                "  dtDateTime   timestamp not null,\n" +
                "  discrType    integer   not null,\n" +
                "  signatureId  integer   not null,\n" +
                "  branchName varchar(512) not null,\n" +
                "  changesC integer   not null,\n" +
                "  unique (dtDateTime, discrType, signatureId)\n" +
                ");";

        final String statsView = "create view statisticsView as\n" +
                "select dtDateTime,\n" +
                "       discrType,\n" +
                "       fullSignature,\n" +
                "       changesCount,\n" +
                "       fileName,\n" +
                "       branchName,\n" +
                "       startOffset\n" +
                "from statsData \n" +
                "       join methodsDictionary on statsData.signatureId = id;";
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(false);
            final DatabaseMetaData meta = connection.getMetaData();
            logger.info("The driver name is " + meta.getDriverName() + "  " + meta.getDriverVersion());
            logger.info("A new database has been created.");
            final Statement statement = connection.createStatement();
            statement.execute(methodsDictionary);
            statement.execute(methodDictInd);
            statement.execute(methodsChangeLog);
            statement.execute(statsTable);
            statement.execute(tempStatsTable);
            statement.execute(statsView);
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(String path) {
        final String url = "jdbc:sqlite:" + path;
        if (connection == null) {
            try {
                DriverManager.registerDriver(new org.sqlite.JDBC());
                connection = DriverManager.getConnection(url);
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
