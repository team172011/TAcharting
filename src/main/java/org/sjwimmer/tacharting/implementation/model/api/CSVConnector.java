package org.sjwimmer.tacharting.implementation.model.api;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.sjwimmer.tacharting.chart.api.OHLCVDataSource;
import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.key.Key;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.model.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.model.types.YahooTimePeriod;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.implementation.model.api.key.CSVKey;
import org.sjwimmer.tacharting.implementation.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Connector class to read financial data from CSV file
 * @apiNote the csv file must have a special structure that can be
 * customized in the {@link CsvSettingsManager}
 */
public class CSVConnector implements OHLCVDataSource<CSVKey, File> {

    private final Properties properties;
    private final Logger log = LoggerFactory.getLogger(CSVConnector.class);
    
    private List<String[]> lines = new ArrayList<>();
    private String name;
    private int id;
    private Currency currency;
    private DateTimeFormatter dateTimeFormatter;
    boolean isDateTwoColumn;

    public CSVConnector(){
        properties = CsvSettingsManager.getProperties();
    }

    @Override
    public boolean connect(File resource){
        String separator = properties.getProperty(Parameter.PROPERTY_CSV_SEPARATOR, ",");
        String quote = properties.getProperty(Parameter.PROPERTY_CSV_QUOTE, "\\\\");
        CSVParser parser = new CSVParserBuilder().withSeparator(separator.charAt(0)).withQuoteChar(quote.charAt(0)).build();
        try(CSVReader reader = new CSVReaderBuilder(new FileReader(resource)).withCSVParser(parser).build();)
        {
        	lines = reader.readAll();
        	String[] infoLine = lines.get(0);
			name = infoLine[0];
	        id = FormatUtils.extractInteger(infoLine[1]);
	        isDateTwoColumn = id == TimeFormatType.yyyy_MM_ddHmsz.id;
	        dateTimeFormatter = FormatUtils.getDateTimeFormatter(id);
	        String currencyString = null;
	        if(infoLine.length>2) {
	            currencyString = infoLine[2].replaceAll("\\s", "");
	        }
	        if(currencyString == null || currencyString.length() != 3)
	            currencyString = Parameter.DEFAULT_CURRENCY;
	        currency = Currency.getInstance(currencyString);
	        lines.remove(0); // remove InfoLine
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IOException ioe) {
			log.error(ioe.getMessage());
			ioe.printStackTrace();
			return false;
		}
        return true;
    }

    /**
     * Reads a csv file with structure of yahoo api: No info line with name and timeFormatId, just header line and
     * {@link TimeFormatType timeFormat YAHOO}
     * @param name the name of this symbol
     * @param file the csv file with financial data in yahoo format
     * @return the corresponding TimeSeries object
     * @throws IOException IOException
     */
    public TaTimeSeries getSeriesFromYahooFile(String name, File file) throws IOException{
        CSVReader reader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(new CSVParser()).build();
        String line[];
        line = reader.readNext();
        Map<Parameter.Columns, Integer> headers = FormatUtils.getHeaderMap(Arrays.asList(line));
        List<Bar> Bars = new ArrayList<>();
        while((line = reader.readNext()) != null) {
            Bars.add(FormatUtils.extractOHLCData(
                    headers, DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern),line,false));
        }
        reader.close();
        if(Bars.get(Bars.size()-1).getEndTime().isBefore(Bars.get(0).getEndTime())){
            Collections.reverse(Bars);
        }
        String yahooIntervall = YahooSettingsManager.getProperties().getProperty(Parameter.PROPERTY_YAHOO_INTERVAL);
        GeneralTimePeriod timePeriod = YahooTimePeriod.of(yahooIntervall).generalTimePeriod;
        return new TaTimeSeries(name==null?"unnamed":name.toUpperCase(),Bars,Currency.getInstance("USD"),timePeriod);
    }

	@Override
	public List<String> getAllAvailableSymbols() throws Exception {
		return Arrays.asList(name);
	}

	@Override
	public TaTimeSeries getSymbolData(CSVKey symbol, ZonedDateTime from, ZonedDateTime to) throws Exception {
		List<Bar> bars = new ArrayList<>();
		Map<Parameter.Columns, Integer> headers = FormatUtils.getHeaderMap(Arrays.asList(lines.get(0)));
		lines.remove(0); // remove line with header columns
		for(String[] line: lines) {
			Bar bar = FormatUtils.extractOHLCData(headers, dateTimeFormatter,line, isDateTwoColumn);
			if(!bar.getEndTime().isAfter(to) && !bar.getEndTime().isBefore(from)) {
				bars.add(bar);
			}
		}
        if(bars.get(bars.size()-1).getEndTime().isBefore(bars.get(0).getEndTime())){
            Collections.reverse(bars);
        }
	    TimeSeries series = new BaseTimeSeries(name==null?"unnamed":name.toUpperCase(), bars);
        GeneralTimePeriod period =  FormatUtils.extractPeriod(series);
        log.debug("Extracted period: "+period);
	    return new TaTimeSeries(series, currency, period);
	}

	@Override
	public TaTimeSeries getSymbolData(CSVKey symbol) throws Exception {
		return getSymbolData(symbol, ZonedDateTime.now().plusYears(1000), ZonedDateTime.now().minusYears(1000));
	}

	@Override
	public List<TaTimeSeries> getSymbolData(List<CSVKey> symbols, ZonedDateTime from, ZonedDateTime to) throws Exception {
		List<TaTimeSeries> series = new ArrayList<>();
		for(CSVKey key: symbols) {
			series.add(getSymbolData(key, ZonedDateTime.now().plusYears(1000), ZonedDateTime.now().minusYears(1000)));
		}
		return series;
	}


	@Override
	public void disconnect() {
		lines = new ArrayList<>();
		name = null;
		currency = null;
		
	}

	@Override
	public boolean isReady() {
		return name != null;
	}

}
