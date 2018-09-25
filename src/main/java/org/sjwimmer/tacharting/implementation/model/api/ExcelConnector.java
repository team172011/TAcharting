package org.sjwimmer.tacharting.implementation.model.api;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sjwimmer.tacharting.chart.api.OHLCVDataSource;
import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.model.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.implementation.model.api.key.ExcelKey;
import org.sjwimmer.tacharting.implementation.util.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Connector class for reading financial data from excel files
 */
public class ExcelConnector implements OHLCVDataSource<ExcelKey, File> {

	private final static Logger log = LoggerFactory.getLogger(ExcelConnector.class);
	Iterator<Row> rowIterator;
    private String name;
    private int id;
    private Currency currency;
    private DateTimeFormatter dateTimeFormatter;
    boolean isDateTwoColumn;
    
    @Override
    public boolean connect(File resource) {
        
		try 
		(	FileInputStream inputStream = new FileInputStream(resource);
			Workbook wb = new XSSFWorkbook(inputStream);
		){
	        Sheet sheet = wb.getSheetAt(0);
	        sheet.rowIterator();
	        // first row with name and time ofFormat
	        Row infoRow = rowIterator.next();
	        name = infoRow.getCell(0).getStringCellValue();
	        String timeFormat = infoRow.getCell(1).getStringCellValue();
	        id =FormatUtils.extractInteger(timeFormat);
	        isDateTwoColumn = id== TimeFormatType.yyyy_MM_ddHmsz.id;
	        dateTimeFormatter = FormatUtils.getDateTimeFormatter(id);
	        String currencyString = infoRow.getCell(2).getStringCellValue().replaceAll("\\s","").toUpperCase();
	        if(currencyString==null){
	            currencyString = Parameter.DEFAULT_CURRENCY;
	        }
		        currency = Currency.getInstance(currencyString);
			} catch (IOException e) {
				log.error("Error connecting with .xls file: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
			return true;
    }

	@Override
	public List<String> getAllAvailableSymbols() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TaTimeSeries getSymbolData(ExcelKey symbol, ZonedDateTime from, ZonedDateTime to) throws Exception {
		 // second row with header description
        Row infoRow = rowIterator.next();
        Iterator<Cell> cellIterator = infoRow.cellIterator();
        ArrayList<String> headerLine = new ArrayList<>();
        while (cellIterator.hasNext()){
            Cell cell = cellIterator.next();
            headerLine.add(cell.getStringCellValue());
        }

        Map<Parameter.Columns, Integer> headerMap = FormatUtils.getHeaderMap(headerLine);
        List<Bar> ticks = new ArrayList<>();
        while (rowIterator.hasNext()){
            Row row = rowIterator.next();
            cellIterator = row.cellIterator();
            ArrayList<String> list = new ArrayList<>();
            while(cellIterator.hasNext()){
                list.add(cellIterator.next().getStringCellValue());
            }
            
            Bar tick = FormatUtils.extractOHLCData(headerMap,dateTimeFormatter,list.toArray(new String[list.size()]),isDateTwoColumn);
            ticks.add(tick);
        }
        if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
            Collections.reverse(ticks);
        }
        TimeSeries series = new BaseTimeSeries(name==null?"unnamed":name,ticks);
        GeneralTimePeriod period = FormatUtils.extractPeriod(series);
        return new TaTimeSeries(series,currency,period);
	}

	@Override
	public TaTimeSeries getSymbolData(ExcelKey symbol) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TaTimeSeries> getSymbolData(List<ExcelKey> symbols, ZonedDateTime from, ZonedDateTime to)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}
}
