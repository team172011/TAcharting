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

package org.sjwimmer.tacharting.chart;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.CacheHint;
import javafx.scene.layout.StackPane;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.time.Minute;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.ta4j.core.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Main class for creating and managing the charts.
 * Creates a Chart, a ChartViewer and shows it.
 * Adds and removes ChartIndicators, TradingRecords
 */
public class TaChart extends StackPane implements MapChangeListener<String, ChartIndicator> {


    private final ChartIndicatorBox chartIndicatorBox;
    private final CombinedDomainXYPlot combinedXYPlot;
    private final XYPlot mainPlot;
    private final TaChartViewer viewer;


    // awt colors override the stylesheets of the StackPane i think..
    private final Color chartBackground = Color.WHITE;
    private final Color legendBackground = new Color(0,0,0,0);

    private final Map<TradingRecord, List<Marker>> mapTradingRecordMarker;
    private final List<XYPlot> currentSubPlots;

    private final ObservableList<String> currentOverlays = FXCollections.observableArrayList();
    private final ObservableList<String> currentSubplots = FXCollections.observableArrayList();



    /**
     * Constructor.
     * @param box a ChartIndicatorBox
     */
    public TaChart(ChartIndicatorBox box){
        mapTradingRecordMarker = new HashMap<>();
        this.chartIndicatorBox = box;
        this.chartIndicatorBox.getChartIndicatorMap().addListener(this);
        XYDataset candlestickData = createOHLCDataset(box.getTimeSeries());
        this.currentSubPlots = new ArrayList<>();
        this.mainPlot = createMainPlot(candlestickData);
        this.combinedXYPlot = createCombinedDomainXYPlot(mainPlot);
        this.setCache(true);
        this.setCacheHint(CacheHint.SPEED);
        final JFreeChart chart = new JFreeChart(chartIndicatorBox.getTimeSeries().getName(), combinedXYPlot);
        this.viewer = new TaChartViewer(chart);
        this.viewer.setCache(true);
        this.viewer.setCacheHint(CacheHint.SPEED);
        chart.setBackgroundPaint(chartBackground);
        getChildren().add(viewer);
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.TOP);
        legend.setItemFont(new Font("Arial", 1, 12));
        legend.setBackgroundPaint(legendBackground);
        chartIndicatorBox.getObservableTimeSeries().addListener((ob, o, n)-> reloadTimeSeries(n));
    }

    public ChartIndicatorBox getChartIndicatorBox() {
        return chartIndicatorBox;
    }


    /**
     * plots or removes the trading record
     * @param record the trading record
     * @param on adds the record if true, else the record will be removed from the org.sjwimmer.tacharting.chart
     */
    public void plotTradingRecord(TradingRecord record, boolean on){
        if (on){
            addEntryExitSignals(record);
        }else {
            removeEntryExitSignals(record);
        }

    }

    /**
     * removes entry and exit signals from the org.sjwimmer.tacharting.chart.
     * @param record
     */
    private void removeEntryExitSignals(TradingRecord record) {
        List<Marker> markers = this.mapTradingRecordMarker.get(record);
        if (markers!= null) {
            for (Marker m : markers) {
                this.mainPlot.removeDomainMarker(m);
            }
            this.mapTradingRecordMarker.remove(record);
        }
    }

    /**
     * Adds entry and exits signals to the cahrt.
     * @param record the trading record
     * */
    private void addEntryExitSignals(TradingRecord record){
        List<Trade> trades = record.getTrades();
        Order.OrderType orderType = record.getLastExit().getType().complementType();
        List<Marker> markers = new ArrayList<>();
        RectangleAnchor entryAnchor = RectangleAnchor.TOP_LEFT;
        RectangleAnchor exitAnchor = RectangleAnchor.BOTTOM_RIGHT;

        Color entryColor = orderType==Order.OrderType.SELL ? Color.RED : Color.GREEN;
        Color exitColor = orderType==Order.OrderType.SELL ? Color.GREEN: Color.RED;
        TimeSeries series = chartIndicatorBox.getTimeSeries();
        for(Trade trade: trades){
            double entry = new Minute(Date.from(
                    series.getTick(trade.getEntry().getIndex()).getEndTime().toInstant())).getFirstMillisecond();
            double exit = new Minute(Date.from(
                    series.getTick(trade.getExit().getIndex()).getEndTime().toInstant())).getFirstMillisecond();

            ValueMarker in = new ValueMarker(entry);
            in.setLabel(orderType.toString());
            in.setLabelPaint(Color.WHITE);
            in.setLabelAnchor(entryAnchor);
            in.setPaint(entryColor);
            this.mainPlot.addDomainMarker(in);

            ValueMarker out = new ValueMarker(exit);
            out.setLabel(orderType.complementType().toString());
            out.setLabelPaint(Color.WHITE);
            out.setLabelAnchor(exitAnchor);
            out.setPaint(exitColor);
            this.mainPlot.addDomainMarker(out);

            IntervalMarker imarker = new IntervalMarker(entry, exit, entryColor);
            imarker.setAlpha(0.1f);
            this.mainPlot.addDomainMarker(imarker);
            markers.add(imarker);
            markers.add(in);
            markers.add(out);
        }
        this.mapTradingRecordMarker.put(record, markers);
    }


    /**
     * Plots the corresponding indicators colorOf the list colorOf identifiers as overlays
     * @param indicatorIdentifiers a list colorOf identifiers e.g. "EMAIndicator_1"
     */
    public void plotOverlays(List<String> indicatorIdentifiers) {
        List<ChartIndicator> overlays = new ArrayList<>();
        for(int i = 1; i < mainPlot.getRendererCount(); i++){ // 0 = candlestick renderer
            XYItemRenderer renderer = mainPlot.getRenderer(i);
            int seriesCounter = 0;
            while(renderer.getSeriesVisible(seriesCounter) != null){
                renderer.setSeriesVisible(seriesCounter,false);
                seriesCounter++;
            }
        }
        int anonymID = 1; // 0 = candlestick org.sjwimmer.tacharting.data
        for(String identifier: indicatorIdentifiers) {
            ChartIndicator chartIndicator = chartIndicatorBox.getChartIndicator(identifier);
            overlays.add(chartIndicator);
            mainPlot.setRenderer(anonymID, chartIndicator.getRenderer()); // set renderer first!
            mainPlot.setDataset(anonymID, chartIndicator.getDataSet());
            chartIndicator.setVisible(true);
            mainPlot.mapDatasetToRangeAxis(anonymID, 0);
            mainPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            anonymID++;
        }
    }

    /**
     * Plots the corresponding indicators colorOf the list colorOf identifiers as subplots
     * @param indicatorIdentifiers a list colorOf identifiers e.g. "MACDIndicator_1"
     */
    public void plotSubPlots(List<String> indicatorIdentifiers){
        List<ChartIndicator> subPlots = new ArrayList<>();

        for(int i = 1; i < combinedXYPlot.getSubplots().size(); i++){
            XYPlot plot = (XYPlot) combinedXYPlot.getSubplots().get(i);
            combinedXYPlot.remove(plot);
        }

        for (String identifier: indicatorIdentifiers){
            ChartIndicator chartIndicator = chartIndicatorBox.getChartIndicator(identifier);
            subPlots.add(chartIndicator);
            XYPlot subPlot = createSubplotFor(chartIndicator);
            combinedXYPlot.add(subPlot);
            currentSubPlots.add(subPlot);
        }
        // workaround, combinedXYPlot would loos the domain axis and org.sjwimmer.tacharting.chart would look empty
        if(indicatorIdentifiers.size() < 1){
            combinedXYPlot.setDomainAxis(((XYPlot)combinedXYPlot.getSubplots().get(0)).getDomainAxis());
        }
    }

    /**
     * Creating the xyPlot for the base candlestick org.sjwimmer.tacharting.chart
     * @param dataset a XYDataset for the candlestick
     */
    private static XYPlot createMainPlot(XYDataset dataset){

        TaCandlestickRenderer renderer = new TaCandlestickRenderer();
        renderer.setCandleWidth(2);
        renderer.setDrawVolume(true);

        NumberAxis numberAxis = new NumberAxis("Price");
        numberAxis.setTickLabelPaint(Color.GRAY);
        numberAxis.setLabelPaint(Color.GRAY);

        DateAxis dateAxis = new DateAxis("Date");
        dateAxis.setTickLabelPaint(Color.GRAY);
        dateAxis.setLabelPaint(Color.GRAY);

        // XYPlot with candlesticks
        XYPlot plot = new XYPlot(dataset, dateAxis, numberAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        BasicStroke grid = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f,
                                            new float[]{1f}, 0.0f);
        plot.setDomainGridlineStroke(grid);
        plot.setRangeGridlineStroke(grid);
        return plot;
    }

    private void reloadTimeSeries(TimeSeries series){
        Platform.runLater(()->viewer.getCanvas().getChart().setTitle(chartIndicatorBox.getTimeSeries().getName()));
        mainPlot.setDataset(0, createOHLCDataset(series));
        mainPlot.setRenderer(0, new TaCandlestickRenderer());
        mainPlot.getDomainAxis().setAutoRange(true);
        mainPlot.getRangeAxis().setAutoRange(true);
    }

    /**
     * Creates a {@link CombinedDomainXYPlot combinedDomainXYPlot} with the <tt>plot</tt> as 'main plot' id = 0
     * @param plot the first plot (main plot) that will get 70 percent colorOf the plot area
     * @return a CombinedXYPlot with the <tt>plot</tt> added
     */
    private static CombinedDomainXYPlot createCombinedDomainXYPlot(final XYPlot plot){
        CombinedDomainXYPlot combinedXYPlot = new CombinedDomainXYPlot(plot.getDomainAxis());
        combinedXYPlot.setGap(2);
        combinedXYPlot.add(plot,11);
        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);
        BasicStroke grid = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f,
                                            new float[]{1f}, 0.0f);
        combinedXYPlot.setDomainGridlineStroke(grid);
        combinedXYPlot.setRangeGridlineStroke(grid);

        return combinedXYPlot;
    }

    /**
     * Returns a plot with the given indicators plotted
     * @param chartIndicator org.sjwimmer.tacharting.chart indicators for the plot
     * @return a XYPlot with the indicators as plots
     */
    private XYPlot createSubplotFor(ChartIndicator chartIndicator){

        NumberAxis numberAxis = new NumberAxis();

        numberAxis.setLabelFont(new Font("NumberAxisLabelFont",1,8));
        numberAxis.setTickLabelPaint(Color.GRAY);
        numberAxis.setLabelPaint(Color.GRAY);
        numberAxis.setTickUnit(new NumberTickUnit(0.5));
        numberAxis.setTickLabelFont(new Font("NumberAxisTickFont",1,8));
        numberAxis.setAutoTickUnitSelection(true);
        XYPlot indicatorPlot = new XYPlot(chartIndicator.getDataSet(), mainPlot.getDomainAxis(), numberAxis, chartIndicator.getRenderer());
        TimeSeries series = chartIndicatorBox.getTimeSeries();
        double x = new Minute(Date.from(series.getTick(0).getEndTime().minusDays(50).toInstant())).getFirstMillisecond();
        double y = numberAxis.getLowerBound()+(numberAxis.getUpperBound()+numberAxis.getLowerBound())/1.1;
        //indicatorPlot.setBackgroundPaint(plotBackground);
        indicatorPlot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        return indicatorPlot;
    }

    /**
     * Builds a JFreeChart OHLC dataset from a ta4j time series.
     * @param series a time series
     * @return an Open-High-Low-Close dataset
     */
    private static OHLCDataset createOHLCDataset(TimeSeries series) {
        final int nbTicks = series.getTickCount();

        Date[] dates = new Date[nbTicks];
        double[] opens = new double[nbTicks];
        double[] highs = new double[nbTicks];
        double[] lows = new double[nbTicks];
        double[] closes = new double[nbTicks];
        double[] volumes = new double[nbTicks];

        for (int i = 0; i < nbTicks; i++) {
            Tick tick = series.getTick(i);
            dates[i] = new Date(tick.getEndTime().toEpochSecond() * 1000);
            opens[i] = tick.getOpenPrice().toDouble();
            highs[i] = tick.getMaxPrice().toDouble();
            lows[i] = tick.getMinPrice().toDouble();
            closes[i] = tick.getClosePrice().toDouble();
            volumes[i] = tick.getVolume().toDouble();
        }

        return new DefaultHighLowDataset(series.getName(), dates, highs, lows, opens, closes, volumes);
    }

    /**
     * This function is called when the {@link ChartIndicator Chartindicators} of the underlying
     * {@link ChartIndicatorBox indicatorBox} change (change, new or remove)
     */
    @Override
    public void onChanged(Change<? extends String, ? extends ChartIndicator> change) {

        ChartIndicator indicator;
        final String key = change.getKey();
        if(change.wasRemoved()){
            indicator = change.getValueRemoved();
            if(indicator.isSubchart()){
                currentSubplots.remove(key);
                plotSubPlots(currentSubplots);
            } else {
                currentOverlays.remove(key);
                plotOverlays(currentOverlays);
            }
        }
        // wasAdded = wasRemoved = true is possible
        if(change.wasAdded()) {
            indicator = change.getValueAdded();
            if(indicator.isSubchart()){
                currentSubplots.add(key);
                plotSubPlots(currentSubplots);
            } else {
                currentOverlays.add(key);
                plotOverlays(currentOverlays);
            }
        }
    }

    /**
     * Custom CandleStickRenderer to display filled red/green candles
     */
    static class TaCandlestickRenderer extends CandlestickRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Paint getItemPaint(int row, int column) {

            //determine up or down candle
            XYDataset dataset = getPlot().getDataset();
            OHLCDataset highLowData = (OHLCDataset) dataset;

            Number yOpen = highLowData.getOpen(row, column);
            Number yClose = highLowData.getClose(row, column);
            boolean isUpCandle = yClose.doubleValue() > yOpen.doubleValue();

            //return the same color as that used to fill the candle
            if (isUpCandle) {
                return getUpPaint();
            } else {
                return getDownPaint();
            }
        }
    }
}



