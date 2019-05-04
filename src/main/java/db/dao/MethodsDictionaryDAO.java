package db.dao;

import db.entities.MethodChangeLogEntity;
import db.entities.MethodDictionaryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;
import state.MethodInfo;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodsDictionaryDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private final String url;
    private final Optional<Connection> connectionOpt;

    public MethodsDictionaryDAO(String url) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.url = url;
        connectionOpt = Utils.connect(url);
    }

    @Override
    protected void finalize() throws Throwable {

        connectionOpt.ifPresent(x -> {
            try {
                x.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        super.finalize();
    }

    public int addToDictionary(List<MethodDictionaryEntity> entities) {
        final String sql = "insert or ignore into methodsDictionary(fullSignature, startOffset, fileName) values(?,?,?)";
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
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
        });

        return updatedObjectsCount.get();
    }

    public int removeFromDictionary(String name) {
        final String sql = "delete from methodsDictionary where fullSignature = ?";
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, name);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Sql exception occured while trying to delete entry from methodsDictionary table", e);
            }
        });
        return updatedObjectsCount.get();
    }

    public int getMethodId(String fullSignature) {
        final String selectQuery = "SELECT id from methodsDictionary where fullSignature = ?";
        final AtomicInteger methodId = new AtomicInteger(-1);
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement pstmt = connectionOpt.get().prepareStatement(selectQuery)) {
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
        });
        return methodId.get();
    }

    public List<MethodChangeLogEntity> buildChangelogs(List<MethodInfo> changes) {
        final String questionMarks = String.join(", ", Collections.nCopies(changes.size(), "?"));
        final String sql = "select id from methodsDictionary where fullSignature in (" + questionMarks + ")";

        final List<MethodChangeLogEntity> entities = new LinkedList<>();
        final AtomicInteger counter = new AtomicInteger();
        final Iterator<MethodInfo> iter = changes.iterator();

        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
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
        });
        return entities;
    }

    public void upsertOfNotChangedMethodEntries(List<MethodDictionaryEntity> entities) {
        final String sql = "insert into methodsDictionary(fullSignature, startOffset, fileName)  values (?, ?, ?)\n" +
                "\n" +
                "on conflict(fullSignature) do update set startOffset=?";

        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                entities.forEach(entity -> {
                    try {
                        statement.setString(1, entity.getFullMethodSignature());
                        statement.setInt(2, entity.getStartOffset());
                        statement.setString(3, entity.getFileName());
                        statement.setInt(4, entity.getStartOffset());
                        statement.addBatch();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
                statement.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public int updateBySignature(String oldSignature, MethodDictionaryEntity entity) {
        final String sql = "update methodsDictionary set fullSignature = ?, fileName = ?, "
                + "startOffset = ? "
                + "where fullSignature = ?";

        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, entity.getFullMethodSignature());
                statement.setString(2, entity.getFileName());
                statement.setInt(3, entity.getStartOffset());
                statement.setString(4, oldSignature);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                //logger.error("Sql exception occured while trying to update methodsDictionary table", e);
            }
        });
        return updatedObjectsCount.get();
    }
}
