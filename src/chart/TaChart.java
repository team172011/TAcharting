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
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.ShapeUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;


public class TaChart extends ApplicationFrame implements ChartMouseListener, ActionListener {

    private TimeSeries series;
    private List<TaChartIndicator> taChartIndicators;
    private TradingRecord record;
    static TaChartIndicatorBox chartIndicatorBox = new TaChartIndicatorBox();

    // gui and plotting
    private JMenuBar menuBar = new JMenuBar();
    JMenu settings = new JMenu("Settings");
    JMenu trading = new JMenu("TradingRecord");
    TaCheckBoxItem showRecord = new TaCheckBoxItem("Show record");

    private CombinedDomainXYPlot combinedXYPlot;
    private XYPlot mainPlot;
    private org.jfree.chart.ChartPanel chartPanel;
    private Crosshair xCrosshair;
    private Crosshair yCrosshair;

    // mapping
    private Map<JMenuItem, Integer> mapEntryToChart = new HashMap<>();
    private Map<JMenuItem, XYPlot> mapEntryToXYPlot = new HashMap<>();
    private List<Marker> markers;

    public TaChart(TimeSeries series, TaChartIndicatorBox box){
        this(series, null, box);
    }


    public TaChart(TimeSeries series, TradingRecord record, TaChartIndicatorBox box){
        this(series,record,box.getTaChartIndicatorList());
    }

    /**
     * Constructor
     * @param series a ta4j time series
     * @param record a ta4j trading record can be null
     * @param indicatorList a list of ChartIndicators
     */
    public TaChart(TimeSeries series, TradingRecord record, List<TaChartIndicator> indicatorList){
        super(series.getName());
        this.series = series;
        this.record = record;
        this.taChartIndicators = indicatorList;
        this.markers = new ArrayList<>();
        prepare();
    }

