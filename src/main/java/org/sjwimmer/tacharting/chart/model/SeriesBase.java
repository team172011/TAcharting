package org.sjwimmer.tacharting.chart.model;

import org.sjwimmer.tacharting.chart.model.types.BaseType;

public class SeriesBase extends AbstractBase {

    private final TaTimeSeries series;

    public SeriesBase(TaTimeSeries series) {
        super(BaseType.SERIES);
        this.series = series;
    }


    public TaTimeSeries getSeries() {
        return series;
    }
}
