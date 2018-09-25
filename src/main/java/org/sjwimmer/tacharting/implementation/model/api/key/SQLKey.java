package org.sjwimmer.tacharting.implementation.model.api.key;

import org.sjwimmer.tacharting.chart.model.key.Key;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;

import java.util.Currency;

/**
 * Class for representing an unique entry in a SQL table
 */
public class SQLKey extends Key {

    public final String symbol;
    public final GeneralTimePeriod period;
    public final Currency currency;

    public SQLKey(String symbol, GeneralTimePeriod period, Currency currency){
        super(String.format("%s (%s)",symbol,currency));
        this.symbol = symbol;
        this.period = period;
        this.currency = currency;
    }

}
