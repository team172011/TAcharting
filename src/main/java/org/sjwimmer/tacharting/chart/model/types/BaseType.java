package org.sjwimmer.tacharting.chart.model.types;

public enum BaseType {
    INDICATOR, // Indicator needs another indicator
    SERIES, // Indicator needs a TimeSeries as input
    BOOTH; // Indicator need Indicator and/or TimeSeries as input
}
