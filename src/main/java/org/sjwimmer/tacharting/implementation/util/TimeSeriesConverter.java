package org.sjwimmer.tacharting.implementation.util;

import org.sjwimmer.tacharting.chart.model.TaTimeSeries;

@FunctionalInterface
public interface TimeSeriesConverter<S> {
	
	TaTimeSeries convert(S input);

}
