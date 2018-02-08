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

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.sjwimmer.tacharting.chart.model.types.ShapeType;
import org.sjwimmer.tacharting.chart.model.types.StrokeType;
import org.ta4j.core.Bar;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A Wrapper for the indicators displaying on a jfreeCharts org.sjwimmer.tacharting.chart panel.
 * A ChartIndicator can consist of several ta4j-indiactors (e.g. bollinger bands, keltner channel, macd...)
 */
public class ChartIndicator {

    private int internalId = -1;
    private Map<Integer, String> internalMapping = new HashMap<>();
    private Map<String, Indicator<Decimal>> indicators = new HashMap<>();
    private XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    private IndicatorKey key;
    private ChartType chartType;
    private IndicatorCategory category;

    private TimeSeriesCollection collection = new TimeSeriesCollection();

    public ChartIndicator(IndicatorKey key){
        this.key = key;
    }

    public void addIndicator(Indicator<Decimal> indicator){
        addIndicator(indicator,indicator.toString(),Color.BLACK, ShapeType.LINE.shape, StrokeType.SMALL_LINE.stroke, ChartType.OVERLAY);
    }

    public void addIndicator(Indicator<Decimal> indicator, Color color, String name, ShapeType shapeType, StrokeType strokeType,ChartType type){
        addIndicator(indicator, name, color, shapeType.shape,strokeType.stroke,type);
    }

    /**
     * Adds a new indicator to this ChartIndicator
     * @param indicator the {@link Indicator}
     * @param name the name of the indicator
     * @param color color for renderer
     * @param shape shape for renderer
     * @param stroke stroke for renderer
     * @param type the kind of chart (Overlay or Subplot)
     */
    public void addIndicator(Indicator<Decimal> indicator, String name, Color color, Shape shape, Stroke stroke, ChartType type){
        internalId++;
        indicators.put(name,indicator);
        internalMapping.put(internalId,name);
        renderer.setSeriesPaint(internalId,color);
        renderer.setSeriesShape(internalId,shape);
        renderer.setSeriesStroke(internalId,stroke);
        this.chartType = type;
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name); //TODO maybe store name here?
        org.ta4j.core.TimeSeries series = indicator.getTimeSeries();
        for(int i = series.getBeginIndex(); i <= series.getEndIndex(); i++){
            Bar t = series.getBar(i);
            chartTimeSeries.add(new Second(new Date(t.getEndTime().toEpochSecond() * 1000)), indicator.getValue(i).doubleValue());
        }
        collection.addSeries(chartTimeSeries);
    }

    /**
     * Return the general name of this indicator (for example "Bollinger Bands")
     * @return general name of the indicator(s)
     */
    public String getGeneralName(){
        return key.toString();
    }

    /**
     * @return false is this indicator is a {@link ChartType overlay}
     */
    public boolean isSubchart(){
        return chartType.equals(ChartType.SUBCHART);
    }

    /**
     * @return the number of indicators that represent this ChartIndicator
     */
    public int getIndicatorsCount(){
        return  indicators.size();
    }

    public XYLineAndShapeRenderer getRenderer(){
        return renderer;
    }

    /**
     * @return the {@link IndicatorCategory category} of this indicator
     */
    public IndicatorCategory getCategory(){
        return this.category;
    }

    /**
     * Extract a {@link org.jfree.data.time.TimeSeries Collection of jfreeCharts TimeSeries} for plotting this
     * ChartIndicator
     * @return a TimeSeriesCollection
     */
    public TimeSeriesCollection getDataSet(){
        return collection;
    }

    /**
     * Can be used to make all lines of this indicator invisible
     * @param visible true if the indicators should be visible if rendered
     */
    public void setVisible(boolean visible) {
        for(int i = 0; i<indicators.size();i++){
            renderer.setSeriesVisible(i, visible);
        }
    }
}
