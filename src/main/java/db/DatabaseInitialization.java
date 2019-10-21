package db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intellij.openapi.components.ProjectComponent;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class DatabaseInitialization implements ProjectComponent {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseInitialization.class);
    private static Connection connection = null;

    public static void createNewDatabase(String path) {
        final String url = "jdbc:sqlite:" + path;
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection(url);
            final DatabaseMetaData meta = connection.getMetaData();
            logger.info("The driver name is " + meta.getDriverName() + "  " + meta.getDriverVersion());

            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase("/db/db.changelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());

            logger.info("A new database has been created.");
        } catch (SQLException | LiquibaseException e) {
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

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}
