package org.sjwimmer.tacharting.implementation.util;

import org.sjwimmer.tacharting.chart.model.TaBarSeries;

@FunctionalInterface
public interface BarSeriesConverter<S> {

	TaBarSeries convert(S input);

}
