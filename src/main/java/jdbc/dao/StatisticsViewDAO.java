package jdbc.dao;

import jdbc.entities.StatisticsViewEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;

import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsViewDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private final String url;

    public StatisticsViewDAO(String url) {
        this.url = url;
    }

    public List<Integer> selectChangesCountDaily(String fullSigName, Date from, Date to) {
        final String sql = "select changesCount from statisticsView where discrType = 1 " +
                "and fullSignature = ? " +
                "and dtDateTime between ? and ?";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final List<Integer> changesData = new LinkedList<>();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, fullSigName);
                statement.setDate(2, from);
                statement.setDate(3, to);

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    changesData.add(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to get statistics data", e);
            }
        });

        return changesData;
    }

    public List<StatisticsViewEntity> getStatDataForFile(String fileName, Date from, Date to) {
        final String sql = "select fullSignature,\n" +
                "       sum(changesCount) as changes,\n" +
                "       fileName,\n" +
                "       startOffset\n" +
                "from statisticsView where discrType = 1 and fileName = ? and dtDateTime between ? and ? group by discrType, fullSignature;";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final List<StatisticsViewEntity> entities = new LinkedList<>();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, fileName);
                statement.setDate(2, from);
                statement.setDate(3, to);

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    entities.add(new StatisticsViewEntity(
                            resultSet.getString(1),
                            resultSet.getInt(2),
                            resultSet.getString(3),
                            resultSet.getInt(4)
                    ));
                }
            } catch (SQLException e) {
                logger.error("Sql exception occured while trying to statistics data", e);
            }
        });

        return entities;
    }
}
