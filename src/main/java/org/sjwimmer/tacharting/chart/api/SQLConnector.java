package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.implementation.model.api.key.SQLKey;
import org.ta4j.core.Bar;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;

/**
 * Interface for connections to a database to store and receive financial data
 */
public interface SQLConnector extends OHLCVDataSource<SQLKey, Void> {

    /**
     * Returns a list of all available symbol of a specific time period
     * @param table the table/time period
     * @return a list of all available symbols of this time period
     * @throws SQLException SQLException
     */
    List<SQLKey> getKeyList(GeneralTimePeriod table) throws SQLException;

    /**
     * Removes all entries which had the key of the <tt>series</tt> key consists of {@link TaTimeSeries#getName()},
     * {@link Currency#getCurrencyCode()}, {@link GeneralTimePeriod}
     * @param series the series with the key (could be empty)
     * @throws SQLException SQLException
     */
    boolean removeData(TaTimeSeries series) throws SQLException;

    boolean removeData(SQLKey key) throws SQLException;

    /**
     * Inserts the data of the <tt>series</tt> into the corresponding table (see {@link GeneralTimePeriod})
     * @param series the time series to store
     * @param replace if true existing entries will be overwritten
     * @throws SQLException SQLException
     */
    void insertData(TaTimeSeries series, boolean replace) throws SQLException;

    /**
     * Returns the last recent available bar of the time series with the <code>key</code>
     * @param key the {@link SQLKey key} that identifies the time series
     * @return the last recent bar
     */
    Bar getLastBar(SQLKey key);

    /**
     * Returns the earliest available bar of the time series with the <code>key</code>
     * @param key the {@link SQLKey key} that identifies the time series
     * @return the earliest available bar
     */
    Bar getFirstBar(SQLKey key);
}
