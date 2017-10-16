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

import chart.view.TaChartMenuBar;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.Tick;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.Minute;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class TaChart extends ApplicationFrame implements ChartMouseListener {

    private TimeSeries series;
    private TaChartIndicatorBox chartIndicatorBox;

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
    private org.jfree.chart.ChartPanel chartPanel;
    private Crosshair xCrosshair;
    private Crosshair yCrosshair;

    private Map<TradingRecord, List<Marker>> mapTradingRecordMarker;
    private List<XYPlot> currentSubPlots;

    /**
     * Constructor
     * @param series a ta4j time series
     * @param box a TaChartIndicatorBox
     */
    public TaChart(TimeSeries series, TaChartIndicatorBox box){
        this(series,box,false);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
    }

    public TaChart(TimeSeries series, TaChartIndicatorBox box, boolean darkTheme){
        super("TaCharting "+series.getName());
        mapTradingRecordMarker = new HashMap<>();
        if (darkTheme){
            setDarkTheme();
        }
        this.series = series;
        this.chartIndicatorBox = box;
        prepare();

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
        JMenuBar menuBar = new TaChartMenuBar(chartIndicatorBox,this);
        setJMenuBar(menuBar);

        this.chart = new JFreeChart(series.getName(), combinedXYPlot);
        this.chart.setBackgroundPaint(chartBackground);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.addChartMouseListener(this);
        this.chartPanel.addOverlay(createCrosshairOverlay());
        this.chartPanel.setBackground(panelBackground);
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setItemFont(new Font("Arial", 1, 8));
        legend.setItemPaint(legendItemPaint);
        legend.setBackgroundPaint(legendBackground);

        setContentPane(chartPanel);
        setBackground(frameBackground);
        pack();
    }

    // searching for a better way... currently recreate all because it is not possible to clean remove a data set
    public void rep(){
        Dimension frameSize = getSize();
        Color bg = chartPanel.getBackground();
        Color frameBg= getBackground();
        Paint chartBg = chart.getBackgroundPaint();
        LegendTitle chartLegend = chart.getLegend();
        Paint legendBg = chartLegend.getBackgroundPaint();
        Paint legendItemPaint = chartLegend.getItemPaint();
        Font legendItemFont = chartLegend.getItemFont();


        this.chart = new JFreeChart(series.getName(), combinedXYPlot);
        this.chart.setBackgroundPaint(chartBg);
        this.chartPanel = new ChartPanel(chart);
        this.chartPanel.addChartMouseListener(this);
        this.chartPanel.addOverlay(createCrosshairOverlay());
        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setItemFont(legendItemFont);
        legend.setBackgroundPaint(legendBg);
        legend.setItemPaint(legendItemPaint);

        setBackground(frameBg);
        setSize(frameSize);
        setContentPane(chartPanel);
        revalidate();
    }

    //TODO summarize both methods to make sure that plotOverlay is called befor plotSubplots

    /**
     * plots or removes the trading record
     * @param record the trading record
     * @param on adds the record if true, else the record will be removed from plot
     */
    public void plotTradingRecord(TradingRecord record, boolean on){
        if (on){
            this.addEntryExitSignals(record);
        } else  {
            this.removeEntryExitSignals(record);

        }

    }

    private void removeEntryExitSignals(TradingRecord record) {
        List<Marker> markers = this.mapTradingRecordMarker.get(record);
        for(Marker m: markers){
            mainPlot.removeDomainMarker(m);
        }
        this.mapTradingRecordMarker.remove(record);
    }

    /**Adds entry and exits signals to the main plot.
     * @param record the trading record
     * */
    private void addEntryExitSignals(TradingRecord record){
        List<Trade> trades = record.getTrades();
        Order.OrderType orderType = record.getLastExit().getType().complementType();
        List<Marker> markers = new ArrayList<>();
        Color entryColor = Color.GREEN;
        Color exitColor = Color.RED;
        RectangleAnchor entryAnchor = RectangleAnchor.TOP_LEFT;
        RectangleAnchor exitAnchor = RectangleAnchor.BOTTOM_RIGHT;
        if(orderType == Order.OrderType.SELL) {
            entryColor = Color.RED;
            exitColor = Color.GREEN;
        }
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
        this.mapTradingRecordMarker.put(record,markers);
    }

    //TODO summarize both methods to make sure that plotOverlay is called befor plotSubplots
    // create active overlays
    public void plotOverlays(List<String> indicatorIdentifiers) {
        List<TaChartIndicator> overlays = new ArrayList<>();
        for (String identifier: indicatorIdentifiers){
            TaChartIndicator taChartIndicator = chartIndicatorBox.getChartIndicator(identifier);
            overlays.add(taChartIndicator);
        }

        Range domainRange = this.mainPlot.getDomainAxis().getRange();
        Range valueRange = this.mainPlot.getRangeAxis().getRange();
        this.mainPlot = createMainPlot(this.candlestickData, overlays);
        this.mainPlot.getDomainAxis().setRange(domainRange);
        this.mainPlot.getRangeAxis().setRange(valueRange);
        this.combinedXYPlot = recreateCombinedDomainXYPlot();
        rep();
    }

    // create active subplots
    public void plotSubPlots(List<String> indicatorIdentifiers){
        List<TaChartIndicator> subPlots = new ArrayList<>();
        for (String identifier: indicatorIdentifiers){
            TaChartIndicator taChartIndicator = chartIndicatorBox.getChartIndicator(identifier);
            subPlots.add(taChartIndicator);
        }

        this.combinedXYPlot = createCombinedDomainXYPlot(mainPlot, subPlots);
        rep();
    }

    /**
     * Creating the xyPlot for the base candlestick chart
     * @param dataset a XYDataset for the candlestick
     */
    private XYPlot createMainPlot(XYDataset dataset, List<TaChartIndicator> overlays){

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
        for(TaChartIndicator taChartIndicator: overlays) {
            int anonymID = plot.getDatasetCount();
            plot.setDataset(anonymID, taChartIndicator.getDataSet());
            plot.mapDatasetToRangeAxis(mainPlot.getDatasetCount(), 0);
            plot.setRenderer(anonymID, taChartIndicator.getRenderer());
            numberAxis.setAutoRangeIncludesZero(false);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        }
        return plot;
    }



    @Override
    public void chartMouseClicked(ChartMouseEvent chartMouseEvent) {
        Rectangle2D dataArea = this.chartPanel.getScreenDataArea();
        JFreeChart chart = chartMouseEvent.getChart();
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();

        List<XYPlot> subplots = (List<XYPlot>) plot.getSubplots();

        ValueAxis xAxis = subplots.get(0).getDomainAxis();

        double x = xAxis.java2DToValue(chartMouseEvent.getTrigger().getX(), dataArea,
                RectangleEdge.BOTTOM);
        double y = DatasetUtilities.findYValue(((XYPlot)plot.getSubplots().get(0)).getDataset(), 0, x);

        XYTextAnnotation an = new XYTextAnnotation(x+" "+y, x, y);
        an.setBackgroundPaint(Color.WHITE);
        //mainPlot.addAnnotation(an);

    }

    private CombinedDomainXYPlot createCombinedDomainXYPlot(XYPlot plot, List<TaChartIndicator> subplots){
        // create the combined xy plot for this and the subplots
        this.currentSubPlots = new ArrayList<>();
        CombinedDomainXYPlot combinedXYPlot = new CombinedDomainXYPlot(plot.getDomainAxis());
        combinedXYPlot.setGap(2);
        combinedXYPlot.add(plot,11);
        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedXYPlot.setBackgroundPaint(plotBackground);
        for(TaChartIndicator taChartIndicator: subplots){
            XYPlot subPlot = createSubplotforIndicators(taChartIndicator);
            combinedXYPlot.add(subPlot);
            currentSubPlots.add(subPlot);
        }

        return combinedXYPlot;
    }

    private CombinedDomainXYPlot recreateCombinedDomainXYPlot(){
        CombinedDomainXYPlot combinedXYPlot = new CombinedDomainXYPlot(mainPlot.getDomainAxis());
        combinedXYPlot.setGap(2);
        combinedXYPlot.add(mainPlot,11);
        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);
        combinedXYPlot.setBackgroundPaint(plotBackground);
        //TODO: loose mapping between TaChartIndicator and his plot
        for(XYPlot p: currentSubPlots){
            combinedXYPlot.add(p);
        }

        return combinedXYPlot;
    }

    /**
     * Returns a plot with the given indicators plotted
     * @param taChartIndicator chart indicators for the plot
     * @return a XYPlot with the indicators as plots
     */
    private XYPlot createSubplotforIndicators(TaChartIndicator taChartIndicator){

        NumberAxis numberAxis = new NumberAxis();

        numberAxis.setLabelFont(new Font("NumberAxisLabelFont",1,8));
        numberAxis.setTickLabelPaint(Color.GRAY);
        numberAxis.setLabelPaint(Color.GRAY);
        numberAxis.setTickUnit(new NumberTickUnit(0.5));
        numberAxis.setTickLabelFont(new Font("NumberAxisTickFont",1,8));
        numberAxis.setAutoTickUnitSelection(true);
        XYPlot indicatorPlot = new XYPlot(taChartIndicator.getDataSet(), mainPlot.getDomainAxis(), numberAxis, taChartIndicator.getRenderer());

        double x = new Minute(Date.from(series.getTick(0).getEndTime().minusDays(50).toInstant())).getFirstMillisecond();
        double y = numberAxis.getLowerBound()+(numberAxis.getUpperBound()+numberAxis.getLowerBound())/1.1;
        XYTextAnnotation annotation = new XYTextAnnotation(taChartIndicator.getGeneralName(), x, y);
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
    private CrosshairOverlay createCrosshairOverlay(){
        TaChrosshairOverlay crosshairOverlay = new TaChrosshairOverlay();

        this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        this.xCrosshair.setLabelPaint(Color.GRAY);
        this.xCrosshair.setLabelVisible(true);
        this.xCrosshair.setLabelBackgroundPaint(Color.WHITE);
        this.xCrosshair.setLabelGenerator(new TaXCrosshairLabelGenerator());
        this.yCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
        this.yCrosshair.setLabelPaint(Color.GRAY);

        //TODO: shifts if subplots are added. Find way to calculate y+(sizeOfSubplots)*numSublots
        this.yCrosshair.setLabelVisible(true);
        this.yCrosshair.setLabelBackgroundPaint(Color.WHITE);
        this.yCrosshair.setVisible(false);
        crosshairOverlay.addDomainCrosshair(xCrosshair);
        crosshairOverlay.addRangeCrosshair(yCrosshair);
        return crosshairOverlay;
    }

    /**
     * Builds a JFreeChart OHLC dataset from a ta4j time series.
     * @param series a time series
     * @return an Open-High-Low-Close dataset
     * from  ta4j/ta4j-examples/src/main/java/ta4jexamples/indicators/CandlestickChart.java
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
     * Create one or several XYDatasets to plot an indicator on the mainPlot
     * @param taChartIndicator an TaChartIndicator that describes the attributes of the indicator(s) that should be plotted
     * @return id of the renderer and dataset for the main plot
     */
    private int createIdForOverlay(TaChartIndicator taChartIndicator){
        int id = mainPlot.getDatasetCount();
        return id;
    }

    public class MyCandlestickRenderer extends CandlestickRenderer {

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

    @Override
    public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
        Rectangle2D dataArea = this.chartPanel.getScreenDataArea();
        JFreeChart chart = chartMouseEvent.getChart();
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();

        List<XYPlot> subplots = (List<XYPlot>) plot.getSubplots();

        DateAxis xAxis = (DateAxis) subplots.get(0).getDomainAxis();

        double x = xAxis.java2DToValue(chartMouseEvent.getTrigger().getX(), dataArea, RectangleEdge.BOTTOM);
        double y = DatasetUtilities.findYValue(((XYPlot)plot.getSubplots().get(0)).getDataset(), 0, x);

        this.xCrosshair.setValue(x);
        this.yCrosshair.setValue(y);

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
}



