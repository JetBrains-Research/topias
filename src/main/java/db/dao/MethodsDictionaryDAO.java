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

public class MethodsDictionaryDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private final Connection connection;

    public MethodsDictionaryDAO() {
        this.connection = DatabaseInitialization.getConnection();
    }

    public int addToDictionary(List<MethodDictionaryEntity> entities) {
        final String sql = "insert or ignore into methodsDictionary(fullSignature, startOffset, fileName) values(?,?,?)";
        final AtomicInteger updatedObjectsCount = new AtomicInteger();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            entities.forEach(entity -> {
                try {
                    statement.setString(1, entity.getFullMethodSignature());
                    statement.setInt(2, entity.getStartOffset());
                    statement.setString(3, entity.getFileName());
                    statement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Sql exception occured while trying to insert new entry to methodsDictionary table", e);
        }

        return updatedObjectsCount.get();
    }

    public int removeFromDictionary(String name) {
        final String sql = "delete from methodsDictionary where fullSignature = ?";
        final AtomicInteger updatedObjectsCount = new AtomicInteger();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Sql exception occured while trying to delete entry from methodsDictionary table", e);
        }

        return updatedObjectsCount.get();
    }

    public int getMethodId(String fullSignature) {
        final String selectQuery = "SELECT id from methodsDictionary where fullSignature = ?";
        final AtomicInteger methodId = new AtomicInteger(-1);

        try (PreparedStatement pstmt = connection.prepareStatement(selectQuery)) {
            pstmt.setString(1, fullSignature);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                methodId.set(resultSet.getInt(1));
            }
            resultSet = null;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Exception occured while trying to get method id by signature name");
            System.out.println(e.getMessage());
        }

        return methodId.get();
    }

    public List<MethodChangeLogEntity> buildChangelogs(List<MethodInfo> changes) {
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
            e.printStackTrace();
            //logger.error("Sql exception occured while trying to execute batch update to methodsDictionary table", e);
        }
        return entities;
    }

    public void upsertOfNotChangedMethodEntries(List<MethodDictionaryEntity> entities) {
        final String sqlUpd = "update methodsDictionary set startOffset = ? "
                + "where fullSignature = ?";

        final String questionMarks = String.join(", ", Collections.nCopies(entities.size(), "?"));
        final String sqlSelect = "select fullSignature from methodsDictionary where fullSignature in (" + questionMarks + ")";
        final Set<String> names = new HashSet<>();
        final AtomicInteger counter = new AtomicInteger();
        try (PreparedStatement statement = connection.prepareStatement(sqlSelect)) {
            entities.forEach(entity -> {
                try {
                    statement.setString(counter.incrementAndGet(), entity.getFullMethodSignature());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                names.add(
                        resultSet.getString(1)
                );
            }
            resultSet = null;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement statement = connection.prepareStatement(sqlUpd)) {
            entities.stream().filter(x -> names.contains(x.getFullMethodSignature())).forEach(x -> {
                try {
                    statement.setInt(1, x.getStartOffset());
                    statement.setString(2, x.getFullMethodSignature());
                    statement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int updateBySignature(List<Pair<String, MethodDictionaryEntity>> sigEntPairs) {
        final String sql = "update methodsDictionary set fullSignature = ?, fileName = ?, "
                + "startOffset = ? "
                + "where fullSignature = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            sigEntPairs.forEach(x -> {
                try {
                    statement.setString(1, x.getSecond().getFullMethodSignature());
                    statement.setString(2, x.getSecond().getFileName());
                    statement.setInt(3, x.getSecond().getStartOffset());
                    statement.setString(4, x.getFirst());
                    statement.addBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
            //logger.error("Sql exception occured while trying to update methodsDictionary table", e);
        }

        return 0;
    }
}
