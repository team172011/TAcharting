package org.sjwimmer.tacharting.chart.api;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.sjwimmer.tacharting.chart.TaTimeSeries;
import org.sjwimmer.tacharting.chart.api.settings.CsvSettingsManager;
import org.sjwimmer.tacharting.chart.api.settings.YahooSettingsManager;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.types.YahooTimePeriod;
import org.sjwimmer.tacharting.chart.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CSVConnector implements Connector<File> {

    private final Properties properties;
    private final Logger log = LoggerFactory.getLogger(CSVConnector.class);

    public CSVConnector(){
        properties = CsvSettingsManager.getProperties();
    }

    @Override
    public TaTimeSeries getSeries(File resource) throws IOException{
        String separator = properties.getProperty(Parameter.PROPERTY_CSV_SEPARATOR, ",");
        String quote = properties.getProperty(Parameter.PROPERTY_CSV_QUOTE, "\\\\");
        CSVParser parser = new CSVParserBuilder().withSeparator(separator.charAt(0)).withQuoteChar(quote.charAt(0)).build();
        CSVReader reader = new CSVReaderBuilder(new FileReader(resource)).withCSVParser(parser).build();
        String line[];
        line = reader.readNext();
        String name = line[0];
        int id = FormatUtils.extractInteger(line[1]);
        boolean isDateTwoColumn = id == TimeFormatType.yyyy_MM_ddHmsz.id;
        DateTimeFormatter dateTimeFormatter = FormatUtils.getDateTimeFormatter(id);

        String currencyString = null;
        if(line.length>2) {
            currencyString = line[2].replaceAll("\\s", "");
        }
        if(currencyString == null || currencyString.length() != 3)
            currencyString = Parameter.DEFAULT_CURRENCY;
        Currency currency = Currency.getInstance(currencyString);


        line = reader.readNext();
        Map<Parameter.Columns, Integer> headers = FormatUtils.getHeaderMap(Arrays.asList(line));

        List<Tick> ticks = new ArrayList<>();
        while((line = reader.readNext()) != null) {
            ticks.add(FormatUtils.extractOHLCData(headers,dateTimeFormatter,line,isDateTwoColumn));
        }
        if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
            Collections.reverse(ticks);
        }
        //TODO: remove daily
        TimeSeries series = new BaseTimeSeries(name==null?"unnamed":name.toUpperCase(),ticks);
        GeneralTimePeriod period =  FormatUtils.extractPeriod(series);
        log.info("Extracted period: "+period);
       return new TaTimeSeries(series,currency,period);
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
        List<Tick> ticks = new ArrayList<>();
        while((line = reader.readNext()) != null) {
            ticks.add(FormatUtils.extractOHLCData(
                    headers, DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern),line,false));
        }
        if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
            Collections.reverse(ticks);
        }
        String yahooIntervall = YahooSettingsManager.getProperties().getProperty(Parameter.PROPERTY_YAHOO_INTERVAL);
        GeneralTimePeriod timePeriod = YahooTimePeriod.of(yahooIntervall).generalTimePeriod;
        return new TaTimeSeries(name==null?"unnamed":name.toUpperCase(),ticks,Currency.getInstance("USD"),timePeriod);
    }

}
