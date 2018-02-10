package org.sjwimmer.tacharting.chart.model;

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
