/*
 The MIT License (MIT)

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

package chart;

import chart.types.IndicatorParameters;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.Tick;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An Wrapper for the indicators displaying on a jfreeCharts chart panel.
 * An ChartIndicator can consist of several ta4j-indiactors (e.g. bollinger bands...)
 */
public class ChartIndicator {

    private List<Indicator> indicators;
    private List<String> indicatorsNames;
    private XYLineAndShapeRenderer renderer;

    private String generalName;
    private boolean isSubchart;

    private IndicatorParameters.TaCategory category = IndicatorParameters.TaCategory.DEFAULT;

    public ChartIndicator(Indicator indicator, String name, boolean isSubchart, IndicatorParameters.TaCategory c){
        this(indicator,name, new XYLineAndShapeRenderer(), isSubchart,c);
    }

    /**
     * Constructor for creating a ChartIndicator instance for just one ta4j indicator
     * @param indicator the ta4j indicator
     * @param name the name of the indicator (with parameters)
     * @param renderer the renderer for line, shape etc.
     * @param isSubchart true if the ChartIndicator should be plotted as subchart
     * @param c the category of the indicator in the menu of this application
     */
    public ChartIndicator(Indicator indicator, String name, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorParameters.TaCategory c){
        indicators = new ArrayList<>();
        indicatorsNames = new ArrayList<>();
        indicators.add(indicator);
        indicatorsNames.add(name);
        generalName = name;
        this.renderer = renderer;
        this.isSubchart = isSubchart;
        category = c;
    }

    public ChartIndicator(List<Indicator> indicators, List<String> names, String generalName, boolean isSubchart, IndicatorParameters.TaCategory c){
        this(indicators,names,generalName,new XYLineAndShapeRenderer(false,false),isSubchart,c);
    }

    /**
     * Constructor for creating a ChartIndicator instance for several ta4j indicator
     * @param indicators the ta4j indicators
     * @param names the names of the indicator (with parameters)
     * @param renderer the renderer for lines, shapes etc.
     * @param isSubchart true if the TaChartIndicators should be plotted as sub chart
     * @param c the category of the ChartIndicator in the menu of this application
     */
    public ChartIndicator(List<Indicator> indicators, List<String> names, String generalName, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorParameters.TaCategory c){
        this.indicators = indicators;
        indicatorsNames = names;
        this.generalName = generalName;
        this.renderer = renderer;
        this.isSubchart = isSubchart;
        category = c;
    }


    public String getGeneralName(){
        return generalName;
    }
    public String getName(int index){
        return indicatorsNames.get(index);
    }

    public List<Indicator> getIndicatorList(){
        return this.indicators;
    }
    public Indicator getIndicator(){
        return getIndicator(0);
    }

    public Indicator getIndicator(int index){
        return indicators.get(index);
    }


    public boolean isSubchart(){
        return isSubchart;
    }

    public int getIndicatorsCount(){
        return  indicators.size();
    }


    public IndicatorParameters.TaCategory getCategory(){
        return this.category;
    }

    public TimeSeriesCollection getDataSet(){
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for(int index = 0; index< this.getIndicatorsCount(); index++){
            Indicator<Decimal> indicator = this.getIndicator(index);
            org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(this.getName(index));
            for(int i = 0; i<indicator.getTimeSeries().getTickCount(); i++){
                Tick t = indicator.getTimeSeries().getTick(i);
                chartTimeSeries.add(new Second(new Date(t.getEndTime().toEpochSecond() * 1000)), indicator.getValue(i).toDouble());
            }
            dataset.addSeries(chartTimeSeries);
        }
        return dataset;
    }

    public XYLineAndShapeRenderer getRenderer(){
        return this.renderer;
    }

    private static List<String> getSimpleName(List<Indicator> indicators){
        List<String> nameList = new ArrayList<>();
        for (Indicator indicator : indicators)
            nameList.add(indicator.toString());
        return  nameList;
    }

    // returns a new ChartIndicator instance of this indicator
    public ChartIndicator clone(){
        return new ChartIndicator(indicators,indicatorsNames,generalName,renderer,isSubchart,category);
    }

    public void setVisible(boolean visible) {
        for(int i = 0; i<indicators.size();i++){
            renderer.setSeriesVisible(i, visible);
        }
    }
}
