package org.sjwimmer.tacharting.chart.model.types;

/**
 * Represents the available menu entries for the
 * {@link org.sjwimmer.tacharting.implementation.model.ChartIndicator ChartIndicators}
 */
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
