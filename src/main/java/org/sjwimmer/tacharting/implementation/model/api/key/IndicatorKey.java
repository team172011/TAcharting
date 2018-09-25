package org.sjwimmer.tacharting.implementation.model.api.key;

import org.sjwimmer.tacharting.chart.model.key.Key;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;

/**
 * Key to identify an indicator instance on the chart/menu/indicatorBox
 * Such as EMAIndicator_1, EMAIndicator2
 */
public class IndicatorKey extends Key {

    private final IndicatorType indicatorNames;
    private final int id;

    public IndicatorKey(IndicatorType name, int id) {
        super(String.format("%s [%s]",name, id));
        this.indicatorNames = name;
        this.id = id;
    }
}
