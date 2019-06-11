package db.dao;

import db.DatabaseInitialization;
import db.entities.MethodChangeLogEntity;
import db.entities.MethodDictionaryEntity;
import kotlin.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import state.MethodInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MethodsDictionaryDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private Connection connection;

    public MethodsDictionaryDAO(String url) {
        this.connection = DatabaseInitialization.getConnection(url);
    }

    public synchronized int addToDictionary(List<MethodDictionaryEntity> entities) {
        final String sql = "insert or ignore into methodsDictionary(fullSignature, fileName) values(?,?)";
        final AtomicInteger updatedObjectsCount = new AtomicInteger();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            entities.forEach(entity -> {
                try {
                    statement.setString(1, entity.getFullMethodSignature());
                    statement.setString(2, entity.getFileName());
                    statement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            logger.error("Sql exception occured while trying to insert new entry to methodsDictionary table", e);
        }

        return updatedObjectsCount.get();
    }

    public synchronized int removeFromDictionary(String name) {
        final String sql = "delete from methodsDictionary where fullSignature = ?";
        final AtomicInteger updatedObjectsCount = new AtomicInteger();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            logger.error("Sql exception occured while trying to delete entry from methodsDictionary table", e);
        }

        return updatedObjectsCount.get();
    }

    public synchronized List<MethodChangeLogEntity> buildChangelogs(List<MethodInfo> changes) {
        final String questionMarks = String.join(", ", Collections.nCopies(changes.size(), "?"));
        final String sql = "select id from methodsDictionary where fullSignature in (" + questionMarks + ")";

        final List<MethodChangeLogEntity> entities = new LinkedList<>();
        final AtomicInteger counter = new AtomicInteger();
        final Iterator<MethodInfo> iter = changes.iterator();


        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            changes.forEach(y -> {
                try {
                    statement.setString(counter.incrementAndGet(), y.getMethodFullName());
                } catch (SQLException e) {
                    logger.error("Sql exception occured while trying to prepare update statements for methodsDictionary table", e);
                }
            });
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                MethodInfo info = iter.next();
                entities.add(new MethodChangeLogEntity(
                        info.getTimeChangeMade(),
                        info.getAuthorInfo(),
                        info.getBranchName(),
                        resultSet.getInt(1)
                ));
            }
            resultSet = null;
        } catch (SQLException e) {
            logger.error("Sql exception occured while trying to execute batch update to methodsDictionary table", e);
        }
        return entities;
    }

    public synchronized void updateBySignature(List<Pair<String, MethodDictionaryEntity>> sigEntPairs) {
        final String questionMarks = String.join(", ", Collections.nCopies(sigEntPairs.size(), "?"));
        final String searchSql = "select * from methodsDictionary where fullSignature in (" + questionMarks + ")";
        final AtomicInteger counter = new AtomicInteger();
        final List<MethodDictionaryEntity> entitiesForUpdate = new LinkedList<>();

        try (PreparedStatement statement = connection.prepareStatement(searchSql)) {
            sigEntPairs.forEach(y -> {
                try {
                    statement.setString(counter.incrementAndGet(), y.getFirst());
                } catch (SQLException e) {
                    logger.error("Sql exception occured while trying to prepare update statements for methodsDictionary table", e);
                }
            });

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                entitiesForUpdate.add(new MethodDictionaryEntity(
                        resultSet.getString(2),
                        0,
                        resultSet.getString(3)
                ));
            }
        } catch (SQLException e) {
            logger.error("Sql exception occured while trying to execute batch update to methodsDictionary table", e);
        }

        final String sql = "update methodsDictionary set fullSignature = ?, fileName = ? "
                + "where fullSignature = ?";

        final Iterator<Pair<String, MethodDictionaryEntity>> iter = sigEntPairs.iterator();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            while (iter.hasNext()) {
                try {
                    final Pair<String, MethodDictionaryEntity> x = iter.next();
                    statement.setString(1, x.getSecond().getFullMethodSignature());
                    statement.setString(2, x.getSecond().getFileName());
                    statement.setString(3, x.getFirst());
                    statement.addBatch();
                    iter.remove();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            logger.error("Sql exception occured while trying to update methodsDictionary table", e);
        }

        //inserting remaining methods
        addToDictionary(sigEntPairs.stream().map(Pair::getSecond)
            .collect(Collectors.toList())
        );
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection = null;
    }
}