    public void prepare(){

        createMainPlot(series);
        createMenuEntriesAndSubPlots();

        JFreeChart chart = new JFreeChart(series.getName(), combinedXYPlot);
        chart.setBackgroundPaint(Color.BLACK);

        chartPanel = new ChartPanel(chart);
        chartPanel.addChartMouseListener(this);
        chartPanel.addOverlay(createCrosshairOverlay());

        LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setBackgroundPaint(Color.BLACK);
        legend.setItemPaint(Color.GRAY);
        legend.setItemFont(new Font("Arial", 1, 7));

        setContentPane(chartPanel);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Creating the xyPlot for the base candlestick chart
     * @param series a ta4j time series
     */
    private void createMainPlot(TimeSeries series){

        TaChart.MyCandlestickRenderer renderer = new TaChart.MyCandlestickRenderer();
        renderer.setCandleWidth(2);
        renderer.setDrawVolume(true);

        NumberAxis numberAxis = new NumberAxis("Price");
        numberAxis.setTickLabelPaint(Color.GRAY);
        numberAxis.setLabelPaint(Color.GRAY);

        DateAxis dateAxis = new DateAxis("Date");
        dateAxis.setTickLabelPaint(Color.GRAY);
        dateAxis.setLabelPaint(Color.GRAY);

        XYDataset dataset = createOHLCDataset(series);

        // XYPlot with candlesticks
        mainPlot = new XYPlot(dataset, dateAxis, numberAxis, renderer);
        mainPlot.setOrientation(PlotOrientation.VERTICAL);
        mainPlot.setBackgroundPaint(Color.BLACK);
        mainPlot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

        float dash[]={1.0f};
        BasicStroke grid = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,10.0f, dash, 0.0f);
        mainPlot.setDomainGridlineStroke(grid);
        mainPlot.setRangeGridlineStroke(grid);

        // create the combined xy plot for this and the subplots
        combinedXYPlot = new CombinedDomainXYPlot(mainPlot.getDomainAxis());
        combinedXYPlot.setGap(2);
        combinedXYPlot.add(mainPlot,11);
        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);
    }

    /**
     * create the menu entries and subplots for the indicators that are flagged as isSubplot == true
     */
    private void createMenuEntriesAndSubPlots(){

        JMenu indicatorsEntry = new JMenu("Indicators");
        List<JMenuItem> categoriesEntry = new ArrayList<>();
        for(TaTypes.categories c : TaTypes.categories.values()){
            indicatorsEntry.add(c.getMenueElement());
        }

        for(TaChartIndicator ci : this.taChartIndicators){
            ci.getMenuEntry().addActionListener(this);
            if (ci.isSubchart()) {
                XYPlot subPlot = createSubplotforIndicators(ci);
                this.mapEntryToXYPlot.put(ci.getMenuEntry(), subPlot); // mapping between dataset id and menue entry
            }
            else {
                int id = setChartTimeSeriesForChartIndicator(ci);
                this.mapEntryToChart.put(ci.getMenuEntry(), id); // mapping between dataset id and menue entry
            }
            indicatorsEntry.getItem(ci.getCategory().getId()).add(ci.getMenuEntry());
        }

        this.trading.add(showRecord);
        showRecord.addActionListener(this);

        this.menuBar.add(settings);
        this.menuBar.add(indicatorsEntry);
        this.menuBar.add(trading);
        setJMenuBar(menuBar);
    }

    /**
     * Returns a plot with the given indicators plotted
     * @param taChartIndicator chart indicators for the plot
     * @return a XYPlot with the indicators as plots
     */
    private XYPlot createSubplotforIndicators(TaChartIndicator taChartIndicator){

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        for(int index = 0; index< taChartIndicator.getIndicatorsCount(); index++){

            Indicator<Decimal> indicator = taChartIndicator.getIndicator(index);
            if (!indicator.getTimeSeries().equals(this.series))
                throw new IllegalStateException("Time series of indicator is another than time series of main chart!");
            org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(taChartIndicator.getName(index));
            for(int i = 0; i<indicator.getTimeSeries().getTickCount(); i++){
                Tick t = indicator.getTimeSeries().getTick(i);
                chartTimeSeries.add(new Second(new Date(t.getEndTime().toEpochSecond() * 1000)), indicator.getValue(i).toDouble());
            }
            renderer.setSeriesPaint(index, taChartIndicator.getPaint(index));
            renderer.setSeriesShape(index, ShapeUtilities.createUpTriangle(0.1f));
            dataset.addSeries(chartTimeSeries);
        }

        NumberAxis numberAxis = new NumberAxis();

        numberAxis.setLabelFont(new Font("NumberAxisLabelFont",1,8));
        numberAxis.setTickLabelPaint(Color.GRAY);
        numberAxis.setLabelPaint(Color.GRAY);
        numberAxis.setTickUnit(new NumberTickUnit(0.5));
        numberAxis.setTickLabelFont(new Font("NumberAxisTickFont",1,8));
        numberAxis.setAutoTickUnitSelection(true);
        XYPlot indicatorPlot = new XYPlot(dataset, mainPlot.getDomainAxis(), numberAxis, renderer);


        double x = new Minute(Date.from(series.getTick(0).getEndTime().minusDays(50).toInstant())).getFirstMillisecond();
        double y = numberAxis.getLowerBound()+(numberAxis.getUpperBound()+numberAxis.getLowerBound())/1.1;
        XYTextAnnotation annotation = new XYTextAnnotation(taChartIndicator.getGeneralName(), x, y);
        annotation.setFont(new Font("SansSerif", Font.BOLD, 6));
        annotation.setPaint(Color.WHITE);
        annotation.setOutlineVisible(true);
        annotation.setBackgroundPaint(Color.BLACK);
        annotation.setTextAnchor(TextAnchor.TOP_LEFT);

        indicatorPlot.addAnnotation(annotation);

        indicatorPlot.setBackgroundPaint(Color.BLACK);
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
    private int setChartTimeSeriesForChartIndicator(TaChartIndicator taChartIndicator){
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        // add indicator data to dataset
        for(int index = 0; index< taChartIndicator.getIndicatorsCount(); index++){
            Indicator<Decimal> indicator = taChartIndicator.getIndicator(index);
            org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(taChartIndicator.getName(index));
            if (!indicator.getTimeSeries().equals(this.series))
                throw new IllegalStateException("Time series of indicator is another than time series of main chart!");
            for(int i = 0; i<indicator.getTimeSeries().getTickCount(); i++){
                Tick t = indicator.getTimeSeries().getTick(i);
                chartTimeSeries.add(new Second(new Date(t.getEndTime().toEpochSecond() * 1000)), indicator.getValue(i).toDouble());
            }
            renderer.setSeriesPaint(index, taChartIndicator.getPaint(index));
            renderer.setSeriesShape(index, ShapeUtilities.createUpTriangle(0.1f));
            renderer.setSeriesVisible(index, false);
            dataset.addSeries(chartTimeSeries);
        }

        int id = mainPlot.getDatasetCount();
        mainPlot.setDataset(id, dataset);
        mainPlot.mapDatasetToRangeAxis(id, 0);

        mainPlot.setRenderer(id, renderer);
        NumberAxis numberAxis = (NumberAxis) mainPlot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);
        mainPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        return id;
    }

    /**Adds entry and exits signals to the main plot.
     * @param trades, List of trades with corresponding entry/exit signals
     * @param orderType, the OrderType BUY or SELL
     * */
    public void addEntryExitSignals(List<Trade> trades, Order.OrderType orderType){
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
            mainPlot.addDomainMarker(in);

            ValueMarker out = new ValueMarker(exit);
            out.setLabel(orderType.complementType().toString());
            out.setLabelPaint(Color.WHITE);
            out.setLabelAnchor(exitAnchor);
            out.setPaint(exitColor);
            mainPlot.addDomainMarker(out);

            IntervalMarker imarker = new IntervalMarker(entry, exit, entryColor);
            imarker.setAlpha(0.1f);
            mainPlot.addDomainMarker(imarker);
            markers.add(in);
            markers.add(out);
            markers.add(imarker);
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == this.showRecord){
            if (showRecord.isSelected()) {
                this.addEntryExitSignals(this.record.getTrades(), record.getLastExit().getType().complementType());
                return;
            }
        if (!showRecord.isSelected()){
            for(Marker m : this.markers){
                mainPlot.removeDomainMarker(m);
            }

        }
        }

        for (TaChartIndicator in : this.taChartIndicators) {
            if (src == in.getMenuEntry()) {
                boolean show = in.getMenuEntry().isSelected();
                if (!in.isSubchart()) {
                    int id = mapEntryToChart.get(in.getMenuEntry());
                    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) mainPlot.getRenderer(id);
                    TimeSeriesCollection d = (TimeSeriesCollection) mainPlot.getDataset(id);
                    for (int i = 0; i < d.getSeriesCount(); i++) {
                        renderer.setSeriesVisible(i, show);
                    }
                } else {
                    if(show) {
                        combinedXYPlot.add(mapEntryToXYPlot.get(in.getMenuEntry()));
                        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);

                    }
                    else {
                        combinedXYPlot.remove(mapEntryToXYPlot.get(in.getMenuEntry()));
                        combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);
                    }
                }

            }

        }
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



