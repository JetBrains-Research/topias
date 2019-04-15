package jdbc.dao;

import jdbc.entities.MethodChangeLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodsChangelogDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsChangelogDAO.class);
    private final String url;

    public MethodsChangelogDAO(String url) {
        this.url = url;
    }

    public void insertMethodsChanges(List<MethodChangeLogEntity> entities) {
        final long commitTime = entities.get(0).getDateChanged();

        final String insertQuery = "insert into methodsChangeLog(dtChanged, authorName, branchName, signatureId) values(?,?,?,?)";

        final String upsertStatisticsDaily = "insert into stats\n" +
                "select *\n" +
                "from (\n" +
                "       select datetime(ROUND(dtChanged / 1000), 'unixepoch', 'start of day') as dtDateTime,\n" +
                "              1 as discrType,\n" +
                "              signatureId,\n" +
                "              count(*)                                                       as changes\n" +
                "       from methodsChangeLog\n" +
                "       where dtChanged = ?\n" +
                "       group by dtDateTime, signatureId) on conflict (dtDateTime, discrType, signatureId) do update\n" +
                "set stats.changesCount = stats.changesCount + changes;";

        final String upsertStatisticsMonthly = "insert into stats\n" +
                "select *\n" +
                "from (\n" +
                "       select datetime(ROUND(dtChanged / 1000), 'unixepoch', 'start of month') as dtDateTime,\n" +
                "              2 as discrType,\n" +
                "              signatureId,\n" +
                "              count(*)                                                       as changes\n" +
                "       from methodsChangeLog\n" +
                "       where dtChanged = ? \n" +
                "       group by dtDateTime, signatureId) on conflict (dtDateTime, discrType, signatureId) do update\n" +
                "set stats.changesCount = stats.changesCount + changes;";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(insertQuery)) {
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
                logger.error("Sql exception occured while trying to execute batch insert to methodsChangeLog table", e);
            }

            try (PreparedStatement statement = connectionOpt.get().prepareStatement(upsertStatisticsMonthly)) {
                statement.setLong(1, commitTime);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try (PreparedStatement statement = connectionOpt.get().prepareStatement(upsertStatisticsDaily)) {
                statement.setLong(1, commitTime);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        });
    }
}
