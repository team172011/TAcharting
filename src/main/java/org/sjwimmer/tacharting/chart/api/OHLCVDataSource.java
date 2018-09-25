package org.sjwimmer.tacharting.chart.api;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.key.Key;
import org.ta4j.core.TimeSeries;

/**
 * Api for a datasource of ohlc data with volume. This could be a 
 * database connection or a connection to another broker (like Interavtive Brokers or IEX)
 * or simple HTML-Get connection.
 *
 */
public interface OHLCVDataSource<K extends Key, R> {

	/**
	 * 
	 * @return a list with all available symbols of this data source
	 * @throws SQLException
	 */
	List<String> getAllAvailableSymbols() throws Exception;
	
    TaTimeSeries getSymbolData(K symbol, ZonedDateTime from, ZonedDateTime to) throws Exception;
    
    TaTimeSeries getSymbolData(K symbol) throws Exception;
    
    List<TaTimeSeries> getSymbolData(List<K> symbols, ZonedDateTime from, ZonedDateTime to) throws Exception;
    
    boolean connect(R ressource);
    
    void disconnect();
    
    boolean isReady();
}
