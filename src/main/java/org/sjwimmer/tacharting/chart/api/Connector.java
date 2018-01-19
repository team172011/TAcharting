package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.TaTimeSeries;

import java.io.IOException;

/**
 * Interface for a connection class that manages resources of financial data
 * @param <T> the kind of resource (e.g {@link java.io.File, a File} or {@link String a path}
 */
public interface Connector<T> {

    /**
     *
     * @param resource the resource of the financial data
     * @return a {@link TaTimeSeries series} of the financial data
     * @throws IOException IOException
     */
    TaTimeSeries getSeries(T resource) throws IOException;
}
