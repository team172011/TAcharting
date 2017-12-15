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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.StackPane;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.overlay.CrosshairOverlayFX;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.time.Minute;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.ta4j.core.*;
import org.ta4j.core.Tick;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class TaChart extends StackPane implements ChartMouseListenerFX, MapChangeListener<String, ChartIndicator> {

    private TimeSeries series;
    private ChartIndicatorBox chartIndicatorBox;

    // gui and plotting
    private XYDataset candlestickData;
    private Color plotBackground = Color.WHITE; // default colors for white theme
    private Color panelBackground = Color.WHITE;
    private Color frameBackground = Color.WHITE;
    private Color chartBackground = Color.WHITE;
    private Color legendBackground = Color.WHITE;
    private Color subPlotNames = Color.BLACK;
    private Color legendItemPaint = Color.BLACK;

    private JFreeChart chart;
    private CombinedDomainXYPlot combinedXYPlot;
    private XYPlot mainPlot;
    private Crosshair xCrosshair;
    private Crosshair yCrosshair;

    private Map<TradingRecord, List<Marker>> mapTradingRecordMarker;
    private List<XYPlot> currentSubPlots;

    private ObservableList<String> currentOverlays = FXCollections.observableArrayList();
    private ObservableList<String> currentSubplots = FXCollections.observableArrayList();

    private ChartViewer viewer;

    private TaChrosshairOverlay crosshairOverlay;
    /**
     * Constructor
     * @param box a ChartIndicatorBox
     */
    public TaChart(ChartIndicatorBox box){
        this(box,false);

    }

    /**
     * Constructor
     * @param box a ChartIndicatorBox
     * @param darkTheme true if dark theme should be used
     */
    public TaChart(ChartIndicatorBox box, boolean darkTheme){
        mapTradingRecordMarker = new HashMap<>();
        if (darkTheme){
            setDarkTheme();
        }
        this.series = box.getTimeSeries();
        this.chartIndicatorBox = box;
        chartIndicatorBox.getChartIndicatorMap().addListener(this);
        prepare();
    }

    public ChartIndicatorBox getChartIndicatorBox() {
        return chartIndicatorBox;
    }

    private void setDarkTheme() {
        plotBackground = Color.BLACK;
        panelBackground = Color.BLACK;
        frameBackground = Color.BLACK;
        chartBackground = Color.BLACK;
        legendBackground = Color.BLACK;
        subPlotNames = Color.WHITE;
        legendItemPaint = Color.WHITE;
    }

    private void prepare(){
        this.candlestickData = createOHLCDataset(series);
        this.mainPlot = createMainPlot(this.candlestickData, new ArrayList<>());
        this.combinedXYPlot = createCombinedDomainXYPlot(mainPlot, new ArrayList<>());
        this.currentSubPlots = new ArrayList<>();

        this.chart = new JFreeChart(series.getName(), combinedXYPlot);
        //this.chart.setBackgroundPaint(chartBackground);
        this.viewer = new ChartViewer(chart);

        this.viewer.addChartMouseListener(this);
        getChildren().add(viewer);
        crosshairOverlay = createCrosshairOverlay();
        //this.viewer.setBackground(panelBackground);
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setItemFont(new Font("Arial", 1, 8));
        legend.setItemPaint(legendItemPaint);
        legend.setBackgroundPaint(legendBackground);
        //viewer.setBackground(frameBackground);

        Platform.runLater(() -> {
            this.viewer.getCanvas().addOverlay(crosshairOverlay);
        });

    }

    // searching for a better way...recreate all charts and subplots because it is not possible to clean
    // remove a data set from an existing chart
    public void rep(){
        viewer.removeChartMouseListener(this);
        viewer.getCanvas().removeOverlay(crosshairOverlay);

        // create new JFreeChart and ChartViewer with possible new plots
        JFreeChart newChart = new JFreeChart(this.series.getName(), this.combinedXYPlot);
        newChart.setBackgroundPaint(chart.getBackgroundPaint());

        LegendTitle legend = newChart.getLegend();
        legend.setPosition(chart.getLegend().getPosition());
        legend.setItemFont(chart.getLegend().getItemFont());
        legend.setBackgroundPaint(chart.getBackgroundPaint());
        legend.setItemPaint(legendItemPaint);


        ChartViewer newViewer = new ChartViewer(newChart);
        newViewer.getCanvas().getChart().setBackgroundPaint(viewer.getCanvas().getChart().getBackgroundPaint());
        newViewer.addChartMouseListener(this);
        this.crosshairOverlay = createCrosshairOverlay();

        getChildren().remove(viewer);
        chart = newChart;
        viewer = newViewer;
        getChildren().add(viewer);


        Platform.runLater(() -> {
            this.viewer.getCanvas().addOverlay(crosshairOverlay);
        });
    }

    /**
     * plots or removes the trading record
     * @param record the trading record
     * @param on adds the record if true, else the record will be removed from plot
     */
    public void plotTradingRecord(TradingRecord record, boolean on){
        if (on){
            addEntryExitSignals(record);
        } else  {
            removeEntryExitSignals(record);

        }

    }

    private void removeEntryExitSignals(TradingRecord record) {
        List<Marker> markers = this.mapTradingRecordMarker.get(record);
        if (markers!= null) {
            for (Marker m : markers) {
                mainPlot.removeDomainMarker(m);
            }
            this.mapTradingRecordMarker.remove(record);
        }
    }

    /**
     * Adds entry and exits signals to the main plot.
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

        for(Trade trade: trades){
            double entry = new Minute(Date.from(series.getTick(trade.getEntry().getIndex()).getEndTime().toInstant())).getFirstMillisecond();
            double exit = new Minute(Date.from(series.getTick(trade.getExit().getIndex()).getEndTime().toInstant())).getFirstMillisecond();

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
     * Plots the corresponding indicators of the list of identifiers as overlays
     * @param indicatorIdentifiers a list of identifiers e.g. "EMAIndicator_1"
     */
    public void plotOverlays(List<String> indicatorIdentifiers) {
        List<ChartIndicator> overlays = new ArrayList<>();
        for (String identifier: indicatorIdentifiers){
            ChartIndicator chartIndicator = chartIndicatorBox.getChartIndicator(identifier);
            overlays.add(chartIndicator);
        }

        Range domainRange = this.mainPlot.getDomainAxis().getRange();
        Range valueRange = this.mainPlot.getRangeAxis().getRange();
        this.mainPlot = createMainPlot(this.candlestickData, overlays);
        this.mainPlot.getDomainAxis().setRange(domainRange);
        this.mainPlot.getRangeAxis().setRange(valueRange);
        this.combinedXYPlot = recreateCombinedDomainXYPlot();
        rep();
    }

    /**
     * Plots the corresponding indicators of the list of identifiers as subplots
     * @param indicatorIdentifiers a list of identifiers e.g. "MACDIndicator_1"
     */
    public void plotSubPlots(List<String> indicatorIdentifiers){
        List<ChartIndicator> subPlots = new ArrayList<>();
        for (String identifier: indicatorIdentifiers){
            ChartIndicator chartIndicator = chartIndicatorBox.getChartIndicator(identifier);
            subPlots.add(chartIndicator);
        }

        this.combinedXYPlot = createCombinedDomainXYPlot(mainPlot, subPlots);
        rep();
    }

    /**
     * Creating the xyPlot for the base candlestick chart
     * @param dataset a XYDataset for the candlestick
     */
    private XYPlot createMainPlot(XYDataset dataset, List<ChartIndicator> overlays){

        TaChart.MyCandlestickRenderer renderer = new TaChart.MyCandlestickRenderer();
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
        plot.setBackgroundPaint(plotBackground);
        float dash[]={1.0f};
        BasicStroke grid = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f, dash, 0.0f);
        plot.setDomainGridlineStroke(grid);
        plot.setRangeGridlineStroke(grid);
        for(ChartIndicator chartIndicator : overlays) {
            int anonymID = plot.getDatasetCount();
            plot.setDataset(anonymID, chartIndicator.getDataSet());
            plot.mapDatasetToRangeAxis(mainPlot.getDatasetCount(), 0);
            plot.setRenderer(anonymID, chartIndicator.getRenderer());
            numberAxis.setAutoRangeIncludesZero(false);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        }
        return plot;
    }

    private CombinedDomainXYPlot createCombinedDomainXYPlot(XYPlot plot, List<ChartIndicator> subplots){
        // create the combined xy plot for this and the subplots
        this.currentSubPlots = new ArrayList<>();
        CombinedDomainXYPlot combinedXYPlot = new CombinedDomainXYPlot(plot.getDomainAxis());
        combinedXYPlot.setGap(2);
        combinedXYPlot.add(plot,11);
        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedXYPlot.setBackgroundPaint(plotBackground);
        for(ChartIndicator chartIndicator : subplots){
            XYPlot subPlot = createSubplotforIndicators(chartIndicator);
            combinedXYPlot.add(subPlot);
            currentSubPlots.add(subPlot);
        }

        return combinedXYPlot;
    }

    private CombinedDomainXYPlot recreateCombinedDomainXYPlot(){
        CombinedDomainXYPlot combinedXYPlot = new CombinedDomainXYPlot(mainPlot.getDomainAxis());
        combinedXYPlot.setGap(0);
        combinedXYPlot.add(mainPlot,11);
        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedXYPlot.setBackgroundPaint(plotBackground);
        //TODO: loose mapping between ChartIndicator and his plot
        for(XYPlot p: currentSubPlots){
            combinedXYPlot.add(p);
        }

        return combinedXYPlot;
    }

    /**
     * Returns a plot with the given indicators plotted
     * @param chartIndicator chart indicators for the plot
     * @return a XYPlot with the indicators as plots
     */
    private XYPlot createSubplotforIndicators(ChartIndicator chartIndicator){

        NumberAxis numberAxis = new NumberAxis();

        numberAxis.setLabelFont(new Font("NumberAxisLabelFont",1,8));
        numberAxis.setTickLabelPaint(Color.GRAY);
        numberAxis.setLabelPaint(Color.GRAY);
        numberAxis.setTickUnit(new NumberTickUnit(0.5));
        numberAxis.setTickLabelFont(new Font("NumberAxisTickFont",1,8));
        numberAxis.setAutoTickUnitSelection(true);
        XYPlot indicatorPlot = new XYPlot(chartIndicator.getDataSet(), mainPlot.getDomainAxis(), numberAxis, chartIndicator.getRenderer());
        double x = new Minute(Date.from(series.getTick(0).getEndTime().minusDays(50).toInstant())).getFirstMillisecond();
        double y = numberAxis.getLowerBound()+(numberAxis.getUpperBound()+numberAxis.getLowerBound())/1.1;
        XYTextAnnotation annotation = new XYTextAnnotation(chartIndicator.getGeneralName(), x, y);
        annotation.setFont(new Font("SansSerif", Font.BOLD, 6));
        annotation.setPaint(subPlotNames);
        annotation.setOutlineVisible(true);
        annotation.setTextAnchor(TextAnchor.TOP_LEFT);
        indicatorPlot.addAnnotation(annotation);
        indicatorPlot.setBackgroundPaint(plotBackground);
        indicatorPlot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);

        return indicatorPlot;
    }

    /**
     * Create the crosshair overlay with a custom paint procedure for main plot
     * @return an crosshair overlay for the main plot and sub plots
     */
    private TaChrosshairOverlay createCrosshairOverlay(){
        TaChrosshairOverlay crosshairOverlay = new TaChrosshairOverlay();

        this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        this.xCrosshair.setLabelPaint(Color.GRAY);
        this.xCrosshair.setLabelVisible(true);
        this.xCrosshair.setLabelBackgroundPaint(Color.WHITE);
        this.xCrosshair.setLabelGenerator(new TaXCrosshairLabelGenerator());

        //TODO: shifts if subplots are added. Find way to calculate y+(sizeOfSubplots)*numSublots
        this.yCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        this.yCrosshair.setLabelPaint(Color.GRAY);
        this.yCrosshair.setLabelVisible(true);
        this.yCrosshair.setLabelBackgroundPaint(Color.WHITE);
        this.yCrosshair.setVisible(true);

        crosshairOverlay.addDomainCrosshair(xCrosshair);
        crosshairOverlay.addRangeCrosshair(yCrosshair);
        return crosshairOverlay;
    }

    /**
     * Builds a JFreeChart OHLC dataset from a ta4j time series.
     * @param series a time series
     * @return an Open-High-Low-Close dataset
     * See ta4j/ta4j-examples/src/main/java/ta4jexamples/indicators/CandlestickChart.java
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

    @Override
    public void chartMouseMoved(ChartMouseEventFX event) {
        if (viewer.getRenderingInfo() != null) {
            Rectangle2D dataArea = viewer.getRenderingInfo().getPlotInfo().getDataArea();
            JFreeChart chart = event.getChart();
            CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();

            List<XYPlot> subplots = (List<XYPlot>) plot.getSubplots();

            DateAxis xAxis = (DateAxis) subplots.get(0).getDomainAxis();
            double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, RectangleEdge.BOTTOM);
            if (!xAxis.getRange().contains(x)) {
                x = Double.NaN;
            }
            double y = DatasetUtils.findYValue(((XYPlot) plot.getSubplots().get(0)).getDataset(), 0, x);

            this.xCrosshair.setValue(x);
            this.yCrosshair.setValue(y);
        } else {
            this.xCrosshair.setValue(Double.NaN);
            this.yCrosshair.setValue(Double.NaN);
        }

    }


    @Override
    public void chartMouseClicked(ChartMouseEventFX chartMouseEvent) {
        //TODO remove this content ?
        /*Rectangle2D dataArea = this.chartPanel.getScreenDataArea();
        JFreeChart chart = chartMouseEvent.getChart();
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();

        List<XYPlot> subplots = (List<XYPlot>) plot.getSubplots();

        ValueAxis xAxis = subplots.get(0).getDomainAxis();

        double x = xAxis.java2DToValue(chartMouseEvent.getTrigger().getX(), dataArea,
                RectangleEdge.BOTTOM);
        double y = DatasetUtils.findYValue(((XYPlot)plot.getSubplots().get(0)).getDataset(), 0, x);

        XYTextAnnotation an = new XYTextAnnotation(x+" "+y, x, y);
        an.setBackgroundPaint(Color.WHITE);
        //mainPlot.addAnnotation(an);
*/
    }

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
     * Custom CrosshairLabelGenerator to display the date on the crosshair
     */
    class TaXCrosshairLabelGenerator implements CrosshairLabelGenerator{

        @Override
        public String generateLabel(Crosshair crosshair) {
            double value = crosshair.getValue();
            long itemLong = (long) (value);
            Date itemDate = new Date(itemLong);
            return new SimpleDateFormat().format(itemDate);
        }
    }


    class TaChrosshairOverlay extends CrosshairOverlayFX {


        @Override
        public void paintOverlay(Graphics2D g2, ChartCanvas chartCanvas) {
            if (chartCanvas.getRenderingInfo() != null) {
                Shape savedClip = g2.getClip();
                /**use the complete ScreenDataArea for X-Crosshair paint*/
                Rectangle2D dataArea = chartCanvas.getRenderingInfo().getPlotInfo().getDataArea();

                g2.clip(dataArea);
                JFreeChart chart = chartCanvas.getChart();
                CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();
                List<XYPlot> subplots = plot.getSubplots();

                // paint domain crosshair -> "standard way" subplot(0) = main plot
                ValueAxis xAxis = subplots.get(0).getDomainAxis();
                RectangleEdge xAxisEdge = subplots.get(0).getDomainAxisEdge();
                Iterator iterator = getDomainCrosshairs().iterator();
                while (iterator.hasNext()) {
                    Crosshair ch = (Crosshair) iterator.next();
                    if (ch.isVisible()) {
                        double x = ch.getValue();
                        double xx = xAxis.valueToJava2D(x, dataArea, xAxisEdge);
                        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                            drawVerticalCrosshair(g2, dataArea, xx, ch);
                        } else {
                            drawHorizontalCrosshair(g2, dataArea, xx, ch);
                        }
                    }
                }


                RectangleEdge yAxisEdge = subplots.get(0).getRangeAxisEdge();
                ValueAxis yAxis = subplots.get(0).getRangeAxis();
                iterator = this.getRangeCrosshairs().iterator();

                /**Use just the subplot(0)=mainPlot ScreenDataArea for Y-Crosshair paint*/
                Rectangle2D subDataArea = getMainPlotScreenDataArea(chartCanvas);
                while (iterator.hasNext()) {
                    Crosshair ch = (Crosshair) iterator.next();
                    if (ch.isVisible()) {
                        double y = ch.getValue();

                        double yy = yAxis.valueToJava2D(y, subDataArea, yAxisEdge);
                        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                            drawHorizontalCrosshair(g2, dataArea, yy, ch);
                        } else {
                            drawVerticalCrosshair(g2, dataArea, yy, ch);
                        }
                    }
                }
                g2.setClip(savedClip);
            }
        }

        public Rectangle2D getMainPlotScreenDataArea(ChartCanvas chartPanel){
            Rectangle2D area = chartPanel.getRenderingInfo().getPlotInfo().getSubplotInfo(0).getDataArea();
            //double x = area.getX()* chartPanel.getScaleX()+chartPanel.getInsets().left;
            //double y = area.getY()* chartPanel.getScaleY()+chartPanel.getInsets().top;
            //double w = area.getWidth()*chartPanel.getScaleX();
            //double h = area.getHeight()*chartPanel.getScaleY();
            //return new Rectangle2D.Double(x, y, w, h);
            return area;
        }

    }

    class MyCandlestickRenderer extends CandlestickRenderer {

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



