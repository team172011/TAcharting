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
package org.sjwimmer.tacharting.chart.parameters;

import javafx.stage.FileChooser;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

public  class Parameter {


    /** Header names of financial data ********************************************************************************/
    public final static HashSet<String> HEADER_DATE = new HashSet<>(Arrays.asList("date", "timestamp"));
    public final static HashSet<String> HEADER_OPEN = new HashSet<>(Arrays.asList("open", "first", "open price", "first price"));
    public final static HashSet<String> HEADER_HIGH = new HashSet<>(Arrays.asList("high", "max", "high price", "max price"));
    public final static HashSet<String> HEADER_LOW = new HashSet<>(Arrays.asList("low", "min", "low price", "min price"));
    public final static HashSet<String> HEADER_CLOSE = new HashSet<>(Arrays.asList("close", "last", "last price", "close price", "price"));
    public final static HashSet<String> HEADER_Volume = new HashSet<>(Arrays.asList("volume", "vol"));
    public final static HashSet<String> HEADER_DATE2 = new HashSet<>(Arrays.asList("time"));

    public enum Columns{
        DATE,
        DATE2,
        OPEN,
        HIGH,
        LOW,
        CLOSE,
        VOLUME
    }

    /** Files *********************************************************************************************/
    public final static String INDICATOR_PROPERTIES_FILE = "indicatorParameters.xml";
    public final static String API_PROPERTIES_FILE = "properties/api.properties";

    public final static FileChooser.ExtensionFilter EXTENSION_FILTER_CSV = new  FileChooser.ExtensionFilter("CSV","*.csv", "*.CSV");
    public final static FileChooser.ExtensionFilter EXTENSION_FILTER_EXCEL = new FileChooser.ExtensionFilter("Execl","*.xls", "*.xlsx");

    /** Property names *********************************************************************************************/
    public static final String PROPERTY_YAHOO_FROM = "yahoo_from";
    public static final String PROPERTY_YAHOO_TO = "yahoo_to";
    public static final String PROPERTY_YAHOO_INTERVAL = "yahoo_interval";

    public static final String PROPERTY_CSV_SEPERATOR = "csv_seperator";
    public static final String PROPERTY_CSV_ENDLINE = "csv_endline";


    /** Menu entries *********************************************************************************************/
    public enum IndicatorCategory {
        CUSTOM(8),
        STRATEGY(9),
        DEFAULT(0),
        BOLLINGER(1),
        CANDLES(2),
        HELPERS(3),
        ICHIMOKU(4),
        KELTNER(5),
        STATISTICS(6),
        VOLUME(7);

        private int id;

        IndicatorCategory(int id) {

            this.id = id;
        }

        public int getId() {
            return id;
        }
    }


    /***********************************************************************************************/
    // Available api provider
        //TODO:Add more..
    public enum ApiProvider{
        Yahoo,
        AlphaVantage
    }

    /***********************************************************************************************/
    // Awkward time formats
            //TODO implement settings file for this
    public final static DateTimeFormatter FORMATTER_y_M_d_hmsZ = DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s z");
    public final static DateTimeFormatter FORMATTER_yyyyMMd = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public final static DateTimeFormatter FORMATTER_yyy_MM_dd = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // yahoo csv
    public final static DateTimeFormatter FORMATTER_dd_MMM_YYYY = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US); // eodata csv
    public static Locale DEFAULT_LOCALE = Locale.US;
    public static String DEFAULT_PATTERN = TimeFormatType.yyyyMMdd.pattern;

    public enum TimeFormat{
        y_M_d_hmsZ(0),
        yyyyMMdd(1),
        yahoo(3),
        TIMESTAMP(2),
        dd_MMM_YYYY(4);

        public final int id;

        TimeFormat(int i){
            this.id =i;
        }

        public ZonedDateTime ofFormat(String date){
            switch (this){
                case yyyyMMdd:
                    return LocalDate.parse(date, FORMATTER_yyyyMMd).atStartOfDay(ZoneId.systemDefault());
                case y_M_d_hmsZ:
                    return ZonedDateTime.parse(date,FORMATTER_y_M_d_hmsZ);
                case yahoo:
                    LocalDate dateTime = LocalDate.parse(date,FORMATTER_yyy_MM_dd);
                    return dateTime.atStartOfDay(ZoneId.systemDefault());
                case dd_MMM_YYYY:
                    return LocalDate.parse(date, FORMATTER_dd_MMM_YYYY).atStartOfDay(ZoneId.systemDefault());
                default:
                    return Instant.ofEpochMilli(new Long(date)).atZone(ZoneId.systemDefault());
            }
        }


        public static TimeFormat from(String s){
            String val = s.toLowerCase().replaceAll("\\s+","");
            if(val.equals("ymd") || val.equals("1") || val.equals("yyyy/mm/dd")){
                return TimeFormat.yyyyMMdd;
            } else if(val.equals("y_m_d_hmsz") || val.equals("0")){
                return TimeFormat.y_M_d_hmsZ;
            } else if(val.equals("yahoo") || val.equals("3") || val.equals("yyyy-mm-dd h:m:s z") || val.equals("yyyy-mm-dd h:m:s")){
                return TimeFormat.yahoo;
            } else if(val.equals("dd_MMM_YYYY") || val.equals("4")){
                return TimeFormat.dd_MMM_YYYY;
            }
            return TimeFormat.TIMESTAMP;
        }
    }

    public enum YahooInterval {
        daily,
        weekly,
        monthly;

        public String toYahooString() {
            switch (this){
                case daily:
                    return "1d";
                case weekly:
                    return "5d";
                case monthly:
                    return "1mo";
                default:
                    return "1d";
            }
        }

        public static YahooInterval of(final String name){
            switch (name){
                case "1d":
                    return daily;
                case "5d":
                    return weekly;
                case "1mo":
                    return monthly;
                default:
                    return daily;
            }
        }
    }
}



