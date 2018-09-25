package org.sjwimmer.tacharting.implementation.model.api.key;

import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.key.Key;
import org.sjwimmer.tacharting.implementation.model.api.CSVConnector;

/**
 * Symbolic {@link Key} extending class for the {@link CSVConnector}.
 * There should only be ohlcv data for one {@link TaTimeSeries} in every
 * .csv file, therefore a key is not needed.
 *
 */
public class CSVKey extends Key {

	public static final CSVKey DEFAULT_KEY = new CSVKey();
	
	public CSVKey() {
		super("CSV Key");
	}

}
