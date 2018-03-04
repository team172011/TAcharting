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

import javafx.scene.paint.Color;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.sjwimmer.tacharting.chart.model.types.ChartMode;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.ta4j.core.Bar;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.*;

/**
 * A Wrapper for the indicators displaying on a jfreeCharts org.sjwimmer.tacharting.chart panel.
 * A ChartIndicator can consist of several ta4j-indiactors (e.g. bollinger bands, keltner channel, macd)
 */
public class ChartIndicator implements JsonRessource{

    private int internalId = -1;
    private final Map<Integer, String> internalMapping = new HashMap<>();
    private final Map<String, Indicator<Decimal>> indicators = new HashMap<>();
    private final Map<String, IndicatorStyleParameter> style = new HashMap<>();
    private IndicatorKey key;
    private ChartType chartType;
    private ChartMode chartMode;
    private IndicatorCategory category;

    private TimeSeriesCollection collection = new TimeSeriesCollection();

    public ChartIndicator(IndicatorKey key){
        this(key, ChartType.OVERLAY,ChartMode.LINE);
    }

    public ChartIndicator(IndicatorKey key, ChartType type){
        this(key, type, ChartMode.LINE);
    }

    public ChartIndicator(IndicatorKey key, ChartType type, ChartMode chartMode){
        this.key = key;
        this.chartType = type;
        this.chartMode = chartMode;
    }

    public void addIndicator(Indicator<Decimal> indicator){
        addIndicator(indicator, indicator.toString(), Color.BLACK, true);
    }

    public void addIndicator(Indicator<Decimal> indicator, String name, Color color){
        addIndicator(indicator, name, color,true);
    }

    /**
     * Adds a new indicator to this ChartIndicato
     *
     * @param indicator the {@link Indicator}
     * @param name the name of the indicator
     * @param color color for renderer
     */
    public void addIndicator(Indicator<Decimal> indicator, String name, Color color, boolean visibile){
        internalId++;
        indicators.put(name,indicator);
        internalMapping.put(internalId, name);
        style.put(name, new IndicatorStyleParameter(color, visibile));
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name); //TODO maybe store name here?
        org.ta4j.core.TimeSeries series = indicator.getTimeSeries();
        for(int i = series.getBeginIndex(); i <= series.getEndIndex(); i++){
            Bar t = series.getBar(i);
            chartTimeSeries.add(new Second(new Date(t.getEndTime().toEpochSecond() * 1000)), indicator.getValue(i).doubleValue());
        }
        collection.addSeries(chartTimeSeries);
    }

    public void setChartType(ChartType type){
        this.chartType = type;
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

    public IndicatorKey getKey() {
        return key;
    }

    public void setKey(IndicatorKey key){
        this.key = key;
    }

    /**
     * Can be used to make all lines of this indicator invisible
     * @param visible true if the indicators should be visible if rendered
     */
    public void setVisible(boolean visible) {
        for(String key: style.keySet()){
            style.get(key).setVisible(visible);
        }
    }

    public  void setVisible(int id, boolean visible){
        style.get(internalMapping.get(id)).setVisible(visible);
    }

    public Indicator<Decimal> getIndicator(String name){
        return indicators.get(name);
    }

    public Indicator<Decimal> getIndicator(int i){
        return indicators.get(internalMapping.get(i));
    }

    public JsonObject createJsonObject(){
        return createJsonObject(getIndicator(0).getTimeSeries().getEndIndex());
    }

    /**
     * Invisible indicators will not be added to the json object
     * @param maxSize
     * @return
     */
    public JsonObject createJsonObject(int maxSize){
        JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("name",getGeneralName());
        json.add("subchart",Boolean.toString(isSubchart()));
        json.add("mode", chartMode.toString().toLowerCase());

        List<String> indicatorNames = new ArrayList<>();
        int start = getIndicator(0).getTimeSeries().getEndIndex()-maxSize;
        for(Map.Entry<String, Indicator<Decimal>> entry: indicators.entrySet()){
            if(!style.get(entry.getKey()).isVisible()){
                continue; // Do not include invisible data
            }
            Indicator<Decimal> indicator = entry.getValue();
            List<String> values = new ArrayList<>();
            JsonArrayBuilder data = Json.createArrayBuilder();
            for(int i = start; i<= indicator.getTimeSeries().getEndIndex(); i++){
                JsonObjectBuilder barEntry = Json.createObjectBuilder();
                barEntry.add("time",indicator.getTimeSeries().getBar(i).getSimpleDateName());
                barEntry.add("value",indicator.getValue(i).toString());
                data.add(barEntry);
            }
            JsonObjectBuilder jsonEntry = Json.createObjectBuilder();
            jsonEntry.add("data", data);
            Color c = Color.PINK;
            jsonEntry.add("color",String.format("#%02X%02X%02X", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)));


            json.add(entry.getKey(), jsonEntry);
            indicatorNames.add(entry.getKey());
        }
        json.add("Indicators",Json.createArrayBuilder(indicatorNames));
        return json.build();
    }
}
