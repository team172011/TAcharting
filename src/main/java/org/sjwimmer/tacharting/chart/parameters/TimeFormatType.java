package org.sjwimmer.tacharting.chart.parameters;

/**
 * Enum for the different time formats that can be in a csv file or in api data streams
 */
public enum TimeFormatType {

    yyyy_MM_ddHmsz(1, "yyyy-MM-dd H:m:s z"),
    yyyyMMdd(2, "yyyy/MM/dd"),
    YAHOO(3, "yyyy-MM-dd"),
    EODATA(4, "dd MM yyyy");

    public final int id;
    public final String pattern;

    TimeFormatType(int id, String pattern) {
        this.id = id;
        this.pattern = pattern;
    }
}
/*
    public final static DateTimeFormatter FORMATTER_y_M_d_hmsZ = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd H:m:s z");
    public final static DateTimeFormatter FORMATTER_yyyyMMd = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd");
    public final static DateTimeFormatter FORMATTER_yyy_MM_dd = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"); // yahoo csv
    public final static DateTimeFormatter FORMATTER_dd_MMM_YYYY = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US); // eodata csv
*/
