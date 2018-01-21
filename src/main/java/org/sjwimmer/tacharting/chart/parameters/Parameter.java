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
import org.sjwimmer.tacharting.chart.types.TimeFormatType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

public  class Parameter {


    public final static String OS = System.getProperty("os.name").toLowerCase();

    /** Header names of financial org.sjwimmer.tacharting.data ********************************************************************************/
    public final static HashSet<String> HEADER_DATE = new HashSet<>(Arrays.asList("date", "timestamp"));
    public final static HashSet<String> HEADER_OPEN = new HashSet<>(Arrays.asList("open", "first", "open price", "first price"));
    public final static HashSet<String> HEADER_HIGH = new HashSet<>(Arrays.asList("high", "max", "high price", "max price"));
    public final static HashSet<String> HEADER_LOW = new HashSet<>(Arrays.asList("low", "min", "low price", "min price"));
    public final static HashSet<String> HEADER_CLOSE = new HashSet<>(Arrays.asList("close", "last", "last price", "close price", "price"));
    public final static HashSet<String> HEADER_Volume = new HashSet<>(Arrays.asList("volume", "vol"));
    public final static HashSet<String> HEADER_DATE2 = new HashSet<>(Arrays.asList("time"));
    public static final int DEFAULT_LOOK_BACK = 2;

    public enum Columns{
        DATE,
        DATE2,
        OPEN,
        HIGH,
        LOW,
        CLOSE,
        VOLUME
    }

    /** Files *********************************************************************************************************/
    public final static String USER_HOME = System.getProperty("user.home");
    public final static char S = System.getProperty("file.separator").charAt(0);
    public final static String PROGRAM_FOLDER = USER_HOME +S+"tachart";
    public final static String INDICATOR_PROPERTIES_FILE = "indicatorParameters.xml";
    public final static String USER_INDICATOR_PROPERTIES_FILE = PROGRAM_FOLDER+S+"indicatorParameters.xml";
    public final static String API_PROPERTIES_FILE = PROGRAM_FOLDER+S+"properties/api.properties";
    public static final String DATABASE_PATH = PROGRAM_FOLDER+S+"financial_data";

    /** Extensions ****************************************************************************************************/
    public final static FileChooser.ExtensionFilter EXTENSION_FILTER_CSV = new  FileChooser.ExtensionFilter("CSV","*.csv", "*.CSV");
    public final static FileChooser.ExtensionFilter EXTENSION_FILTER_EXCEL = new FileChooser.ExtensionFilter("Execl","*.xls", "*.xlsx");

    /** Property names ************************************************************************************************/
    public static final String PROPERTY_YAHOO_FROM = "yahoo_from";
    public static final String PROPERTY_YAHOO_TO = "yahoo_to";
    public static final String PROPERTY_YAHOO_INTERVAL = "yahoo_interval";

    public static final String PROPERTY_CSV_SEPARATOR = "csv_seperator";
    public static final String PROPERTY_CSV_QUOTE = "csv_endline";

    /** Database ******************************************************************************************************/
    public static final String DATABASE_NAME = "financial_data.db";



    /** Menu entries **************************************************************************************************/
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
    public static Locale DEFAULT_LOCALE = Locale.US;
    public static String DEFAULT_PATTERN = TimeFormatType.yyyyMMdd.pattern;

    /************************************************************************************************************/
    public static String DEFAULT_CURRENCY = "USD";


}



