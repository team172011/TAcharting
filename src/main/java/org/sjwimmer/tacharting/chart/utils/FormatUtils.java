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
package org.sjwimmer.tacharting.chart.utils;

import javafx.util.StringConverter;
import org.sjwimmer.tacharting.chart.parameters.*;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Tick;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.sjwimmer.tacharting.chart.parameters.IndicatorParameterType.*;

/**
 * Class for several transformations.<p/>
 * In the most cases this class will be needed to transform a String value (from property,
 * database or another file, to the corresponding java object. Mapping between String value and java object is defined
 * by enums (see {@link ChartType}, {@link ShapeType}, {@link StrokeType}, {@link TimeFormatType}), or static variables
 * (see {@link Parameter}
 * This class stores a bunch of static functions and static {@link StringConverter StringConverters} to allow theses
 * transformations.
 */
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
     * Extracts the OHLC data from a string array into a tick object
     * @param headerMap the header maps that maps indices colorOf the <tt>line</tt> to the {@link Parameter.Columns columns}
     * @param formatPattern the {@link org.sjwimmer.tacharting.chart.parameters.Parameter.TimeFormat time ofFormat}
     * @param line the string array with corresponding entries for the tick
     * @return a {@link Tick tick} object with the ohlc data
     */
    public static Tick extractOHLCData(Map<Parameter.Columns, Integer> headerMap, DateTimeFormatter formatPattern, String[] line,boolean twoDateColumns){
        ZonedDateTime date;
        if(twoDateColumns){
            date = ZonedDateTime.parse(line[headerMap.get(Parameter.Columns.DATE)]
                    +" "+line[headerMap.get(Parameter.Columns.DATE2)]+" PST", formatPattern);
        } else {
            //TODO: workaround...
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
        return new BaseTick(date, open, high, low, close, volume);
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

    public static StringConverter<javafx.scene.paint.Color> ColorFxConverter = new StringConverter<javafx.scene.paint.Color>() {
        @Override
        public String toString(javafx.scene.paint.Color color) {
            return String.format("%s,%s,%s,%s",color.getRed(),color.getGreen(),color.getBlue(),color.getBrightness());
        }

        @Override
        public javafx.scene.paint.Color fromString(String color) {
            String[] rgb = color.split(",");
            if (rgb.length == 3) {
                return javafx.scene.paint.Color.rgb((int)Float.parseFloat(rgb[0])*255, (int)Float.parseFloat(rgb[1])*255,(int)Float.parseFloat(rgb[2])*255);
            } else if (rgb.length == 4) {
                return javafx.scene.paint.Color.rgb((int)Float.parseFloat(rgb[0])*255,(int)Float.parseFloat(rgb[1])*255, (int)Float.parseFloat(rgb[2])*255, Float.parseFloat(rgb[3]));
            }
            return javafx.scene.paint.Color.rgb(0,0,255,1); // default
        }
    };

    public static StringConverter<Color> ColorAWTConverter = new StringConverter<Color>() {

        @Override
        public String toString(Color color) {
            return String.format("%s,%s,%s,%s",(float)color.getRed()/255,(float)color.getGreen()/255,(float)color.getBlue()/255,(float)color.getAlpha()/255);
        }

        @Override
        public Color fromString(String color) {
            try {
                String[] rgb = color.split(",");
                if (rgb.length == 3) {
                    return new Color(Float.parseFloat(rgb[0]), Float.parseFloat(rgb[1]), Float.parseFloat(rgb[2]));
                } else if (rgb.length == 4) {
                    return new Color(Float.parseFloat(rgb[0]), Float.parseFloat(rgb[1]), Float.parseFloat(rgb[2]), Float.parseFloat(rgb[3]));
                }
            } catch (IllegalArgumentException ille){
                return new Color(0.0f, 0.0f,0.1f, 0.1f); // default
            }
            return new Color(0.0f, 0.0f,0.1f, 0.1f); // default
        }
    };

    /** Converter ****************************************************************************************************/

    public static StringConverter<ShapeType> ShapeTypeConverter = new StringConverter<ShapeType>() {
        @Override
        public String toString(ShapeType object) {
            return object.toString();
        }

        @Override
        public ShapeType fromString(String string) {
            string = string.toUpperCase().replace("\\s","");
            return ShapeType.valueOf(string);
        }
    };

    public static StringConverter<StrokeType> StrokeTypeConverter = new StringConverter<StrokeType>() {
        @Override
        public String toString(StrokeType object) {
            if(object==null){
                return null; // Don't know why object is null at start /TODO
            }
            return object.toString();
        }

        @Override
        public StrokeType fromString(String string) {
            string = string.toUpperCase().replace("\\s","");
            return StrokeType.valueOf(string);
        }
    };

    public static StringConverter<Boolean> BooleanypeConverter = new StringConverter<Boolean>() {
        @Override
        public String toString(Boolean object) {
            return object.toString();
        }

        @Override
        public Boolean fromString(String string) {
            return Boolean.valueOf(string);
        }
    };

    public static StringConverter<ChartType> ChartTypeConverter = new StringConverter<ChartType>() {
        @Override
        public String toString(ChartType object) {
            return object.toString();
        }

        @Override
        public ChartType fromString(String string) {
            return ChartType.valueOf(string);
        }
    };

}
