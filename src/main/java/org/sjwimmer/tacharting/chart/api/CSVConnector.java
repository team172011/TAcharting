package org.sjwimmer.tacharting.chart.api;

import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.sjwimmer.tacharting.chart.api.settings.CsvSettingsManager;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.parameters.TimeFormatType;
import org.sjwimmer.tacharting.chart.utils.FormatUtils;
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

    public CSVConnector(){
        properties = CsvSettingsManager.getProperties();
    }

    @Override
    public TimeSeries getSeries(File file) throws IOException{
        CSVReader reader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(new CSVParser()).build();
        String line[];
        line = reader.readNext();
        String name = line[0];
        int id = FormatUtils.extractInteger(line[1]);
        boolean isDateTwoColumn = id == TimeFormatType.yyyy_MM_ddHmsz.id;
        DateTimeFormatter dateTimeFormatter = FormatUtils.getDateTimeFormatter(id);

        line = reader.readNext();
        Map<Parameter.Columns, Integer> headers = FormatUtils.getHeaderMap(Arrays.asList(line));

        List<Tick> ticks = new ArrayList<>();
        while((line = reader.readNext()) != null) {
            ticks.add(FormatUtils.extractOHLCData(headers,dateTimeFormatter,line,isDateTwoColumn));
        }
        if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
            Collections.reverse(ticks);
        }
       return new BaseTimeSeries(name==null?"unnamed":name,ticks);
    }

    /**
     * Reads file with yahoo api structure. No first line with name and timeFormatId, just header line and
     * {@link TimeFormatType timeFormat YAHOO}
     * @param name the name to display for this symbol
     * @param file the csv file with financial data in yahoo format
     * @return the corresponding TimeSeries object
     * @throws IOException IOException
     */
    public TimeSeries getSeriesFromYahooFile(String name, File file) throws IOException{
        CSVReader reader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(new CSVParser()).build();
        String line[];
        line = reader.readNext();
        Map<Parameter.Columns, Integer> headers = FormatUtils.getHeaderMap(Arrays.asList(line));
        List<Tick> ticks = new ArrayList<>();
        while((line = reader.readNext()) != null) {
            ticks.add(FormatUtils.extractOHLCData(headers, DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern),line,false));
        }
        if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
            Collections.reverse(ticks);
        }
        return new BaseTimeSeries(name==null?"unnamed":name,ticks);
    }
}
