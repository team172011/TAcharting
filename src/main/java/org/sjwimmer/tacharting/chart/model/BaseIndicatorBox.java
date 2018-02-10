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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.sjwimmer.tacharting.chart.api.IndicatorParameterManager;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.sjwimmer.tacharting.chart.parameters.CreateFunctions;
import org.ta4j.core.Indicator;
import org.ta4j.core.Strategy;

import javax.xml.xpath.XPathException;
import java.util.List;

/**
 * Represents the model for plotted time series with indicators.
 * Stores the Strategies that can be plotted.
 */
public class BaseIndicatorBox implements IndicatorBox {

    private final SimpleObjectProperty<TaTimeSeries> currentSeries;
    private final ObservableList<IndicatorKey> indicatorKeys;
    private final IndicatorParameterManager parameterManager;
    private final ObservableMap<String, Strategy> strategies = FXCollections.observableHashMap();


    public BaseIndicatorBox(TaTimeSeries series, IndicatorParameterManager parameterManager){
        this.parameterManager = parameterManager;
        this.indicatorKeys = FXCollections.observableArrayList(parameterManager.getAllKeys());
        this.currentSeries = new SimpleObjectProperty<>(series);
        indicatorKeys.stream().forEach(e->System.out.print(e.getType().getDisplayName()));
    }

    @Override
    public void setTimeSeries(TaTimeSeries series) {
        if(!series.equals(currentSeries.get())){
            currentSeries.setValue(series);
        }
    }

    @Override
    public TaTimeSeries getTimeSeries() {
        return currentSeries.get();
    }

    @Override
    public ObservableObjectValue<TaTimeSeries> getObservableTimeSeries() {
        return currentSeries;
    }

    @Override
    public void addStrategy(String name, Strategy strategy) {
        strategies.put(name,strategy);
    }

    @Override
    public Strategy getStrategy(String name) {
        return strategies.get(name);
    }

    @Override
    public ObservableMap<String, Strategy> getAllStrategies() {
        return strategies;
    }

    @Override
    public void addIndicator(IndicatorKey identifier, List<Indicator> indicators, List<String> names, String generalName, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c) {

    }

    @Override
    public void addIndicator(Indicator indicator, boolean isSubchart) {

    }

    @Override
    public void addIndicator(IndicatorKey identifier, Indicator indicator, boolean isSubchart, IndicatorCategory c) {

    }

    @Override
    public void addIndicator(IndicatorKey identifier, Indicator indicator, String name, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c) {

    }

    @Override
    public void addIndicator(IndicatorKey identifier, ChartIndicator chartIndicator) {

    }

    @Override
    public void removeTempIndicator(IndicatorKey key) {

    }

    @Override
    public ObservableMap<IndicatorKey, ChartIndicator> getIndicartors() {
        return null;
    }

    @Override
    public ObservableMap<IndicatorKey, ChartIndicator> getTempIndicators() {
        return FXCollections.observableHashMap();
    }

    @Override
    public ChartIndicator getChartIndicator(IndicatorKey identifier) {
        return null;
    }

    @Override
    public IndicatorParameterManager getPropertiesManager() {
        return parameterManager;
    }

    @Override
    public ChartIndicator loadIndicator(AbstractBase base, IndicatorKey key) throws IllegalArgumentException, XPathException {
        return CreateFunctions.functions.get(key.getType()).apply(base, parameterManager.getParametersFor(key));
    }
}
