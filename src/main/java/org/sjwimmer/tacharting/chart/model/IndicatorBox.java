/*
 GNU Lesser General Public License

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.sjwimmer.tacharting.chart.model;

import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableMap;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.sjwimmer.tacharting.chart.api.IndicatorParameterManager;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.ta4j.core.Indicator;
import org.ta4j.core.Strategy;

import java.util.List;

public interface IndicatorBox {

    /**
     * Sets the current {@link TaTimeSeries series} of this indicator box
     * All dynamical added indicators (via runtime through {@link #addIndicator(Indicator, boolean) addIndicator(...)}
     * will be deleted or updated // TODO: check if it is possible to update a custom indicator
     * All static added indicators (loaded fom xml file) will be reloaded so that they contain correct data
     * @param series the {@link TaTimeSeries time series}
     */
    void setTimeSeries(TaTimeSeries series);

    /**
     * Returns the {@link TaTimeSeries time series} stored in this indicator box
     * @return the time series
     */
    TaTimeSeries getTimeSeries();
    ObservableObjectValue<TaTimeSeries> getObservableTimeSeries();

    /**
     * Adds a strategy to the indicator box
     * @param name the unique name of the strategy that will be displayed in menu
     * @param strategy the {@link Strategy strategy}
     */
    void addStrategy(String name, Strategy strategy);

    /**
     * Retruns the corresponding {@link Strategy strategy}
     * @param name the name of the strategy
     * @return if exists corresponding strategy else <code>null</code>
     */
    Strategy getStrategy(String name);

    /**
     * @return All {@link Strategy strategies} stored in this indicator box
     */
    ObservableMap<String, Strategy> getAllStrategies();


    void addIndicator(IndicatorKey identifier, List<Indicator> indicators, List<String> names, String generalName, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c);

    void addIndicator(Indicator indicator, boolean isSubchart);

    void addIndicator(IndicatorKey identifier, Indicator indicator, boolean isSubchart, IndicatorCategory c);

    void addIndicator(IndicatorKey identifier, Indicator indicator, String name, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c);

    void addIndicator(IndicatorKey identifier, ChartIndicator chartIndicator);

    void removeTempIndicator(IndicatorKey key);

    ObservableMap<IndicatorKey, ChartIndicator> getIndicartors();

    ObservableMap<IndicatorKey, ChartIndicator> getTempIndicators();

    ChartIndicator getChartIndicator(IndicatorKey identifier);

    IndicatorParameterManager getPropertiesManager();

    ChartIndicator loadIndicator(IndicatorBase base, IndicatorKey key);
}
