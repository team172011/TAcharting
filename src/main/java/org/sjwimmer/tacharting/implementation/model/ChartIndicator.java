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

package org.sjwimmer.tacharting.implementation.model;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.ta4j.core.Bar;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.Num;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A Wrapper for the indicators displaying on a jfreeCharts org.sjwimmer.tacharting.chart panel.
 * An ChartIndicator can consist colorOf several ta4j-indiactors (e.g. bollinger bands...)
 */
public class ChartIndicator<Type> {

    private final List<Indicator<Type>> indicators;
    private final List<String> indicatorsNames;
    private final XYLineAndShapeRenderer renderer;
    private final String generalName;
    private boolean isSubchart;
    private final IndicatorCategory category;

    /**
     * Constuctor to create a ChartIndicator instance for just one {@link Indicator}
     * @param indicator the {@link Indicator}
     * @param name the displayed name of the indicator
     * @param isSubchart false if the indicator should be plotted as overlay on candlesticks chart
     */
    public ChartIndicator(Indicator<Type> indicator, String name, boolean isSubchart){
        this(indicator,name, new XYLineAndShapeRenderer(), isSubchart, IndicatorCategory.CUSTOM);
    }

    /**
     * Constuctor to create a ChartIndicator instance for just one {@link Indicator}
     * @param indicator the {@link Indicator}
     * @param name the displayed name of the indicator
     * @param isSubchart false if the indicator should be plotted as overlay on candlesticks chart
     * @param c the {@link IndicatorCategory category} for the indicator
     */
    public ChartIndicator(Indicator<Type> indicator, String name, boolean isSubchart, IndicatorCategory c){
        this(indicator,name, new XYLineAndShapeRenderer(), isSubchart,c);
    }

    /**
     * Constuctor to create a ChartIndicator instance for just one {@link Indicator}
     * @param indicator the ta4j indicator
     * @param name the name colorOf the indicator (with parameters)
     * @param renderer the renderer for line, shape etc.
     * @param isSubchart true if the ChartIndicator should be plotted as subchart
     * @param c the category colorOf the indicator in the menu colorOf this application
     */
    public ChartIndicator(Indicator<Type> indicator, String name, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c){
        indicators = new ArrayList<>();
        indicatorsNames = new ArrayList<>();
        indicators.add(indicator);
        indicatorsNames.add(name);
        generalName = name;
        this.renderer = renderer;
        this.isSubchart = isSubchart;
        category = c;
    }

    /**
     * Constructor for creating a ChartIndicator instance for several ta4j indicator
     * @param indicators the ta4j indicators
     * @param names the names colorOf the indicator (with parameters)
     * @param renderer the renderer for lines, shapes etc.
     * @param isSubchart true if the TaChartIndicators should be plotted as sub org.sjwimmer.tacharting.chart
     * @param c the category colorOf the ChartIndicator in the menu colorOf this application
     */
    public ChartIndicator(List<Indicator<Type>> indicators, List<String> names, String generalName, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c){
        this.indicators = indicators;
        indicatorsNames = names;
        this.generalName = generalName;
        this.renderer = renderer;
        this.isSubchart = isSubchart;
        category = c;
    }

    /**
     * Return the general name of this indicator (for example "Bollinger Bands")
     * @return general name of the indicator(s)
     */
    public String getGeneralName(){
        return generalName;
    }

    /**
     * Returns the name of a indicator
     * @param index index of the indicator
     * @return the name of the indicator
     */
    public String getName(int index){
        return indicatorsNames.get(index);
    }

    /**
     * @return a List of all {@link Indicator indicators} that represent this ChartIndicator
     */
    public List<Indicator<Type>> getIndicatorList(){
        return this.indicators;
    }

    /**
     *
     * @return the (first) indicator that represents this ChartIndicator
     */
    public Indicator<Type> getIndicator(){
        return getIndicator(0);
    }

    /**
     *
     * @param index index of the indicator
     * @return the indicator at index <code>index</code>
     */
    public Indicator<Type> getIndicator(int index){
        return indicators.get(index);
    }

    /**
     * @return false is this indicator is a {@link ChartType overlay}
     */
    public boolean isSubchart(){
        return isSubchart;
    }

    /**
     * @return the number of indicators that represent this ChartIndicator
     */
    public int getIndicatorsCount(){
        return  indicators.size();
    }

    /**
     * @return the {@link IndicatorCategory category} of this indicator
     */
    public IndicatorCategory getCategory(){
        return this.category;
    }

    /**
     * Extracts a {@link org.jfree.data.time.TimeSeries Collection of jfreeCharts TimeSeries} for plotting this
     * ChartIndicator
     * @return a TimeSeriesCollection
     * @throws ClassCastException if the generic type of {@link ChartIndicator} is not in bounds of {@link Num} interface
     */
    public TimeSeriesCollection getDataSet(){
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for(int index = 0; index< this.getIndicatorsCount(); index++){
            Indicator<Type> indicator = this.getIndicator(index);
            org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(this.getName(index));
            
            for(int i = 0; i<indicator.getTimeSeries().getBarCount(); i++){
                Bar t = indicator.getTimeSeries().getBar(i);
                chartTimeSeries.add(new Second(new Date(t.getEndTime().toEpochSecond() * 1000)), ((Num)indicator.getValue(i)).doubleValue());
            }
            dataset.addSeries(chartTimeSeries);
        }
        return dataset;
    }

    /**
     * @return the {@link XYLineAndShapeRenderer renderer} to plot this indicator
     */
    public XYLineAndShapeRenderer getRenderer(){
        return this.renderer;
    }

    // returns a new ChartIndicator instance of this indicator
    public ChartIndicator<Type> clone(){
        return new ChartIndicator<Type>(indicators,indicatorsNames,generalName,renderer,isSubchart,category);
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
