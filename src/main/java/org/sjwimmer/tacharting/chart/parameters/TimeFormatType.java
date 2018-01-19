package org.sjwimmer.tacharting.chart.parameters;

/**
 * Enum for the different time formats that can be in a csv file or in api org.sjwimmer.tacharting.data streams
 */
public enum TimeFormatType {

    // e.g  2017-01-27, 16:00:00 ok
    yyyy_MM_ddHmsz(1, "yyyy-MM-dd H:m:s z","Date and Time in two columns (2017-01-27, 16:00:00)"),
    // e.g. 2017/08/18 ok
    yyyyMMdd(2, "yyyy/MM/dd", "(2017/08/18)"),
    // e.g  2017-12-31 ok
    YAHOO(3, "yyyy-MM-dd", "Format of Yahoo financial api (2017-12-31)"),
    // e.g  02 Jan 2017
    EODATA(4, "dd MMM yyyy", "Format of Eoddata (http://eoddata.com/) 02 Jan 2017");

    public final int id;
    public final String pattern, comment;

    TimeFormatType(int id, String pattern, String comment) {
        this.id = id;
        this.pattern = pattern;
        this.comment = comment;
    }
}
