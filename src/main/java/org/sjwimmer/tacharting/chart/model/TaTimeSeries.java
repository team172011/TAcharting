package org.sjwimmer.tacharting.chart.model;

import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.implementation.model.api.key.SQLKey;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.util.Currency;
import java.util.List;

public class TaTimeSeries extends BaseTimeSeries implements ChartTimeSeries{

	private static final long serialVersionUID = 8968804460398253480L;
	
	private final Currency currency;
    private final GeneralTimePeriod periodType;

    public TaTimeSeries(String name, List<Bar> BarList, Currency currency, GeneralTimePeriod periodType){
        super(name,BarList);
        this.currency = currency;
        this.periodType = periodType;
    }

    public TaTimeSeries(TimeSeries series, Currency currency, GeneralTimePeriod periodType){
        this(series.getName(), series.getBarData(),currency,periodType);
    }

    public Currency getCurrency() {
        return currency;
    }

    public GeneralTimePeriod getTimeFormatType() {
        return periodType;
    }

    public SQLKey getKey(){
        return new SQLKey(this.getName(),periodType,currency);
    }

    /**
     * Two TaTimeSeries are equal if symbol, timePeriod and currency are the same
     * This overwriting is needed to reach correct behaviour of HashMaps and Sets
     * @param o object
     * @return false if <tt>o</tt> is not the 'same' or a TaTimeSeries
     */
    @Override
    public boolean equals(Object o){
        if(!(o instanceof TaTimeSeries)){
            return false;
        }
        TaTimeSeries other = ((TaTimeSeries)o);
        return other.getCurrency().equals(this.currency) &&
                other.getTimeFormatType().equals(this.periodType) &&
                other.getName().equals(this.getName());
    }
}
