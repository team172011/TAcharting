package org.sjwimmer.tacharting.chart.api;

import org.sjwimmer.tacharting.chart.model.SQLKey;
import org.sjwimmer.tacharting.chart.model.TaTimeSeries;

/**
 * Interface for a connection class that manages resources of financial data
 * @param <T> the kind of resource (e.g {@link java.io.File, a File}, {@link String a path},
 *           or a {@link SQLKey key of a sql table}
 */
public interface Connector<T> {

    /**
     * @param resource the resource <code>T</code> of the financial data
     * @return a {@link TaTimeSeries currentSeries} of the financial data
     * @throws Exception Exception
     */
    TaTimeSeries getSeries(T resource) throws Exception;
}
