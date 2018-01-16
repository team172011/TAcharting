package org.sjwimmer.tacharting.chart.api;

import org.ta4j.core.TimeSeries;

import java.io.IOException;


public interface Connector<T> {

    TimeSeries getSeries(T ressource) throws IOException;
}
