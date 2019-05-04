package db.dao;

import db.entities.StatisticsViewEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import processing.Utils;
import settings.enums.DiscrType;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class StatisticsViewDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private final String url;

    public StatisticsViewDAO(String url) {
        try {
            DriverManager.registerDriver(new org.sqlite.JDBC());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.url = "jdbc:sqlite:" + url;
    }

    public List<Integer> selectChangesCountDaily(String fullSigName, DiscrType period) {
        final LocalDate to = LocalDate.now();
        final LocalDate from = to.minusDays(period.equals(DiscrType.WEEK) ? 7 : 30);

        final String sql = "select changesCount from statisticsView where discrType = 0 " +
                "and fullSignature = ? " +
                "and dtDateTime between ? and ?";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final List<Integer> changesData = new LinkedList<>();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, fullSigName);
                statement.setLong(2, from.toEpochDay());
                statement.setLong(3, to.toEpochDay());

                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    changesData.add(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Sql exception occured while trying to get statistics data", e);
            }
        });

        try {
            connectionOpt.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return changesData;
    }

    public List<StatisticsViewEntity> getStatDataForFile(String fileName, DiscrType period) {
        final LocalDate to = LocalDate.now();
        final LocalDate from = to.minusDays(period.equals(DiscrType.WEEK) ? 7 : 30);

        final String sql = "select fullSignature,\n" +
                "       sum(changesCount) as changes,\n" +
                "       fileName,\n" +
                "       startOffset\n" +
                "from statisticsView where discrType = 0 and fileName = ? and dtDateTime between ? and ? group by discrType, fullSignature;";

        final Optional<Connection> connectionOpt = Utils.connect(url);
        final List<StatisticsViewEntity> entities = new LinkedList<>();
        connectionOpt.ifPresent(x -> {
            try (PreparedStatement statement = connectionOpt.get().prepareStatement(sql)) {
                statement.setString(1, fileName);
                statement.setString(2, from.toString());
                statement.setString(3, to.toString());

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    entities.add(new StatisticsViewEntity(
                            resultSet.getString(1),
                            resultSet.getInt(2),
                            resultSet.getString(3),
                            resultSet.getInt(4)
                    ));
                }
                resultSet = null;
            } catch (SQLException e) {
                e.printStackTrace();
                logger.error("Sql exception occured while trying to statistics data", e);
            }
        });
        try {
            connectionOpt.get().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entities;
    }
}
