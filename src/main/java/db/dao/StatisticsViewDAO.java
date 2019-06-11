package db.dao;

import db.DatabaseInitialization;
import db.entities.StatisticsViewEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settings.enums.DiscrType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;


public class StatisticsViewDAO {
    private final static Logger logger = LoggerFactory.getLogger(MethodsDictionaryDAO.class);
    private Connection connection;

    public StatisticsViewDAO(String url) {
        this.connection = DatabaseInitialization.getConnection(url);
    }

    public synchronized List<Integer> selectChangesCountDaily(String fullSigName, DiscrType period, String branchName) {
        final int days = period.equals(DiscrType.MONTH) ? 30 : 7;
        final LocalDate to = LocalDate.now().plusDays(1);
        final LocalDate from = to.minusDays(days);


        final String sql = "select dtDateTime, sum(changesCount) from statisticsView where\n" +
                "fullSignature = ? " +
                "and dtDateTime between ? and ?" +
                "and branchName = ?" +
                "group by dtDateTime";

        Integer[] changesData = new Integer[days];
        Arrays.fill(changesData, 0);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullSigName);
            statement.setString(2, from.toString());
            statement.setString(3, to.toString());
            statement.setString(4, branchName);

            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int pos = (int) DAYS.between(from, LocalDate.parse(resultSet.getString(1).split(" ")[0]));
                changesData[pos] += resultSet.getInt(2);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Sql exception occured while trying to get statistics data", e);
        }


        return Arrays.asList(changesData);
    }

    public synchronized List<StatisticsViewEntity> getMostChangedMethods(DiscrType period, String branchName) {
        final LocalDate to = LocalDate.now().plusDays(1);
        final LocalDate from = to.minusDays(period.equals(DiscrType.WEEK) ? 7 : 30);
        final String sql = "select fullSignature,\n" +
                "       sum(changesCount) as changesC,\n" +
                "       fileName\n" +
                "from statisticsView where dtDateTime between ? and ? and branchName = ? group by fullSignature order by changesC desc limit 15;";

        final List<StatisticsViewEntity> entities = new LinkedList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, from.toString());
            statement.setString(2, to.toString());
            statement.setString(3, branchName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                entities.add(new StatisticsViewEntity(
                        resultSet.getString(1),
                        resultSet.getInt(2),
                        resultSet.getString(3)
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entities;
    }

    public synchronized List<StatisticsViewEntity> getStatDataForFile(String fileName, DiscrType period, String branchName) {
        final LocalDate to = LocalDate.now().plusDays(1);
        final LocalDate from = to.minusDays(period.equals(DiscrType.WEEK) ? 7 : 30);

        final String sql = "select fullSignature,\n" +
                "       sum(changesCount) as changesC,\n" +
                "       fileName\n" +
                "from statisticsView where discrType = 0 and branchName = ? and fileName = ? and dtDateTime between ? and ? group by fullSignature;";

        final List<StatisticsViewEntity> entities = new LinkedList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, branchName);
            statement.setString(2, fileName);
            statement.setString(3, from.toString());
            statement.setString(4, to.toString());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                entities.add(new StatisticsViewEntity(
                        resultSet.getString(1),
                        resultSet.getInt(2),
                        resultSet.getString(3)
                ));
            }
            resultSet = null;
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Sql exception occured while trying to statistics data", e);
        }

        return entities;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        connection = null;
    }
}
