package jdbc.dao;

import jdbc.entities.MethodDictionaryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodsDictionaryDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private final String url;

    public MethodsDictionaryDAO(String url) {
        this.url = url;
    }

    public int addToDictionary(List<MethodDictionaryEntity> entities) {
        final String sql = "insert into methodsDictionary(fullSignature, startOffset) values(?,?)";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
                try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                    entities.forEach(y -> {
                        try {
                            statement.setString(1, y.getFullMethodSignature());
                            statement.setInt(2, y.getStartOffset());
                            statement.addBatch();
                        } catch (SQLException e) {
                            logger.error("Sql exception occured while trying to prepare insert statements to methodsDictionary table", e);
                        }
                    });
                    updatedObjectsCount.set(
                            Arrays.stream(statement.executeBatch())
                                    .reduce((a, b) -> a + b).orElse(0));
                } catch (SQLException e) {
                    logger.error("Sql exception occured while trying to execute batch insert to methodsDictionary table", e);
                }
            });

        return updatedObjectsCount.get();
    }

    public int addToDictionary(MethodDictionaryEntity entity) {
        final String sql = "insert into methodsDictionary(fullSignature, startOffset) values(?,?)";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, entity.getFullMethodSignature());
                statement.setInt(2, entity.getStartOffset());
                updatedObjectsCount.set(statement.executeUpdate());
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to insert new entry to methodsDictionary table", e);
            }
        });
        return updatedObjectsCount.get();
    }

    public int removeFromDictionary(List<String> names) {
        final String sql = "delete from methodsDictionary where fullSignature = ?";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                names.forEach(y -> {
                    try {
                        statement.setString(1, y);
                        statement.addBatch();
                    } catch (SQLException e) {
                        logger.error("Sql exception occured while trying to prepare delete statements for methodsDictionary table", e);
                    }
                });
                updatedObjectsCount.set(
                        Arrays.stream(statement.executeBatch())
                                .reduce((a, b) -> a + b).orElse(0));
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to execute batch delete from methodsDictionary table", e);
            }
        });

        return updatedObjectsCount.get();
    }

    public int removeFromDictionary(String name) {
        final String sql = "delete from methodsDictionary where fullSignature = ?";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, name);
                updatedObjectsCount.set(statement.executeUpdate());
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to delete entry from methodsDictionary table", e);
            }
        });
        return updatedObjectsCount.get();
    }

    public int updateDictionary(List<MethodDictionaryEntity> entities) {
        final String sql = "update methodsDictionary set fullSignature = ?, "
                + "startOffset = ? "
                + "where id = ?";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                entities.forEach(y -> {
                    try {
                        statement.setString(1, y.getFullMethodSignature());
                        statement.setInt(2, y.getStartOffset());
                        statement.setInt(3, y.getId());
                        statement.addBatch();
                    } catch (SQLException e) {
                        logger.error("Sql exception occured while trying to prepare update statements for methodsDictionary table", e);
                    }
                });
                updatedObjectsCount.set(
                        Arrays.stream(statement.executeBatch())
                                .reduce((a, b) -> a + b).orElse(0));
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to execute batch update to methodsDictionary table", e);
            }
        });

        return updatedObjectsCount.get();
    }

    public int findIdBySignatureName(String fullSignature) {
        final String selectQuery = "SELECT id from methodsDictionary where fullSignature = ?";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger methodId = new AtomicInteger(-1);
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement pstmt = connectionOpt.get().prepareStatement(selectQuery)) {
                pstmt.setString(1, fullSignature);
                final ResultSet resultSet = pstmt.executeQuery();
                while (resultSet.next()) {
                    methodId.set(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                System.out.println("Exception occured while trying to get method id by signature name");
                System.out.println(e.getMessage());
            }
        });
        return methodId.get();
    }
}
