package jdbc.dao;

import jdbc.entities.MethodChangeLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MethodsChangelogDAO {
    private final String url;
    private final static Logger logger = LoggerFactory.getLogger(MethodsChangelogDAO.class);

    public MethodsChangelogDAO(String url) {
        this.url = url;
    }

    public int insertMethodsChanges(List<MethodChangeLogEntity> entities) {
        final String sql = "insert into methodsChangeLog(dtChanged, authorName, branchName, signatureId) values(?,?,?,?)";
        final Optional<Connection> connectionOpt = Utils.connect(url);
        final AtomicInteger updatedObjectsCount = new AtomicInteger();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                entities.forEach(y -> {
                    try {
                        statement.setDate(1, y.getDateChanged());
                        statement.setString(2, y.getAuthorName());
                        statement.setString(3, y.getBranchName());
                        statement.setInt(4, y.getSignatureId());
                        statement.addBatch();
                    } catch (SQLException e) {
                        logger.error("Sql exception occured while trying to prepare insert statements to methodsChangeLog table", e);
                    }
                });
                updatedObjectsCount.set(
                        Arrays.stream(statement.executeBatch())
                                .reduce((a, b) -> a + b).orElse(0));
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to execute batch insert to methodsChangeLog table", e);
            }
        });

        return updatedObjectsCount.get();
    }
}
