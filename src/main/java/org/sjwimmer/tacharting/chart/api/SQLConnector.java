package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.TaTimeSeries;
import org.sjwimmer.tacharting.chart.types.GeneralTimePeriod;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;

/**
 * Interface for connections to a database to store and receive financial data
 */
public interface SQLConnector extends Connector<SQLKey> {

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
    void removeData(TaTimeSeries series) throws SQLException;

    /**
     * Inserts the data of the <tt>series</tt> into the corresponding table (see {@link GeneralTimePeriod})
     * @param series the time series to store
     * @param replace if true existing entries will be overwritten
     * @throws SQLException SQLException
     */
    void insertData(TaTimeSeries series, boolean replace) throws SQLException;

    /**
     * Returns a time series object that stores the available data of the symbol,currency,period combination
     * @param symbol the symbol
     * @param currency the currency
     * @param table the {@link GeneralTimePeriod table/period}, there should be one table for each period
     * @param from date from
     * @param to date to
     * @return11
     * @throws SQLException
     */
    TaTimeSeries getSeries(String symbol, Currency currency, GeneralTimePeriod table, ZonedDateTime from, ZonedDateTime to) throws SQLException;
}
