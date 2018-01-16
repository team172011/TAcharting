package org.sjwimmer.tacharting.chart.api;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.utils.FormatUtils;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExcelConnector implements Connector<File> {


    @Override
    public TimeSeries getSeries(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        Workbook wb = new XSSFWorkbook(inputStream);
        Sheet sheet = wb.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();

        // first row with name and time ofFormat
        Row infoRow = rowIterator.next();
        String name = infoRow.getCell(0).getStringCellValue();
        String timeFormat = infoRow.getCell(1).getStringCellValue();
        int id = Integer.parseInt(timeFormat);
        boolean isTwoColumnDate = id== Parameter.TimeFormat.y_M_d_hmsZ.id;
        DateTimeFormatter dateFormatter = FormatUtils.getDateTimeFormatter(id);


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
        return new BaseTimeSeries(name==null?"unnamed":name,ticks);
    }
}
