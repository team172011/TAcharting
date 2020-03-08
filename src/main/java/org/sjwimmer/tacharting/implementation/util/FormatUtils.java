/*
 GNU Lesser General Public License

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.sjwimmer.tacharting.implementation.util;

import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.model.types.IndicatorParameterType;
import org.sjwimmer.tacharting.chart.model.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BarSeries;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.sjwimmer.tacharting.chart.model.types.IndicatorParameterType.*;


public class FormatUtils {

    /**
     * Tries to find the needed columns from a header row
     * @param firstLine line with the header descriptions (e.g.: time, open, high, low, close, volume...)
     * @return a Map with index and the corresponding column description
     */
    public static <T extends Iterable<String>> Map<Parameter.Columns, Integer> getHeaderMap(T firstLine) {

        final Map<Parameter.Columns, Integer> headers = new HashMap<>();
        int i = 0;
        for(String col: firstLine){
            col = col.toLowerCase().replaceAll("\\s+","");
            if(Parameter.HEADER_DATE.contains(col)){
                headers.put(Parameter.Columns.DATE, i);
            }
            else if(Parameter.HEADER_OPEN.contains(col)) {
                headers.put(Parameter.Columns.OPEN, i);
            }
            else if(Parameter.HEADER_HIGH.contains(col)){
                headers.put(Parameter.Columns.HIGH, i);
            }
            else if(Parameter.HEADER_LOW.contains(col)){
                headers.put(Parameter.Columns.LOW, i);
            }
            else if(Parameter.HEADER_CLOSE.contains(col)){
                headers.put(Parameter.Columns.CLOSE, i);
            }
            else if(Parameter.HEADER_Volume.contains(col)){
                headers.put(Parameter.Columns.VOLUME, i);
            }
            else if(Parameter.HEADER_DATE2.contains(col)){
                headers.put(Parameter.Columns.DATE2,i);
            }
            i++;
        }
        return headers;
    }

    /**
     * Extracts the OHLC org.sjwimmer.tacharting.data from a string array into a Bar object
     * @param headerMap the header maps that maps indices colorOf the <tt>line</tt> to the {@link Parameter.Columns columns}
     * @param formatPattern the {@link DateTimeFormatter dateTimeFormatter}
     * @param line the string array with corresponding entries for the Bar
     * @return a {@link Bar Bar} object with the ohlc org.sjwimmer.tacharting.data
     */
    public static Bar extractOHLCData(Map<Parameter.Columns, Integer> headerMap, DateTimeFormatter formatPattern, String[] line,boolean twoDateColumns){
        ZonedDateTime date;
        if(twoDateColumns){
            date = ZonedDateTime.parse(line[headerMap.get(Parameter.Columns.DATE)]
                    +" "+line[headerMap.get(Parameter.Columns.DATE2)]+" PST", formatPattern);
        } else {
            //TODO: its a workaround, because some formats do not allow directly convert to ZonedDateTime because of missing ZoneId...
            LocalDate localDate = LocalDate.parse(line[headerMap.get(Parameter.Columns.DATE)],formatPattern);
            ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
            date = zonedDateTime;
        }
        double open = Double.parseDouble(line[headerMap.get(Parameter.Columns.OPEN)]);
        double high = Double.parseDouble(line[headerMap.get(Parameter.Columns.HIGH)]);
        double low = Double.parseDouble(line[headerMap.get(Parameter.Columns.LOW)]);
        double close = Double.parseDouble(line[headerMap.get(Parameter.Columns.CLOSE)]);
        double volume = Double.NaN;
        if(headerMap.get(Parameter.Columns.VOLUME) != null){
            volume = Double.parseDouble(line[headerMap.get(Parameter.Columns.VOLUME)]);
        }
        return new BaseBar(Duration.ZERO, date, open, high, low, close, volume, 0, 0, Parameter.numFunction);
    }

    public static DateTimeFormatter getDateTimeFormatter(int id){
        for(TimeFormatType timeFormatType: TimeFormatType.values()){
            if(timeFormatType.id == id){
                return DateTimeFormatter.ofPattern(timeFormatType.pattern, Parameter.DEFAULT_LOCALE);
            }
        }
        return DateTimeFormatter.ofPattern(Parameter.DEFAULT_PATTERN, Parameter.DEFAULT_LOCALE);
    }

    public static int extractInteger(String string){
        String clean = string.replaceAll("\\s","");
        return Integer.parseInt(clean);
    }

    public static double extractDouble(String string){
        String clean = string.replaceAll("\\s","");
        return Double.parseDouble(clean);
    }

    /**
     * Run over the Bars of a time series and return their {@link GeneralTimePeriod}
     * @param series the time series
     * @return the underlying {@link GeneralTimePeriod} (minimum gap between end time of two consecutive Bars of the
     * complete time series or the minimum gap of at least 20 percent of consecutive Bars of the <tt>series</tt>
     */
    public static GeneralTimePeriod extractPeriod(BarSeries series){
        long minDiff = Long.MAX_VALUE;
        int counter=0;
        double threshold = series.getBarCount()*0.2;
        // get the index i and i+1 of the Bars with min diff
        // stop if 20% of the series have the same minDiff
        for(int i=0;i<series.getBarCount()-1;i++){
            Bar Bar = series.getBar(i);
            Bar next = series.getBar(i+1);
            long diff =  Duration.between(Bar.getEndTime(),next.getEndTime()).toMinutes();
            if(diff < minDiff){
                minDiff = diff;
            } else if(minDiff == diff){
                if(counter++>threshold){
                    System.out.println("threshold reached");
                    break;
                }
            }
        }
        if(minDiff<1){
            return GeneralTimePeriod.REALTIME;
        } else if(minDiff<5) {
            return GeneralTimePeriod.MINUTE;
        } else if(minDiff<60){
            return GeneralTimePeriod.FIVE_MINUTE;
        } else if(minDiff < 60*24) {
            return GeneralTimePeriod.HOUR;
        } else if(minDiff < 60*24*5){
            return GeneralTimePeriod.DAY;
        } else if(minDiff < 60*24*19){
            return GeneralTimePeriod.FIVE_DAY;
        } else if(minDiff < 60*24*19*3){
            return GeneralTimePeriod.MONTH;
        } else if(minDiff < 60*24*19*12){
            return GeneralTimePeriod.QUARTER;
        } else if(minDiff < 60*24*19*12 *2 ){
            return GeneralTimePeriod.YEAR;
        }
        throw new IllegalArgumentException(
                String.format("TimePeriod of series %s could not be extracted minDiff (seconds): %s",series.getName(),minDiff));
    }


    /**
     * Returns a javaFX Color object that represents the same color as the awt color object
     * @param c a java.awt.Color object
     */
    public static javafx.scene.paint.Color awtColorToJavaFX(Color c) {
        return javafx.scene.paint.Color.rgb(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 255.0);
    }

    public static Color javaFXColorToAwt(javafx.scene.paint.Color c){
        return new Color((float) c.getRed(),(float)c.getGreen(), (float)c.getBlue(), (float) c.getOpacity());
    }



    public static IndicatorParameterType indicatorParameterTypeOf(String val) {
        final String typeToSwitch = val.toUpperCase().replaceAll("\\s+", "");
        switch (typeToSwitch) {
            case "COLOR": {
                return COLOR;
            }
            case "STROKE": {
                return STROKE;
            }
            case "SHAPE": {
                return SHAPE;
            }
            case "CHARTTYPE": {
                return CHARTTYPE;
            }
            case "INTEGER": {
                return INTEGER;
            }
            case "DOUBLE": {
                return DOUBLE;
            }
            case "BOOLEAN": {
                return BOOLEAN;
            }
            default: {
                return STRING;
            }
        }
    }

}
