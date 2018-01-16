package chart.utils;

import chart.parameters.Parameter;
import org.ta4j.core.BaseTick;
import org.ta4j.core.Tick;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class FormatUtils {

    /**
     * Tries to find the needed columns from a header row
     * @param firstLine line with the header descriptions (e.g.: time, open, high, low, close, volume...)
     * @return a Map with index and the corresponding column description
     */
    public static <T extends Iterable<String>> Map<Parameter.Columns, Integer> getHeaderMap(T firstLine) {

        Map<Parameter.Columns, Integer> headers = new HashMap<>();
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
     * @param timeFormat the {@link chart.parameters.Parameter.TimeFormat time ofFormat}
     * @param line the string array with corresponding entries for the tick
     * @return a {@link Tick tick} object with the ohlc data
     */
    public static Tick extractOHLCData(Map<Parameter.Columns, Integer> headerMap, Parameter.TimeFormat timeFormat, String[] line){
        ZonedDateTime date;
        if(timeFormat == Parameter.TimeFormat.y_M_d_hmsZ){
            date = timeFormat.ofFormat(line[headerMap.get(Parameter.Columns.DATE)]
                    +" "+line[headerMap.get(Parameter.Columns.DATE2)]+" PST"); // TODO better handling colorOf special case with two columns based date
        } else {
            date = timeFormat.ofFormat(line[headerMap.get(Parameter.Columns.DATE)]);
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

    /**
     *
     * @param color color as String or RGB value as String
     * @return corresponding awt color
     */
    public static Color colorOf(String color) {
        String[] rgb = color.split(",");
        if (rgb.length == 3) {
            return new Color(Float.parseFloat(rgb[0]), Float.parseFloat(rgb[1]),Float.parseFloat(rgb[2]));
        } else if (rgb.length == 4) {
            return new Color(Float.parseFloat(rgb[0]),Float.parseFloat(rgb[1]), Float.parseFloat(rgb[2]), Float.parseFloat(rgb[3])/255);
        }
        return Color.BLUE; // default
    }

}
