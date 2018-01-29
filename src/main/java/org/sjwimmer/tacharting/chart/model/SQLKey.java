package org.sjwimmer.tacharting.chart.model;

import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;

import java.util.Currency;

public class SQLKey {


    public final String symbol;
    public final GeneralTimePeriod period;
    public final Currency currency;

    public SQLKey(String symbol, GeneralTimePeriod period, Currency currency){
        this.symbol = symbol;
        this.period = period;
        this.currency = currency;
    }
}
