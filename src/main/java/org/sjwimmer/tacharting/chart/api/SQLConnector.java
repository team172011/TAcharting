package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.TaTimeSeries;
import org.sjwimmer.tacharting.chart.parameters.GeneralTimePeriod;
import org.ta4j.core.TimeSeries;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;

/**
 * Interface for connections to a database to store and receive financial data
 */
public interface SQLConnector {

    /**
     * Returns a list of all available symbol of a specific time period
     * @param table the table/time period
     * @return a list of all available symbols of this time period
     * @throws SQLException sqlexception
     */
    List<String> getSymbolList(GeneralTimePeriod table) throws SQLException;

    /**
     * Removes all entries which had the key of the <tt>series</tt> key consists of {@link TaTimeSeries#getName()},
     * {@link Currency#getCurrencyCode()}, {@link GeneralTimePeriod}
     * @param series the series with the key (could be empty)
     * @throws SQLException sqlexception
     */
    void removeData(TimeSeries series) throws SQLException;

    /**
     * Inserts the data of the <tt>series</tt> into the corresponding table (see {@link GeneralTimePeriod})
     * @param series
     * @param replace
     * @throws SQLException
     */
    void insertData(TaTimeSeries series, boolean replace) throws SQLException;

    TimeSeries getTimeSeries(String symbol, Currency currency, GeneralTimePeriod table, ZonedDateTime from, ZonedDateTime to) throws SQLException;
}
