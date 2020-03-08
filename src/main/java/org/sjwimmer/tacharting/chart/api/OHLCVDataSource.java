package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.model.TaBarSeries;
import org.sjwimmer.tacharting.chart.model.key.Key;

import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.List;

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
	
    TaBarSeries getSymbolData(K symbol, ZonedDateTime from, ZonedDateTime to) throws Exception;
    
    TaBarSeries getSymbolData(K symbol) throws Exception;
    
    List<TaBarSeries> getSymbolData(List<K> symbols, ZonedDateTime from, ZonedDateTime to) throws Exception;
    
    boolean connect(R ressource);
    
    void disconnect();
    
    boolean isReady();
}
