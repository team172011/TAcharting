package org.sjwimmer.tacharting.chart.model;

import java.util.Currency;

import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.implementation.model.api.key.SQLKey;
import org.ta4j.core.TimeSeries;

public interface ChartTimeSeries extends TimeSeries {
	
	/**
	 * Returns the {@link Currency} of this time series
	 * @return {@link Currency}
	 */
	public Currency getCurrency();
	
	/**
	 * Returns the {@link GeneralTimePeriod} of this time series
	 * @return {@link GeneralTimePeriod}
	 */
	public GeneralTimePeriod getTimeFormatType();
	
	/**
	 * Returns the {@link SQLKey} that identifies this time series
	 * in a SQLDatabase
	 * @return
	 */
	public SQLKey getKey();
	
    /**
     * Two ChartTimeSeries are equal if symbol, timePeriod and currency are the same
     * This overwriting is needed to reach correct behaviour of HashMaps and Sets
     * @param o object
     * @return false if <tt>o</tt> is not the 'same' or a TaTimeSeries
     */
    public  boolean equals(Object o);

}
