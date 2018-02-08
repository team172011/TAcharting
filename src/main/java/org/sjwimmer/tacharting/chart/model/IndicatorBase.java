package org.sjwimmer.tacharting.chart.model;


import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;


public class IndicatorBase {

    private final Indicator<Decimal> indicator;
    private final TimeSeries series;

    public IndicatorBase(Indicator<Decimal> indicator){
        this.series = indicator.getTimeSeries();
        this.indicator = indicator;
    }

    public IndicatorBase(TimeSeries series){
        this.series = series;
        indicator = null;
    }

    public Indicator<Decimal> getIndicator(){
        return this.indicator;
    }

    public TimeSeries getSeries(){
        return this.series;
    }


}
