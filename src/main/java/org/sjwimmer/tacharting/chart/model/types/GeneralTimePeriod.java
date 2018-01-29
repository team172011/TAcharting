package org.sjwimmer.tacharting.chart.model.types;

import org.sjwimmer.tacharting.chart.api.SQLConnector;

/**
 * Enum for possible time periods of candlestick data. For each {@code GeneralTimePeriod} there should be one table in the
 * database to store and update the data permanently.
 * see also:
 * <ul>  {@link SQLConnector}</ul>
 */
public enum GeneralTimePeriod {

    REALTIME,
    MINUTE,
    FIVE_MINUTE,
    HOUR,
    DAY,
    FIVE_DAY, //WEEK
    MONTH,
    QUARTER,
    YEAR
}
