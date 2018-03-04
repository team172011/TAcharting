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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.sjwimmer.tacharting.chart.controller.manager.BaseIndicatorParameterManager;
import org.sjwimmer.tacharting.chart.controller.manager.IndicatorParameterManager;
import org.sjwimmer.tacharting.chart.controller.manager.IndicatorSettingsManager;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.sjwimmer.tacharting.chart.model.types.IndicatorParameterType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.ta4j.core.Indicator;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;

import javax.xml.xpath.XPathException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.sjwimmer.tacharting.chart.model.types.IndicatorType.*;

/**
 * Represents the model for plotted time currentSeries with indicators.
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
        init();
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
    public ChartIndicator loadIndicator(IndicatorKey key) throws IllegalArgumentException, XPathException {
        return indicatorFunctions.get(key.getType()).apply(parameterManager.getParametersFor(key));
    }

    private static IndicatorParameterManager manager = new BaseIndicatorParameterManager();
    public final Map<IndicatorType, Function<Map<String, IndicatorParameter>, ChartIndicator>> indicatorFunctions
            = new HashMap<>();








    /** functions to create a ChartIndicator **************************************************************************/
     {
        indicatorFunctions.put(EMA, params -> {
            ChartIndicator basedOn = extractChartIndicatorBase(params);
            int innerIndex = params.get(Parameter.iid) == null ? 0 : params.get(Parameter.iid).getInteger();
            EMAIndicator ema = new EMAIndicator(basedOn.getIndicator(innerIndex),
                    params.get(Parameter.tf).getInteger());
            basedOn.addIndicator(ema,
                    String.format("EMA (%s, %s)",
                            basedOn.getGeneralName(),
                            params.get("Time Frame").getInteger()),
                    params.get("Color").getColor());
            basedOn.setKey(new IndicatorKey(EMA,params.get(Parameter.id).getInteger()));
            return basedOn;
        });

        indicatorFunctions.put(OPEN, params -> {
            TaTimeSeries base = extractSeries(params);
            ChartIndicator indicator = new ChartIndicator(new IndicatorKey(OPEN, params.get(Parameter.id).getInteger()),
                    ChartType.OVERLAY);
            indicator.addIndicator(new OpenPriceIndicator(base));
            indicator.setVisible(false);
            return indicator;
        });

        indicatorFunctions.put(MAX, params -> {
            TaTimeSeries base = extractSeries(params);
            ChartIndicator indicator = new ChartIndicator(new IndicatorKey(MAX, params.get(Parameter.id).getInteger()),
                    ChartType.OVERLAY);
            indicator.addIndicator(new MaxPriceIndicator(base));
            indicator.setVisible(false);
            return indicator;
        });

        indicatorFunctions.put(MIN, params -> {
            TaTimeSeries base = extractSeries(params);
            ChartIndicator indicator = new ChartIndicator(new IndicatorKey(MIN, params.get(Parameter.id).getInteger()),
                    ChartType.OVERLAY);
            indicator.addIndicator(new MinPriceIndicator(base));
            indicator.setVisible(false);
            return indicator;
        });

        indicatorFunctions.put(CLOSE, params -> {
            TaTimeSeries base = extractSeries(params);
            ChartIndicator indicator = new ChartIndicator(new IndicatorKey(CLOSE, params.get(Parameter.id).getInteger()),
                    ChartType.OVERLAY);
            indicator.addIndicator(new ClosePriceIndicator(base));
            indicator.setVisible(false);
            return indicator;
        });

    }

    private ChartIndicator extractChartIndicatorBase(Map<String, IndicatorParameter> params){
        IndicatorKey basedOnParameter = params.get(Parameter.base).getIndicatorKey();
        Map<String, IndicatorParameter> baseParams = new HashMap<>();
        try {
            baseParams = manager.getParametersFor(basedOnParameter);  //TODO hier wird eine Referenz zum parameterManager benoetigt, damit eine Indicatorbase selbst,
        } catch (Exception e){                                        //staendig erzeugt werden kann
            e.printStackTrace();
        }

        return indicatorFunctions.get(basedOnParameter.getType()).apply(baseParams);
    }

    private TaTimeSeries extractSeries(Map<String, IndicatorParameter> params) {
        return getTimeSeries();
    }

    public final Map<IndicatorType, BiFunction<ChartingContext, IndicatorKey, Dialog<ChartIndicator>>> dialogFunctions = new HashMap<>();

    /** Dialogs to add a indicator to the chart ***********************************************************************/
    public void init(){
        dialogFunctions.put(EMA, (context,key) -> {

            IndicatorParameterManager parameterManager = context.getManager();

            Dialog<ChartIndicator> dialog = new Dialog<>();
            dialog.setTitle("Adding "+key.getType().getDisplayName());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.APPLY);
            dialog.getDialogPane().setContent(IndicatorSettingsManager.getAsNode(key,context));
            dialog.setResultConverter(d->{
                if(d == ButtonType.APPLY){
                    try {
                        Map<String, IndicatorParameter> emaPara = parameterManager.getParametersFor(key);
                        emaPara.put(Parameter.series,new IndicatorParameter("Series", IndicatorParameterType.SERIES,currentSeries.get()));
                        return indicatorFunctions.get(EMA).apply(emaPara);
                    } catch (XPathException xpe){
                        xpe.printStackTrace();
                    }
                }
                return null;
            });
            return dialog;
        });

    }



}
