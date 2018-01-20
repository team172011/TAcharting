package org.sjwimmer.tacharting.chart;

import org.sjwimmer.tacharting.chart.types.GeneralTimePeriod;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.util.Currency;
import java.util.List;

public class TaTimeSeries extends BaseTimeSeries{

    private final Currency currency;
    private final GeneralTimePeriod periodType;

    public TaTimeSeries(String name, List<Tick> tickList, Currency currency, GeneralTimePeriod periodType){
        super(name,tickList);
        this.currency = currency;
        this.periodType = periodType;
    }

    public TaTimeSeries(TimeSeries series, Currency currency, GeneralTimePeriod periodType){
        this(series.getName(), series.getTickData(),currency,periodType);
    }

    public Currency getCurrency() {
        return currency;
    }

    public GeneralTimePeriod getTimeFormatType() {
        return periodType;
    }
}
