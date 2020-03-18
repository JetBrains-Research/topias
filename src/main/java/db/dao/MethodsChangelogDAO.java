package db.dao;

import db.DatabaseInitialization;
import db.entities.MethodChangeLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MethodsChangelogDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsChangelogDAO.class);
    private Connection connection;

    public MethodsChangelogDAO(String url) {
        this.connection = DatabaseInitialization.getConnection(url);
    }

    public synchronized void insertMethodsChanges(List<MethodChangeLogEntity> entities) {
        if (entities == null || entities.isEmpty())
            return;

        final long commitTime = entities.get(0).getDateChanged();

        final String insertQuery = "insert into methodsChangeLog(dtChanged, authorName, branchName, signatureId) values(?,?,?,?)";

        final String truncateTempTable = "delete from tempStatsData;";

        final String insertStatDailyInTemp = "insert into statsData\n" +
                "select dtDateTime, discrType, signatureId, branchName, changesC from (\n" +
                "       select datetime(ROUND(dtChanged / 1000), 'unixepoch', 'start of day') as dtDateTime,\n" +
                "              0 as discrType,\n" +
                "              signatureId,\n" +
                "              branchName,\n" +
                "              count(*) as changesC\n" +
                "       from methodsChangeLog\n" +
                "       where dtChanged = ?\n" +
                "       group by signatureId, branchName)" +
                " where true \n" +
                "on conflict (dtDateTime, discrType, signatureId) do update set changesCount=changesCount+1";

        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            entities.forEach(y -> {
                try {
                    statement.setLong(1, y.getDateChanged());
                    statement.setString(2, y.getAuthorName());
                    statement.setString(3, y.getBranchName());
                    statement.setInt(4, y.getSignatureId());
                    statement.addBatch();
                } catch (SQLException e) {
                    logger.error("Sql exception occured while trying to prepare insert statements to methodsChangeLog table", e);
                }
            });
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();e.printStackTrace();
            logger.error("Sql exception occured while trying to execute batch insert to methodsChangeLog table", e);
        }

        try (PreparedStatement statement = connection.prepareStatement(insertStatDailyInTemp)) {
            statement.setLong(1, commitTime);
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection = null;
    }
}
