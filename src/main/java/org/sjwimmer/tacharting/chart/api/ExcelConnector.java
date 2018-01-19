package org.sjwimmer.tacharting.chart.api;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sjwimmer.tacharting.chart.TaTimeSeries;
import org.sjwimmer.tacharting.chart.parameters.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.parameters.TimeFormatType;
import org.sjwimmer.tacharting.chart.utils.FormatUtils;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Connector class for reading financial data from excel files
 */
public class ExcelConnector implements Connector<File> {

    @Override
    public TaTimeSeries getSeries(File resource) throws IOException {
        FileInputStream inputStream = new FileInputStream(resource);
        Workbook wb = new XSSFWorkbook(inputStream);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        // first row with name and time ofFormat
        Row infoRow = rowIterator.next();
        String name = infoRow.getCell(0).getStringCellValue();
        String timeFormat = infoRow.getCell(1).getStringCellValue();
        int id =FormatUtils.extractInteger(timeFormat);
        boolean isTwoColumnDate = id== TimeFormatType.yyyy_MM_ddHmsz.id;
        DateTimeFormatter dateFormatter = FormatUtils.getDateTimeFormatter(id);
        String currencyString = infoRow.getCell(2).getStringCellValue().replaceAll("\\s","").toUpperCase();
        if(currencyString==null){
            currencyString = Parameter.DEFAULT_CURRENCY;
        }
        Currency currency = Currency.getInstance(currencyString);

        // second row with header description
        infoRow = rowIterator.next();
        Iterator<Cell> cellIterator = infoRow.cellIterator();
        ArrayList<String> headerLine = new ArrayList<>();
        while (cellIterator.hasNext()){
            Cell cell = cellIterator.next();
            headerLine.add(cell.getStringCellValue());
        }

        Map<Parameter.Columns, Integer> headerMap = FormatUtils.getHeaderMap(headerLine);
        List<Tick> ticks = new ArrayList<>();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            cellIterator = row.cellIterator();
            ArrayList<String> list = new ArrayList<>();
            while(cellIterator.hasNext()){
                list.add(cellIterator.next().getStringCellValue());
            }
            Tick tick = FormatUtils.extractOHLCData(headerMap,dateFormatter,list.toArray(new String[list.size()]),isTwoColumnDate);
            ticks.add(tick);
        }
        if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
            Collections.reverse(ticks);
        }
        TimeSeries series = new BaseTimeSeries(name==null?"unnamed":name,ticks);
        GeneralTimePeriod period = FormatUtils.extractPeriod(series);
        return new TaTimeSeries(series,currency,period);
    }
}
