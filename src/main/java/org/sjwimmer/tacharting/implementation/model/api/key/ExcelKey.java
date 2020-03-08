package org.sjwimmer.tacharting.implementation.model.api.key;

import org.sjwimmer.tacharting.chart.model.TaBarSeries;
import org.sjwimmer.tacharting.chart.model.key.Key;
import org.sjwimmer.tacharting.implementation.model.api.ExcelConnector;

/**
 * Symbolic {@link Key} extending class for the {@link ExcelConnector}.
 * There should only be ohlcv data for one {@link TaBarSeries} in every
 * .xslx file, therefore a key is not needed.
 *
 */
public class ExcelKey extends Key {

	public static final ExcelKey DEFAULT_KEY = new ExcelKey();
	
	public ExcelKey() {
		super("Excel Key");
	}

}
