package db.dao;

import db.entities.MethodChangeLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class MethodsChangelogDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsChangelogDAO.class);
    private final String url;

    public MethodsChangelogDAO(String url) {
        this.url = url;
    }

    public void insertMethodsChanges(List<MethodChangeLogEntity> entities) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (entities == null || entities.isEmpty())
            return;

        final long commitTime = entities.get(0).getDateChanged();

        final String insertQuery = "insert into methodsChangeLog(dtChanged, authorName, branchName, signatureId) values(?,?,?,?)";

        final String truncateTempTable = "delete from tempStatsData;";

        final String insertStatDailyInTemp = "insert into tempStatsData\n" +
                "select * from (\n" +
                "       select datetime(ROUND(dtChanged / 1000), 'unixepoch', 'start of day') as dtDateTime,\n" +
                "              0 as discrType,\n" +
                "              signatureId,\n" +
                "              count(*) as changesC\n" +
                "       from methodsChangeLog\n" +
                "       where dtChanged = ?\n" +
                "       group by datetime(ROUND(dtChanged / 1000), 'unixepoch', 'start of day'), signatureId);";

        final String upsertStatDaily = "insert into statsData select " +
                "* from " +
                "tempStatsData where true on conflict(dtDateTime, discrType, signatureId) do update set changesCount=changesCount+'changesC';";
//
//        final String insertStatMonthly = "insert into statsData\n" +
//                "select *\n" +
//                "from (\n" +
//                "       select datetime(ROUND(dtChanged / 1000), 'unixepoch', 'start of month') as dtDateTime,\n" +
//                "              1 as discrType,\n" +
//                "              signatureId,\n" +
//                "              count(*)                                                       as changesC,\n" +
//                "              (datetime(ROUND(dtChanged / 1000), 'unixepoch', 'start of day') || '|' || 1 || '|' || signatureId) as uniqueConstraint\n" +
//                "       from methodsChangeLog\n" +
//                "       where dtChanged = ? \n" +
//                "       group by dtDateTime, signatureId)\n" +
//                "WHERE true\n" +
//                "on conflict (uniqueConstraint) do update\n" +
//                "set changesCount = changesCount + changesC;";

        final Optional<Connection> connectionOpt = Utils.connect(url);

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
                e.printStackTrace();
                logger.error("Sql exception occured while trying to execute batch insert to methodsChangeLog table", e);
            }

            try (PreparedStatement statement = connectionOpt.get().prepareStatement(insertStatDailyInTemp)) {
                statement.setLong(1, commitTime);
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try (Statement statement = connectionOpt.get().createStatement()) {
                statement.executeUpdate(upsertStatDaily);
                statement.execute(truncateTempTable);
            } catch (SQLException e) {
                e.printStackTrace();
            }
//
//            try (PreparedStatement statement = connectionOpt.get().prepareStatement(insertStatMonthly)) {
//                statement.setLong(1, commitTime);
//                statement.execute();
//
//                statement.execute(upsertStatDaily);
//                statement.executeUpdate(truncateTempTable);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }

        });
    }
}
