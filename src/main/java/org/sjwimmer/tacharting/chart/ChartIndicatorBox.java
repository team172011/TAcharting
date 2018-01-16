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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.sjwimmer.tacharting.chart.parameters.ChartType;
import org.sjwimmer.tacharting.chart.parameters.Parameter.IndicatorCategory;
import org.sjwimmer.tacharting.chart.parameters.ShapeType;
import org.sjwimmer.tacharting.chart.parameters.StrokeType;
import org.sjwimmer.tacharting.chart.utils.FormatUtils;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.bollinger.PercentBIndicator;
import org.ta4j.core.indicators.candles.LowerShadowIndicator;
import org.ta4j.core.indicators.candles.RealBodyIndicator;
import org.ta4j.core.indicators.candles.UpperShadowIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.volume.*;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.sjwimmer.tacharting.chart.parameters.Parameter.IndicatorCategory.DEFAULT;

// TODO: overload notifyObserver function to change just entries that are modified
public class ChartIndicatorBox {

    private final ObservableMap<String, ChartIndicator> chartIndicatorMap; // currently loaded indicators
    private final ObservableMap<String, TradingRecord> tradingRecordMap; // stored trading records
    private final ObservableMap<String, ChartIndicator> tempChartIndicatorBackup; // indicators that are dynamically added
    private final PropertiesManager parameter;
    private final ObjectProperty<TimeSeries> series;
    private final ObjectProperty<Indicator> closePriceIndicator;

    /**
     * Constructor
     */
    public ChartIndicatorBox(TimeSeries series){
        if(series==null){
            throw new IllegalArgumentException("Null not permitted for TimeSeries");
        }
        this.chartIndicatorMap = FXCollections.observableMap(new HashMap<>());
        this.tradingRecordMap = FXCollections.observableMap(new HashMap<>());
        this.tempChartIndicatorBackup =  FXCollections.observableMap(new HashMap<>());
        this.series = new SimpleObjectProperty<>(series);
        this.closePriceIndicator = new SimpleObjectProperty<>(new ClosePriceIndicator(this.series.get()));
        this.parameter = new PropertiesManager();
    }

    /**
     * Sets (changes) the {@link TimeSeries time series} for this ChartIndicatorBox.
     * All currently loaded indicators in the {{@link #chartIndicatorMap} chartIndicatorMap} will be updated with the
     * new <tt>series</tt>
     * @apiNote  All dynamically added indicators will be deleted if <tt>series</tt> != this.series.get()
     * @param series the new TimeSeries object for this indicator box
     */
    public void setTimeSeries(TimeSeries series){
        if(series == null){
            throw new IllegalArgumentException("Null not permitted for TimeSeries");
        }
        if(series == this.series.get()){
            return;
        }
        this.series.setValue(series);
        this.closePriceIndicator.set(new ClosePriceIndicator(series));
        this.tempChartIndicatorBackup.clear();
        reloadAll();
    }

    public void addTradingRecord(String name, TradingRecord record){
        this.tradingRecordMap.put(name, record);
    }

    public TradingRecord getTradingRecord(String name){
        return this.tradingRecordMap.get(name);
    }

    public ObservableMap<String,TradingRecord> getAllTradingRecords(){
        return this.tradingRecordMap;
    }

    public TimeSeries getTimeSeries(){
        return series.get();
    }

    public ObservableObjectValue<TimeSeries> getObservableTimeSeries(){
        return series;
    }

    // simple moving average
    private void loadSMAIndicator(String key) throws XPathException {
        int smaTimeFrame = Integer.parseInt(parameter.getParameter(key,"Time Frame"));
        Color color = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color"));
        StrokeType stroke = StrokeType.valueOf(parameter.getParameter(key, "Stroke"));
        ShapeType shape = ShapeType.valueOf(parameter.getParameter(key,"Shape"));
        ChartType chartType = parameter.getChartType(key);
        IndicatorCategory category = parameter.getCategory(key);

        ChartIndicator sma = new ChartIndicator(new SMAIndicator(closePriceIndicator.get(), smaTimeFrame),
                String.format("%s (%s) (%s)",getIdentifier(key),getID(key),smaTimeFrame),
                createRenderer(color,stroke,shape),
                chartType.toBoolean(),
                category);
        addChartIndicator(key, sma);
    }

    // exponential moving average
    private void loadEMAIndicator(String key)throws XPathException {
        int timeFrame = Integer.parseInt(parameter.getParameter(key,"Time Frame"));
        Color color = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color"));
        StrokeType stroke = StrokeType.valueOf(parameter.getParameter(key, "Stroke"));
        ShapeType shape = ShapeType.valueOf(parameter.getParameter(key,"Shape"));
        ChartType chartType = parameter.getChartType(key);
        IndicatorCategory category = parameter.getCategory(key);

        addChartIndicator(key, new EMAIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s] (%s)",getIdentifier(key),getID(key),timeFrame),
                createRenderer(color, stroke, shape),
                chartType.toBoolean(),
                category);
    }

    //CCI
    private void loadCCIIndicator(String key)throws XPathException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key,"Time Frame"));
        Color color = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color"));
        StrokeType stroke = StrokeType.valueOf(parameter.getParameter(key, "Stroke"));
        ShapeType shape = ShapeType.valueOf(parameter.getParameter(key,"Shape"));
        ChartType chartType = parameter.getChartType(key);
        IndicatorCategory category = parameter.getCategory(key);

        addChartIndicator(key, new CCIIndicator(series.get(), timeFrame),
                String.format("%s [%s] (%s)",getIdentifier(key),getID(key),timeFrame),
                createRenderer(color, stroke, shape),
                chartType.toBoolean(),
                category);
    }

    //CMO
    private void loadCMOIndicator(String key)throws XPathException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key,"Time Frame"));
        Color color = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color"));
        StrokeType stroke = StrokeType.valueOf(parameter.getParameter(key, "Stroke"));
        ShapeType shape = ShapeType.valueOf(parameter.getParameter(key,"Shape"));
        ChartType chartType = ChartType.valueOf(parameter.getParameter(key, "Chart Type"));
        IndicatorCategory category = parameter.getCategory(key);

        addChartIndicator(key, new CMOIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s] (%s)",getIdentifier(key), getID(key), timeFrame),
                createRenderer(color, stroke, shape),
                chartType.toBoolean(),
                category);
    }

    // Bollinger Bands and the width
    public void loadBollingerBands(String key) throws XPathException{
        List<Indicator> indicatorList = new ArrayList<>();
        List<String> namesList = new ArrayList<>();
        XYLineAndShapeRenderer bbRenderer = new XYLineAndShapeRenderer();
        int id = getID(key);

        int timeFrame = Integer.parseInt(parameter.getParameter(key,"Time Frame"));

        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePriceIndicator.get(), timeFrame);
        EMAIndicator bollingerEMA = new EMAIndicator(closePriceIndicator.get(),timeFrame);
        Color color1 = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Middle Band"));
        StrokeType stroke1 = StrokeType.valueOf(parameter.getParameter(key, "Stroke Middle Band"));
        ShapeType shape1 = ShapeType.valueOf(parameter.getParameter(key,"Shape Middle Band"));
        Color color2 = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Upper Band"));
        StrokeType stroke2 = StrokeType.valueOf(parameter.getParameter(key, "Stroke Upper Band"));
        ShapeType shape2 = ShapeType.valueOf(parameter.getParameter(key,"Shape Upper Band"));
        Color color3 = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Lower Band"));
        StrokeType stroke3 = StrokeType.valueOf(parameter.getParameter(key, "Stroke Lower Band"));
        ShapeType shape3 = ShapeType.valueOf(parameter.getParameter(key,"Shape Lower Band"));
        ChartType chartType = ChartType.valueOf(parameter.getParameter(key, "Chart Type"));
        IndicatorCategory category = parameter.getCategory(key);

        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(bollingerEMA);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm,sd);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm,sd);

        indicatorList.add(bbm);
        indicatorList.add(bbu);
        indicatorList.add(bbl);

        namesList.add("Middle Band"+ timeFrame);
        namesList.add("Upper Band ");
        namesList.add("Lower Band ");

        bbRenderer.setSeriesPaint(0, color1);
        bbRenderer.setSeriesStroke(0, stroke1.stroke);
        bbRenderer.setSeriesShape(0, shape1.shape);
        bbRenderer.setSeriesPaint(1, color2);
        bbRenderer.setSeriesStroke(1,stroke2.stroke);
        bbRenderer.setSeriesShape(1, shape2.shape);
        bbRenderer.setSeriesPaint(2, color3);
        bbRenderer.setSeriesStroke(2, stroke3.stroke);
        bbRenderer.setSeriesShape(2, shape3.shape);
        addChartIndicator(key,
                indicatorList,
                namesList,
                String.format("Bollinger Bands [%s] (%s)",id,timeFrame),
                bbRenderer,chartType.toBoolean(),
                category);
    }

    /* TODO: find solution to plot indicators on other indicators (BollingerBandsWidth for the Bolliger Bands
    public void loadBollingerBandsWidth(String key){
        addChartIndicator("Bollinger Bands Width_" + getID(key),
                new BollingerBandWidthIndicator(bbu, bbm, bbl),
                String.ofFormat("Bollinger Band Width [%s]", id),
                bbRenderer,
                ChartType.SUBCHART.toBoolean(),
                category);
    }
    */

    public void loadPercentBIndicator(String key) throws XPathException{
        int timeFrame =Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        Decimal k = Decimal.valueOf(parameter.getParameter(key,"K Multiplier"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType type = parameter.getChartType(key);
        addChartIndicator(key,
                new PercentBIndicator(closePriceIndicator.get(), timeFrame, k),
                String.format("%s [%s] (%s, %s)",getIdentifier(key),getID(key),timeFrame,k),
                renderer,
                type.toBoolean(),
                category);

    }

    //Amount Indicator
    public void loadAmountIndicator(String key) throws XPathExpressionException {
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        addChartIndicator(key,
                new AmountIndicator(series.get()),
                String.format("Amount [%s]", getID(key)),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // RSI Indicator
    public void loadRSIIndicator(String key) throws XPathExpressionException {
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");

        addChartIndicator(key,
                new RSIIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s] (%s)",getIdentifier(key), getID(key),timeFrame),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // SmoothedRSIIndicator
    public void loadSmoothedRSIIndicator(String key) throws XPathExpressionException {
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");

        addChartIndicator(key,
                new RSIIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s] (%s)",getIdentifier(key), getID(key),timeFrame),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // PVIIndicator
    public void loadPVIIndicator(String key) throws XPathExpressionException {
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");

        addChartIndicator(key,
                new PVIIndicator(series.get()),
                String.format("%s [%s]",getIdentifier(key), getID(key)),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // NVIIndicator
    public void loadNVIIndicator(String key) throws XPathExpressionException {
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        addChartIndicator(key,
                new NVIIndicator(series.get()),
                String.format("%s [%s]",getIdentifier(key), getID(key)),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // OnBalanceVolumeIndicator
    public void loadOnBalanceVolumeIndicator(String key) throws XPathExpressionException {
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");

        addChartIndicator(key,
                new OnBalanceVolumeIndicator(series.get()),
                String.format("%s [%s]",getIdentifier(key), getID(key)),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // VWAPIndicator
    public void loadVWAPIndicator(String key) throws XPathExpressionException {
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");

        addChartIndicator(key,
                new VWAPIndicator(series.get(), timeFrame),
                String.format("%s [%s] (%s)",getIdentifier(key), getID(key), timeFrame),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // MACD Indicator
    public void loadMACDIndicator(String key) throws XPathExpressionException {
        int timeFrameShort = Integer.parseInt(parameter.getParameter(key, "Time Frame Short"));
        int timeFrameLong = Integer.parseInt(parameter.getParameter(key, "Time Frame Long"));
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        boolean signalLine = Boolean.valueOf(parameter.getParameter(key, "Add Signal Line"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        MACDIndicator mcd = new MACDIndicator(closePriceIndicator.get(), timeFrameShort, timeFrameLong);
        if(!signalLine){
            addChartIndicator(key,
                    mcd,
                    String.format("%s [%s] (%s, %s)",getIdentifier(key), getID(key), timeFrameShort,timeFrameLong),
                    renderer,
                    chartType.toBoolean(),
                    category);

        } else{
            int timeFrameSignal = Integer.parseInt(parameter.getParameter(key, "Time Frame Signal Line"));
            List<String> names = new ArrayList<>();
            List<Indicator> indicators = new ArrayList<>();
            indicators.add(mcd);
            indicators.add(new EMAIndicator(mcd, timeFrameSignal));
            names.add(String.format("%s [%s] (%s, %s)",getIdentifier(key), getID(key), timeFrameShort,timeFrameLong));
            names.add(String.format("Signal Line [%s] (%s)",getID(key),timeFrameSignal));
            Color color = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Signal Line"));
            ShapeType shape = ShapeType.valueOf(parameter.getParameter(key, "Shape Signal Line"));
            StrokeType stroke = StrokeType.valueOf(parameter.getParameter(key, "Stroke Signal Line"));
            renderer.setSeriesPaint(1,color);
            renderer.setSeriesShape(1,shape.shape);
            renderer.setSeriesStroke(1, stroke.stroke);
            addChartIndicator(key,
                    indicators,
                    names,
                    String.format("%s [%s] (%s, %s)",getIdentifier(key), getID(key), timeFrameShort,timeFrameLong),
                    renderer,
                    chartType.toBoolean(),
                    category);
        }



    }

    //Average Directional Movement Down and Up
    public void loadAverageDirectionalMovementUP_DOWN(String key) throws XPathExpressionException {
        Color color1 = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Up"));
        StrokeType stroke1 = StrokeType.valueOf(parameter.getParameter(key, "Stroke Up"));
        ShapeType shape1 = ShapeType.valueOf(parameter.getParameter(key,"Shape Up"));
        Color color2 = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Down"));
        StrokeType stroke2 = StrokeType.valueOf(parameter.getParameter(key, "Stroke Down"));
        ShapeType shape2 = ShapeType.valueOf(parameter.getParameter(key,"Shape Down"));
        ChartType chartType = ChartType.valueOf(parameter.getParameter(key, "Chart Type"));
        IndicatorCategory category = parameter.getCategory(key);
        int timeFrameUp = Integer.parseInt(parameter.getParameter(key, "Time Frame Up"));
        int timeFrameDown = Integer.parseInt(parameter.getParameter(key, "Time Frame Up"));
        List<Indicator> ilAdx = new ArrayList<>();
        List<String> nlAdx = new ArrayList<>();

        ilAdx.add(new AverageDirectionalMovementUpIndicator(series.get(), timeFrameDown));
        ilAdx.add(new AverageDirectionalMovementDownIndicator(series.get(), timeFrameDown));
        nlAdx.add("ADX UP "+timeFrameUp);
        nlAdx.add("ADX Down "+timeFrameUp);
        XYLineAndShapeRenderer adxRenderer = new XYLineAndShapeRenderer();
        adxRenderer.setSeriesPaint(0, color1);
        adxRenderer.setSeriesStroke(0, stroke1.stroke);
        adxRenderer.setSeriesShape(0, shape1.shape);
        adxRenderer.setSeriesPaint(1, color2);
        adxRenderer.setSeriesStroke(1, stroke2.stroke);
        adxRenderer.setSeriesShape(1, shape2.shape);
        addChartIndicator(key,
                ilAdx,
                nlAdx,
                String.format("%s [%s] (%s, %s)", getIdentifier(key), getID(key), timeFrameUp, timeFrameDown ),
                adxRenderer,
                chartType.toBoolean(),
                category);

    }

    // True Range Indicator
    public void loadTrueRangeIndicator(String key) throws XPathExpressionException {
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        ChartType chartType = parameter.getChartType(key);
        IndicatorCategory category = parameter.getCategory(key);
        addChartIndicator(key,
                new TrueRangeIndicator(series.get()),
                String.format("%s [%s]",getIdentifier(key), getID(key)),
                renderer,
                chartType.toBoolean(),
                category);

    }

    // Keltner channels
    public void loadKeltner(String key) throws XPathException{

        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        Decimal ratio = Decimal.valueOf(parameter.getParameter(key, "Ratio"));
        int atr = Integer.parseInt(parameter.getParameter(key, "Time Frame ATR"));
        Color colorU = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Upper"));
        StrokeType strokeU = StrokeType.valueOf(parameter.getParameter(key, "Stroke Upper"));
        ShapeType shapeU = ShapeType.valueOf(parameter.getParameter(key,"Shape Upper"));
        Color colorL = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Lower"));
        StrokeType strokeL = StrokeType.valueOf(parameter.getParameter(key, "Stroke Lower"));
        ShapeType shapeL = ShapeType.valueOf(parameter.getParameter(key,"Shape Lower"));
        ChartType chartType = ChartType.valueOf(parameter.getParameter(key, "Chart Type"));
        IndicatorCategory category = parameter.getCategory(key);

        XYLineAndShapeRenderer renderer = createRenderer(key, "Color Middle", "Shape Middle", "Stroke Middle");
        renderer.setSeriesStroke(1, strokeU.stroke);
        renderer.setSeriesStroke(2, strokeL.stroke);
        renderer.setSeriesShape(1, shapeU.shape);
        renderer.setSeriesShape(2, shapeL.shape);
        renderer.setSeriesPaint(1, colorU);
        renderer.setSeriesPaint(2, colorL);

        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(series.get(), timeFrame);
        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM,ratio,atr);
        KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM,ratio,atr);

        List<Indicator> ilKelt = new ArrayList<>();
        List<String> nlKelt = new ArrayList<>();
        ilKelt.add(kcL);
        ilKelt.add(kcM);
        ilKelt.add(kcU);
        nlKelt.add("Keltner Lower");
        nlKelt.add("Keltner Middle");
        nlKelt.add("Keltner Upper");
        addChartIndicator(key,
                ilKelt,
                nlKelt,
                String.format("%s [%s] (%s, %s, %S)", getIdentifier(key), getID(key), timeFrame, ratio, atr),
                renderer,
                chartType.toBoolean(),
                category);
    }

    // Arron Up/Down in one subplot
    public void loadAroonUP_DOWN(String key) throws XPathException{
        int arronUp = Integer.parseInt(parameter.getParameter(key, "Time Frame Up"));
        int arronDown = Integer.parseInt(parameter.getParameter(key, "Time Frame Down"));
        Color colorD = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color Down"));
        StrokeType strokeD = StrokeType.valueOf(parameter.getParameter(key, "Stroke Down"));
        ShapeType shapeD = ShapeType.valueOf(parameter.getParameter(key, "Shape Down"));
        ChartType chartType = parameter.getChartType(key);
        IndicatorCategory category = parameter.getCategory(key);

        List<Indicator> ilAroon = new ArrayList<>();
        List<String> nlAroon = new ArrayList<>();
        ilAroon.add(new AroonDownIndicator(series.get(), arronDown));
        ilAroon.add(new AroonUpIndicator(series.get(), arronUp));
        nlAroon.add("Aroon Down "+arronDown);
        nlAroon.add("Aroon Up "+arronUp);
        XYLineAndShapeRenderer arronUpDownRenderer = createRenderer(key, "Color Up", "Shape Up", "Stroke Up");

        arronUpDownRenderer.setSeriesPaint(1, colorD);
        arronUpDownRenderer.setSeriesStroke(1, strokeD.stroke);
        arronUpDownRenderer.setSeriesShape(1, shapeD.shape);

        addChartIndicator(key,
                ilAroon,
                nlAroon,String.format("%s [%s] (%s, %s)",getIdentifier(key), getID(key),arronUp, arronDown),
                arronUpDownRenderer,
                chartType.toBoolean(),
                category);
    }

    // Lower Shadown Indicator
    public  void loadLowerShadowIndicator(String key) throws XPathExpressionException {
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        addChartIndicator(key,new LowerShadowIndicator(series.get()),String.format("%s [%s]", getIdentifier(key), getID(key)),renderer, true, category);
    }

    // Upper Shadown Indicator
    public  void loadUpperShadowIndicator(String key) throws XPathExpressionException {
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        addChartIndicator(key,new UpperShadowIndicator(series.get()),String.format("%s [%s]", getIdentifier(key), getID(key)),renderer, true, category);
    }

    // Upper Shadown Indicator
    public  void loadRealBodyIndicator(String key) throws XPathExpressionException {
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        addChartIndicator(key, new RealBodyIndicator(series.get()),String.format("%s [%s]", getIdentifier(key), getID(key)),renderer, true, category);
    }

    // MVWAP + VWAP
    public void loadMVWAPIndicator(String key) throws XPathException{
        int timeFrameMVWAP = Integer.parseInt(parameter.getParameter(key, "Time Frame VWAP"));
        int timeFrameVWAP = Integer.parseInt(parameter.getParameter(key, "Time Frame MVWAP"));

        VWAPIndicator vwap = new VWAPIndicator(series.get(),timeFrameVWAP);
        MVWAPIndicator mvwap = new MVWAPIndicator(vwap,timeFrameMVWAP);

        List<Indicator> ilVwap = new ArrayList<>();
        List<String> nlVwap = new ArrayList<>();

        XYLineAndShapeRenderer wapRenderer = createRenderer(key, "Color MVWAP", "Shape MVWAP", "Stroke MVWAP");
        Color vwapColor = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key, "Color VWAP"));
        
        StrokeType vwapStroke = StrokeType.valueOf(parameter.getParameter(key, "Stroke VWAP"));
        ShapeType vwapShape = ShapeType.valueOf(parameter.getParameter(key, "Shape VWAP"));
        wapRenderer.setSeriesPaint(1, vwapColor);
        wapRenderer.setSeriesStroke(1, vwapStroke.stroke);
        wapRenderer.setSeriesShape(1, vwapShape.shape);
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        ilVwap.add(mvwap);
        ilVwap.add(vwap);
        nlVwap.add(String.format("%s [%s] (%s)",getIdentifier(key), getID(key), timeFrameMVWAP));
        nlVwap.add(String.format("%s [%s] (%s)","VWAP", getID(key), timeFrameVWAP));
        addChartIndicator(key, ilVwap, nlVwap,"MVWAP/VWAP ",wapRenderer, chartType.toBoolean(), category);
    }

    // TrailingStopLossIndicator
    public void loadTraillingStopLossIndicator(String key) throws XPathException{
        Double threshold = Double.parseDouble(parameter.getParameter(key, "Threshold"));

        ChartType chartType = parameter.getChartType(key);
        IndicatorCategory category = parameter.getCategory(key);
        XYLineAndShapeRenderer xyLineAndShapeRenderer = createRenderer(key, "Color", "Shape", "Stroke");
        addChartIndicator(key,
                new TrailingStopLossIndicator(closePriceIndicator.get(),Decimal.valueOf(threshold)),
                String.format("%s [%s] (%s)", getIdentifier(key), getID(key), threshold),
                xyLineAndShapeRenderer,
                chartType.toBoolean(),
                category);
    }

    // Triple EMAIndicator
    public void loadTrippleEMAIndicator(String key) throws XPathException{
        int timeFrame =Integer.parseInt(parameter.getParameter(key, "Time Frame"));

        ChartType chartType = parameter.getChartType(key);
        IndicatorCategory category = parameter.getCategory(key);
        XYLineAndShapeRenderer xyLineAndShapeRenderer = createRenderer(key, "Color", "Shape", "Stroke");
        addChartIndicator(key,
                new TripleEMAIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s] (%s)", getIdentifier(key), getID(key), timeFrame),
                xyLineAndShapeRenderer,
                chartType.toBoolean(),
                category);
    }

    // UlcerIndexIndicator
    public void loadUlcerIndexIndicator(String key) throws XPathException{
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        addChartIndicator(key, new UlcerIndexIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s]", getIdentifier(key), getID(key)),renderer, chartType.toBoolean(), category);
    }


    // WMAIndicator
    public void loadWMAIndicator(String key) throws XPathException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        addChartIndicator(key, new WMAIndicator(closePriceIndicator.get(), timeFrame),String.format("%s [%s] (%s)",
                getIdentifier(key), getID(key), timeFrame),renderer, chartType.toBoolean(), category);
    }


    // ZLEMAIndicator
    public void loadZLEMAIndicator(String key) throws XPathException{
        int ZLEMAIndicator_1 = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        addChartIndicator(key,
                new ZLEMAIndicator(closePriceIndicator.get(), ZLEMAIndicator_1),String.format("%s [%s] (%s)",
                        getIdentifier(key), getID(key),ZLEMAIndicator_1), renderer, chartType.toBoolean(), category);
    }




    // RAVI Indicator
    public void loadRAVIIndicator(String key) throws XPathException{
        int timeFrameShort = Integer.parseInt(parameter.getParameter(key, "Time Frame Short"));
        int timeFrameLong = Integer.parseInt(parameter.getParameter(key, "Time Frame Long"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);

        addChartIndicator(key,new RAVIIndicator(closePriceIndicator.get(), timeFrameShort, timeFrameLong),
                String.format("%s [%s] (%s, %s)", getIdentifier(key), getID(key),timeFrameShort,timeFrameLong),
                renderer,chartType.toBoolean(), category);
    }


    // ROC Indicator
    public void loadROCIndicator(String key) throws XPathException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        addChartIndicator(key,
                new ROCIndicator(closePriceIndicator.get(), timeFrame),String.format("%s [%s] (%s)",
                        getIdentifier(key), getID(key), timeFrame), renderer, chartType.toBoolean(), category);
    }

    // Fisher Indicator
    public void loadFischerIndicator(String key) throws XPathException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        double alpha = Double.parseDouble(parameter.getParameter(key, "Alpha"));
        double beta = Double.parseDouble(parameter.getParameter(key, "Beta"));
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        addChartIndicator(key, new FisherIndicator(closePriceIndicator.get(), timeFrame, Decimal.valueOf(alpha), Decimal.valueOf(beta)),
                String.format("%s [%s] (%s, %s, %s)", getIdentifier(key), getID(key),timeFrame,alpha,beta),
                chartType.toBoolean(), category);
    }

    // HMA Indicator
    public void loadHMAIndicator(String key) throws XPathException {
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);
        addChartIndicator(key, new HMAIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s] (%s)",getIdentifier(key), getID(key), timeFrame), chartType.toBoolean(),
                category);
    }

    // KAMA Indicator
    public void loadKAMAIndicator(String key) throws XPathException{
        int timeFrameEffRatio = Integer.parseInt(parameter.getParameter(key, "Time Frame Effective Ratio"));
        int timeFrameFast = Integer.parseInt(parameter.getParameter(key, "Time Frame Slow"));
        int timeFrameSlow = Integer.parseInt(parameter.getParameter(key, "Time Frame Fast"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        ChartType chartType = parameter.getChartType(key);

        addChartIndicator(key,new KAMAIndicator(closePriceIndicator.get(),timeFrameEffRatio,timeFrameFast,timeFrameSlow),
                String.format("%s [%s] (%s, %s, %s)",getIdentifier(key), getID(key), timeFrameEffRatio, timeFrameFast, timeFrameSlow),
                renderer,chartType.toBoolean(), category);
    }

    // Previous Value Indicator
    public void loadPreviousValueIndicator(String key) throws XPathException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key,"Time Frame"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);
        addChartIndicator(key, new PreviousValueIndicator(closePriceIndicator.get(), timeFrame),
                String.format("%s [%s](%s)",getIdentifier(key), getID(key),timeFrame), renderer,
                false, category);

    }

    // Stochastic RSI Indicator
    public void loadStochasticRSIIndicator(String key) throws XPathExpressionException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        XYLineAndShapeRenderer renderer = createRenderer(key, "Color", "Shape", "Stroke");
        IndicatorCategory category = parameter.getCategory(key);

        addChartIndicator(key, new StochasticRSIIndicator(closePriceIndicator.get(),timeFrame),
                String.format("%s [%s](%s)",getIdentifier(key), getID(key),timeFrame), renderer,true,category);
    }


    // StochasticOscillatorKIndicator StochasticOscillatorDIndicator
    public void loadStochasticOscillatorKIndicatorStochasticOscillatorDIndicator(String key) throws XPathExpressionException{
        int timeFrame = Integer.parseInt(parameter.getParameter(key, "Time Frame"));
        List<Indicator> indicators = new ArrayList<>();
        List<String> names = new ArrayList<>();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0,parameter.getColorOf(key, "Color D"));
        renderer.setSeriesShape(0, parameter.getShapeOf(key, "Shape D"));
        renderer.setSeriesStroke(0,parameter.getStrokeOf(key, "Stroke D"));
        renderer.setSeriesStroke(1,parameter.getStrokeOf(key, "Stroke K"));
        renderer.setSeriesShape(1, parameter.getShapeOf(key, "Shape K"));
        renderer.setSeriesPaint(1,parameter.getColorOf(key, "Color K"));
        IndicatorCategory category = parameter.getCategory(key);

        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(closePriceIndicator.get());

        indicators.add(stochD);
        indicators.add(new StochasticOscillatorKIndicator(stochD,timeFrame,new MaxPriceIndicator(series.get()), new MinPriceIndicator(series.get())));
        names.add(String.format("%s [%s]", "Stoch. Oscillator D", getID(key)));
        names.add(String.format("%s [%s](Stoch. Oscillator D, %s","Stoch. Oscillator K",getID(key),timeFrame));
        addChartIndicator(key, indicators,names,String.format("Stoch. Oscillator K(%s, %s)","Stoch. Oscillator D",timeFrame),renderer,true,category);
    }

    /**
     * Creates and add all ta4j indicators with generic type Decimal to the box.
     * Use the parameter from the indicatorParameter.properties for indicator parameter
     */
        /*
        // Average Gain indicator
        int averGainTimeFrame = parameter.getOneIntFor("AverageGainIndicator_1",20);
        addChartIndicator(new AverageGainIndicator(closePriceIndicator, averGainTimeFrame),"Average Gain"+averGainTimeFrame, true,
                TaTypes.categories.HELPERS);

        // Average Loss indicator
        int averLossTimeFrame = parameter.getOneIntFor("AverageLossIndicator_1",20);
        addChartIndicator(new AverageLossIndicator(closePriceIndicator, averLossTimeFrame),"Average Gain "+averLossTimeFrame, true,
                TaTypes.categories.HELPERS);

        // Average True Range indicator
        int averTrueRangeTimeFrame = parameter.getOneIntFor("AverageTrueRangeIndicator_1",20);
        addChartIndicator("AverageTrueRangeIndicator",new AverageTrueRangeIndicator(series, averTrueRangeTimeFrame), true,
                TaTypes.categories.HELPERS);

        // Close Location Value indicator
        addChartIndicator("CloseLocationValueIndicator",new CloseLocationValueIndicator(series), true,
                TaTypes.categories.HELPERS);

        // Constant Indicator TODO: does not work, time series is null
        // buildChartIndicator(new ConstantIndicator(closePriceIndicator.getValue(0)),Color.RED,"Constant First CP", false);

        // Cumulated Gains Indicator
        int cumulatedGainsFrame = parameter.getOneIntFor("CumulatedGainsIndicator_1",20);
        addChartIndicator("CumulatedGainsIndicator_1",new CumulatedGainsIndicator(closePriceIndicator,cumulatedGainsFrame), true,
                TaTypes.categories.HELPERS);

        // Cumulated Losses Indicator
        addChartIndicator("CumulatedLossesIndicator",new CumulatedLossesIndicator(closePriceIndicator,20), true,
                TaTypes.categories.HELPERS);

        // Directional Up and Down Indicator
        int directUp = parameter.getOneIntFor("DirectionalUpIndicator_1",20);
        int directDown = parameter.getOneIntFor("DirectionalDownIndicator_1",20);
        List<Indicator> ilDud = new ArrayList<>();
        ilDud.add(new DirectionalDownIndicator(series,directUp));
        ilDud.add(new DirectionalUpIndicator(series,directDown));
        List<String> nlDud = new ArrayList<>();
        nlDud.add("Directional Down "+directDown);
        nlDud.add("Directional Up "+directUp);
        XYLineAndShapeRenderer directUpDownRenderer = new XYLineAndShapeRenderer();
        directUpDownRenderer.setSeriesPaint(0, Color.RED);
        directUpDownRenderer.setSeriesStroke(0, TaTypes.SMALL_LINE);
        directUpDownRenderer.setSeriesShape(0, TaTypes.shape_smallRec );
        directUpDownRenderer.setSeriesPaint(1, Color.GREEN);
        directUpDownRenderer.setSeriesStroke(1,TaTypes.SMALL_LINE);
        directUpDownRenderer.setSeriesShape(1, TaTypes.shape_smallRec);
        addChartIndicator(ilDud, nlDud, "Directional Up/Down "+directUp+", "+directDown,directUpDownRenderer, true,
                TaTypes.categories.HELPERS);


        //Directional Movement Down and UP
        List<Indicator> ilDmud = new ArrayList<>();
        ilDmud.add(new DirectionalMovementDownIndicator(series));
        ilDmud.add(new DirectionalMovementUpIndicator(series));
        List<String> nlmDud = new ArrayList<>();
        nlmDud.add("Directional Movement Down");
        nlmDud.add("Directional Movement Up");

        XYLineAndShapeRenderer dmudRenderer = new XYLineAndShapeRenderer();
        dmudRenderer.setSeriesPaint(0, Color.RED);
        dmudRenderer.setSeriesStroke(0, TaTypes.SMALL_LINE);
        dmudRenderer.setSeriesShape(0, TaTypes.shape_smallRec );
        dmudRenderer.setSeriesPaint(1, Color.GREEN);
        dmudRenderer.setSeriesStroke(1,TaTypes.SMALL_LINE);
        dmudRenderer.setSeriesShape(1, TaTypes.shape_smallRec);
        addChartIndicator(ilDmud, nlmDud, "Directional Movement Up/Down", dmudRenderer,true,
                TaTypes.categories.HELPERS);

        // Highest Value Indicator
        int highestValueT = parameter.getOneIntFor("highestValue_1",20);
        addChartIndicator(new HighestValueIndicator(closePriceIndicator, highestValueT), "Highest Value "+highestValueT,false, TaTypes.categories.HELPERS);

        // Lowest Value Indicator
        int lowestValueT = parameter.getOneIntFor("lowestValue_1",20);
        addChartIndicator(new LowestValueIndicator(closePriceIndicator, lowestValueT),"Lowest Value "+lowestValueT,false, TaTypes.categories.HELPERS);

        // Max Price Indicator
        addChartIndicator(new MaxPriceIndicator(series),"Max Price Indicator",false, TaTypes.categories.HELPERS);

        // Mean Deviation Indicator
        int meanDevT = parameter.getOneIntFor("MeanDeviationIndicator_1",20);
        addChartIndicator(new MeanDeviationIndicator(closePriceIndicator, meanDevT),
                "Mean Deciation "+meanDevT,false, TaTypes.categories.HELPERS);

        // Mean Price Indicator
        addChartIndicator("MedianPriceIndicator",new MedianPriceIndicator(series),false, TaTypes.categories.HELPERS);

        // Median Price Indicator
        addChartIndicator("MedianPriceIndicator",new MedianPriceIndicator(series),false, TaTypes.categories.HELPERS);

        // Open price Indicator
        addChartIndicator("OpenPriceIndicator",new OpenPriceIndicator(series),false, TaTypes.categories.HELPERS);


        // Price Variantion Indicator
        addChartIndicator("PriceVariationIndicator",new PriceVariationIndicator(series),true, TaTypes.categories.HELPERS);

        // Smoothed Average Gain Indicator+
        int meanDevTimeFrame = parameter.getOneIntFor("SmoothedAverageGainIndicator_1",20);
        addChartIndicator("SmoothedAverageGainIndicator",new SmoothedAverageGainIndicator(closePriceIndicator,20),true, TaTypes.categories.HELPERS);

        // Smoothed Average Gain Indicator
        addChartIndicator("SmoothedAverageLossIndicator",new SmoothedAverageLossIndicator(closePriceIndicator,20),true, TaTypes.categories.HELPERS);

        // Trade Count //TODO: integer does not work
        //addChartIndicator(new TradeCountIndicator(series),true, TaTypes.categories.HELPERS);

        // Typical Price Indicator
        addChartIndicator("TypicalPriceIndicator",new TypicalPriceIndicator(series),false, TaTypes.categories.HELPERS);

        // Volume Indicator
        addChartIndicator("VolumeIndicator",new VolumeIndicator(series),true, TaTypes.categories.HELPERS);

        //ichimoku
        int kijunSenPara = parameter.getOneIntFor("IchimokuKijunSenIndicator_1",20);
        int tenkanSenPara = parameter.getOneIntFor("IchimokuTenkanSenIndicator_1",26);
        int spanB = parameter.getOneIntFor("IchimokuSenkouSpanBIndicator_1",56);
        List<Indicator> ilIchi = new ArrayList<>();
        IchimokuKijunSenIndicator kijunSen = new IchimokuKijunSenIndicator(series, kijunSenPara);
        IchimokuTenkanSenIndicator tenkanSen = new IchimokuTenkanSenIndicator(series, tenkanSenPara);

        ilIchi.add(new IchimokuSenkouSpanAIndicator(series, tenkanSen, kijunSen));
        ilIchi.add(new IchimokuSenkouSpanBIndicator(series, spanB));
        ilIchi.add(kijunSen);
        ilIchi.add(tenkanSen);
        XYLineAndShapeRenderer ichiRenderer = new XYLineAndShapeRenderer();
        ichiRenderer.setSeriesPaint(0, Color.GREEN);
        ichiRenderer.setSeriesStroke(0, TaTypes.SMALL_LINE);
        ichiRenderer.setSeriesShape(0, TaTypes.NONE );
        ichiRenderer.setSeriesPaint(1, Color.RED);
        ichiRenderer.setSeriesStroke(1,TaTypes.SMALL_LINE);
        ichiRenderer.setSeriesShape(1, TaTypes.NONE);
        ichiRenderer.setSeriesPaint(2, Color.RED.brighter());
        ichiRenderer.setSeriesStroke(2,TaTypes.SMALL_LINE);
        ichiRenderer.setSeriesShape(2, TaTypes.NONE);
        ichiRenderer.setSeriesPaint(3, Color.BLUE);
        ichiRenderer.setSeriesStroke(3,TaTypes.SMALL_LINE);
        ichiRenderer.setSeriesShape(3, TaTypes.NONE);
        List<String> nlIchi = new ArrayList<>();
        nlIchi.add("Senkou Span A (TenkanSen, KijunSen)"); // no idea what i am doing^^
        nlIchi.add("Senkou Span B "+spanB);
        nlIchi.add("KijunSen "+kijunSenPara);
        nlIchi.add("TenkanSen "+tenkanSenPara);
        addChartIndicator(ilIchi,nlIchi,"Ichimoku All",ichiRenderer, false, TaTypes.categories.ICHIMOKU);

        addChartIndicator("IchimokuKijunSenIndicator_1",kijunSen, false, TaTypes.categories.ICHIMOKU);
        addChartIndicator("IchimokuTenkanSenIndicator_1",tenkanSen, false, TaTypes.categories.ICHIMOKU);



        // Correlation Coefficient Indicator
        int correlationTimeFrame = parameter.getOneIntFor("CorrelationCoefficientIndicator_1",5);
        addChartIndicator(new CorrelationCoefficientIndicator(closePriceIndicator, new MinPriceIndicator(series),correlationTimeFrame),
           "Correlation Coefficient cp, minP"+correlationTimeFrame,true, TaTypes.categories.STATISTICS);

        // Covariance Indicator
        int covarrianceTimeFrame = parameter.getOneIntFor("CovarianceIndicator_1",5);
        addChartIndicator(new CovarianceIndicator(closePriceIndicator,new MinPriceIndicator(series),20),
             "Covariance Indicator cp minP "+covarrianceTimeFrame,true, TaTypes.categories.STATISTICS);

        // Period Growth Rate Indicator
        int periodicalGrowthRateFrame = parameter.getOneIntFor("PeriodicalGrowthRateIndicator_1",5);
        addChartIndicator(new PeriodicalGrowthRateIndicator(closePriceIndicator,periodicalGrowthRateFrame),"Period Growth Rate cp "+periodicalGrowthRateFrame,true, TaTypes.categories.STATISTICS);

        // Simple Linear Regression Indicator
        int simpleLinearRegressionTimeFrame = parameter.getOneIntFor("SimpleLinearRegressionIndicator_1",15);
        addChartIndicator(new SimpleLinearRegressionIndicator(closePriceIndicator,simpleLinearRegressionTimeFrame), "Simple Linear Regression cp "+simpleLinearRegressionTimeFrame,false, TaTypes.categories.STATISTICS);

        // Standard Deviatation Indicator
        int standardDeviationTimeFrame = parameter.getOneIntFor("StandardDeviationIndicator_1",15);
        addChartIndicator(new StandardDeviationIndicator(closePriceIndicator, standardDeviationTimeFrame),
               "Standard Deviatation cp "+standardDeviationTimeFrame, true, TaTypes.categories.STATISTICS);

        //Standard Error Indicator
        int standardErrorTimeFrame = parameter.getOneIntFor("standardErrorTimeFrame",5);
        addChartIndicator(new StandardErrorIndicator(closePriceIndicator,standardErrorTimeFrame), "Standard Error cp "+standardErrorTimeFrame, true, TaTypes.categories.STATISTICS);

        //VarianceIndicator
        int varianceTimeFrame = parameter.getOneIntFor("VarianceIndicator_1",10);
        addChartIndicator(new VarianceIndicator(closePriceIndicator,varianceTimeFrame),"Variance cp "+varianceTimeFrame, true, TaTypes.categories.STATISTICS);

        //Accerleration Deceleration Indicator
        int[] accDeDef = {20,50};
        accDeDef = parameter.getXIntFor("AccelerationDecelerationIndicator_1",2,accDeDef);
        addChartIndicator(new AccelerationDecelerationIndicator(series,20,50),"Accel. Decel. "+accDeDef[0]+" "+accDeDef[1], true, TaTypes.categories.DEFAULT);



        // Average Directional Movement
        int admTimeFrame = parameter.getOneIntFor("AverageDirectionalMovementIndicator_1",14);
        AverageDirectionalMovementIndicator admd = new AverageDirectionalMovementIndicator(series, 14);
        addChartIndicator(new AverageDirectionalMovementDownIndicator(series, admTimeFrame),
                "ADX "+admTimeFrame,true, TaTypes.categories.DEFAULT);

        // Awesome Oscillator
        int[] awsDef = {5,34};
        int[] awesomeOscillator= parameter.getXIntFor("AwesomeOscillatorIndicator_1",2,awsDef);
        addChartIndicator(new AwesomeOscillatorIndicator(closePriceIndicator,awesomeOscillator[0],awesomeOscillator[1]),
                "Awesome Oscillator cp "+awesomeOscillator[0]+" "+awesomeOscillator[1],true, TaTypes.categories.DEFAULT);



        XYLineAndShapeRenderer chandLong = new XYLineAndShapeRenderer();
        chandLong.setSeriesStroke(0,TaTypes.SMALL_LINE);
        chandLong.setSeriesPaint(0,Color.GREEN);
        chandLong.setSeriesShape(0, TaTypes.NONE);

        // ChandelierExitLongIndicator
        addChartIndicator(new ChandelierExitLongIndicator(series),"Chandelier Exit Long", chandLong,false, TaTypes.categories.DEFAULT);

        XYLineAndShapeRenderer chandShort = new XYLineAndShapeRenderer();
        chandShort.setSeriesStroke(0,TaTypes.SMALL_LINE);
        chandShort.setSeriesPaint(0,Color.RED);
        chandShort.setSeriesShape(0, TaTypes.NONE);
        // ChandelierExitShortIndicator
        addChartIndicator(new ChandelierExitShortIndicator(series),"Chandelier Exit Short",chandShort,false, TaTypes.categories.DEFAULT);

        // CMO Indicator
        int cmoTimeFrame = parameter.getOneIntFor("CMOIndicator_1",14);
        addChartIndicator(new CMOIndicator(closePriceIndicator, cmoTimeFrame),"CMO cp "+cmoTimeFrame,
                true, TaTypes.categories.DEFAULT);

        // Coppock Curve Indicator
        int[]coppCurveDef = {14,11,10};
        coppCurveDef = parameter.getXIntFor("CoppockCurveIndicator_1",3,coppCurveDef);
        addChartIndicator(new CoppockCurveIndicator(closePriceIndicator,coppCurveDef[0],coppCurveDef[1],coppCurveDef[2]),
                "Coppock Curve "+coppCurveDef[0]+" "+coppCurveDef[1]+" "+coppCurveDef[2], true, TaTypes.categories.DEFAULT);

        // Directional Movement Indicator
        int directMoveTimeFrame = parameter.getOneIntFor("DirectionalMovementIndicator_1",14);
        addChartIndicator(new DirectionalMovementIndicator(series, directMoveTimeFrame),
                "Directional Movement "+directMoveTimeFrame, true, TaTypes.categories.DEFAULT);

        // Double EMA Indicator
        int doubleEmaTimeFrame = parameter.getOneIntFor("DoubleEMAIndicator_1", 20);
        addChartIndicator(new DoubleEMAIndicator(closePriceIndicator, doubleEmaTimeFrame),
                "DoubleEMA cp "+doubleEmaTimeFrame, false, TaTypes.categories.DEFAULT);

        // DPO Indicator
        /**
         * @since since the repository moved to https://github.com/ta4j/ta4j/ this indicator has been fixed
         */
        /*
        int dpoFrame = parameter.getOneIntFor("DPOIndicator_1",20);
        addChartIndicator(new indicators.DPOIndicator(closePriceIndicator,20),
                "DPO (20)", true, TaTypes.categories.DEFAULT);



        // Mass Index Indicator
        int[] massParams = {20,10};
        massParams = parameter.getXIntFor("MACDIndicator_1",2,macParams);
        addChartIndicator(new MassIndexIndicator(series,massParams[0],massParams[1]),
                "Mass Index Indicator "+massParams[0]+" "+massParams[1], true, TaTypes.categories.DEFAULT);

        /**@since since the repository moved to https://github.com/ta4j/ta4j/ this indicator has been fixed*/
        // Parabolic Sar Indicator
        /*
        int parabolicTimeFrame = parameter.getOneIntFor("ParabolicSarIndicator_1", 20);
        indicators.ParabolicSarIndicator nPara = new indicators.ParabolicSarIndicator(series);
        XYLineAndShapeRenderer parabolicRenderer = new XYLineAndShapeRenderer();
        parabolicRenderer.setSeriesPaint(0,Color.YELLOW);
        parabolicRenderer.setSeriesStroke(0, TaTypes.BIG_DOTS);
        addChartIndicator(nPara, "Parabolic Sar "+parabolicTimeFrame, parabolicRenderer, false, TaTypes.categories.DEFAULT);

        // PPO Indicator
        int[] ppoDef = {10,50};
        ppoDef = parameter.getXIntFor("PPOIndicator", 2, ppoDef);
        addChartIndicator(new PPOIndicator(closePriceIndicator, 10, 50),
                "PPO (10,50)", false, TaTypes.categories.DEFAULT);


        // Random Walk High and Low
        int rwHighTimeFrame = parameter.getOneIntFor("RandomWalkIndexHighIndicator_1",20);
        int rwLowTimeFrame = parameter.getOneIntFor("RandomWalkIndexLowIndicator_1",20);
        List<Indicator> ilRw = new ArrayList<>();
        List<String> nlRw = new ArrayList<>();

        ilRw.add(new RandomWalkIndexHighIndicator(series, rwHighTimeFrame));
        ilRw.add(new RandomWalkIndexLowIndicator(series, rwLowTimeFrame));
        nlRw.add("Random Walk High " + rwHighTimeFrame);
        nlRw.add("Random Walk Low " + rwLowTimeFrame );
        XYLineAndShapeRenderer randWalkRenderer = new XYLineAndShapeRenderer();
        randWalkRenderer.setSeriesPaint(0, Color.GREEN);
        randWalkRenderer.setSeriesStroke(0,TaTypes.SMALL_LINE);
        randWalkRenderer.setSeriesShape(0, TaTypes.NONE);
        randWalkRenderer.setSeriesPaint(1, Color.RED);
        randWalkRenderer.setSeriesStroke(1,TaTypes.SMALL_LINE);
        randWalkRenderer.setSeriesShape(1, TaTypes.NONE);
        addChartIndicator(ilRw,nlRw,"Random Walk "+rwHighTimeFrame+" "+rwLowTimeFrame,randWalkRenderer,true, TaTypes.categories.DEFAULT);



        // AccumulationDistributionIndicator
        addChartIndicator(new AccumulationDistributionIndicator(series),"Accumulation Distribution", true, TaTypes.categories.VOLUME);

        // Chaikin Money Flow Indicator
        int ChaikinMoneyFlowIndicator_1 = parameter.getOneIntFor("ChaikinMoneyFlowIndicator_1",20);
        addChartIndicator(new ChaikinMoneyFlowIndicator(series,ChaikinMoneyFlowIndicator_1),
                "Chaikin Money "+ChaikinMoneyFlowIndicator_1, true, TaTypes.categories.VOLUME);





    }
        */
    private void addChartIndicator(String identifier, List<Indicator> indicators, List<String> names, String generalName,XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c){
        chartIndicatorMap.put(identifier, new ChartIndicator(indicators,names,generalName,renderer,isSubchart,c));
    }

    public void addIndicator(String identifier, List<Indicator> indicators, List<String> names, String generalName,XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c){
        //chartIndicatorMap.put(identifier,new ChartIndicator(indicators,names,generalName,renderer,isSubchart,c));
        tempChartIndicatorBackup.put(identifier,new ChartIndicator(indicators,names,generalName,renderer,isSubchart,c));

    }

    /**
     * Build and add an org.sjwimmer.tacharting.chart indicator to charts indicator list (random color, default name)
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on sub org.sjwimmer.tacharting.chart
     */
    private void addChartIndicator(Indicator indicator, boolean isSubchart){
         chartIndicatorMap.put(indicator.toString(),new ChartIndicator(indicator, indicator.toString(), isSubchart, DEFAULT));
    }

    /**
     * Build and add an org.sjwimmer.tacharting.chart indicator to charts indicator list (random color, default name)
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on sub org.sjwimmer.tacharting.chart
     */
    public void addIndicator(Indicator indicator, boolean isSubchart){
        //chartIndicatorMap.put(indicator.toString(),new ChartIndicator(indicator, indicator.toString(), isSubchart, DEFAULT));
        tempChartIndicatorBackup.put(indicator.toString(),new ChartIndicator(indicator, indicator.toString(), isSubchart, DEFAULT));
    }

    /**
     * Build and add an org.sjwimmer.tacharting.chart indicator to charts indicator list (random color, default name)
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on su bchart
     * @param c the category colorOf the org.sjwimmer.tacharting.chart
     */
    private void addChartIndicator(String identifier, Indicator indicator, boolean isSubchart, IndicatorCategory c){
        chartIndicatorMap.put(identifier, new ChartIndicator(indicator,indicator.toString(), isSubchart, c));
    }

    /**
     * Build and add an org.sjwimmer.tacharting.chart indicator to charts indicator list (random color, default name)
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on su bchart
     * @param c the category colorOf the org.sjwimmer.tacharting.chart
     */
    public void addIndicator(String identifier, Indicator indicator, boolean isSubchart, IndicatorCategory c){
        //chartIndicatorMap.put(identifier, new ChartIndicator(indicator,indicator.toString(), isSubchart, c));
        tempChartIndicatorBackup.put(identifier, new ChartIndicator(indicator,indicator.toString(), isSubchart, c));
    }

    /**
     * Build and add an org.sjwimmer.tacharting.chart indicator to charts indicator list
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on sub org.sjwimmer.tacharting.chart
     * @param name the name colorOf the indicator that should be displayed
     * @param c the category colorOf the org.sjwimmer.tacharting.chart
     */
    private void addChartIndicator(String identifier, Indicator indicator, String name, boolean isSubchart, IndicatorCategory c){
        chartIndicatorMap.put(identifier, new ChartIndicator(indicator, name, isSubchart, c));
        //tempChartIndicatorBackup.put(identifier, new ChartIndicator(indicator, name, isSubchart, c));
    }

    public void addIndicator(String identifier, Indicator indicator, String name, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c){
        //addIndicator(identifier, new ChartIndicator(indicator, name, renderer, isSubchart, c));
        tempChartIndicatorBackup.put(identifier, new ChartIndicator(indicator, name, renderer, isSubchart, c));
    }

    private void addChartIndicator(String identifier, Indicator indicator, String name, XYLineAndShapeRenderer renderer, boolean isSubchart, IndicatorCategory c){
        chartIndicatorMap.put(identifier, new ChartIndicator(indicator, name, renderer, isSubchart, c));
        //tempChartIndicatorBackup.put(identifier,new ChartIndicator(indicator,name,renderer,isSubchart,c));
    }

    /**
     * Adds an existing ChartIndicator to the box
     * @param chartIndicator an ChartIndicator
     */
    private void addChartIndicator(String identifier, ChartIndicator chartIndicator){
        chartIndicatorMap.put(identifier, chartIndicator);
    }

    /**
     * Adds an existing ChartIndicator to the box
     * @param chartIndicator an ChartIndicator
     */
    public void addIndicator(String identifier, ChartIndicator chartIndicator){
        //addChartIndicator(identifier, chartIndicator);
        tempChartIndicatorBackup.put(identifier,chartIndicator);
    }

    /**
     * Removes an (loaded) indicator from {@link #chartIndicatorMap}
     * @implNote does not remove the at runtime added indicators
     * @param key the key colorOf the indicator
     */
    public void removeIndicator(String key){
        this.chartIndicatorMap.remove(key);
    }

    /**
     * Get all indicators that are stored in this box
     * @return all ChartIndicators that are stored in this box
     */
    public ObservableMap<String, ChartIndicator> getChartIndicatorMap() {
        return chartIndicatorMap;
    }

    /**
     * Returns the indicator that is stored for the identifier
     * @param identifier the identifier colorOf the indicator (display identifier/general identifier/properties identifier)
     * @return the indicator that is stored for the identifier
     */
    public ChartIndicator getChartIndicator(String identifier){
        return this.chartIndicatorMap.get(identifier);
    }

    public PropertiesManager getPropertiesManager(){
        return this.parameter;
    }

    /**
     * Reload all indicators in {@link #chartIndicatorMap}.
     */
    public void reloadAll(){
        Iterator<Map.Entry<String, ChartIndicator>> it = this.chartIndicatorMap.entrySet().iterator();
        while(it.hasNext()){
            String key = it.next().getKey();
            try{
                reloadIndicator(key);
            } catch (XPathException xpe){
                removeIndicator(key); // could not be loaded, remove indicator from internal list
                //TODO: handle
            }

        }
    }

    //TODO: implement public initXXXIndicator(Object params..) functions for creating "subindicators" e.g. keltner, bollinger..
    /**
     * Reloads the indicator from the properties file
     * @param key
     */
    public void reloadIndicator(String key) throws IllegalArgumentException, XPathException{
        String indicatorName = key.split("_")[0];

        switch (indicatorName){
            case "SMAIndicator": {
                loadSMAIndicator(key);
                break;
            }
            case "CCIIndicator":{
                loadCCIIndicator(key);
                break;
            }
            case "EMAIndicator":{
                loadEMAIndicator(key);
                break;
            }
            case "CMOIndicator":{
                loadCMOIndicator(key);
                break;
            }
            case "BollingerBands": {
                loadBollingerBands(key);
                break;
            }
            case "PercentBIIndicator":{
                loadPercentBIndicator(key);
                break;
            }
            case "AmountIndicator":{
                loadAmountIndicator(key);
                break;
            }

            case "RSIIndicator":{
                loadRSIIndicator(key);
                break;
            }
            case "SmoothedRSIIndicator":{
                loadSmoothedRSIIndicator(key);
                break;
            }

            case "PVIIndicator":{
                loadPVIIndicator(key);
                break;
            }
            case "NVIIndicator":{
                loadNVIIndicator(key);
                break;
            }
            case "OnBalanceVolumeIndicator":{
                loadOnBalanceVolumeIndicator(key);
                break;
            }
            case "MACDIndicator":{
                loadMACDIndicator(key);
                break;
            }
            case "AverageDirectionalMovementUpDown":{
                loadAverageDirectionalMovementUP_DOWN(key);
                break;
            }
            case "TrueRangeIndicator":{
                loadTrueRangeIndicator(key);
                break;
            }
            case "AroonUpDown":{
                loadAroonUP_DOWN(key);
                break;
            }
            case "Keltner":{
                loadKeltner(key);
                break;
            }

            case "LowerShadowIndicator":{
                loadLowerShadowIndicator(key);
                break;
            }
            case "MVWAPIndicator":{
                loadMVWAPIndicator(key);
                break;
            }
             case "RealBodyIndicator":{
                loadRealBodyIndicator(key);
                break;
            }
            case "UpperShadowIndicator":{
                loadUpperShadowIndicator(key);
                break;
            }
            case "TrailingStopLossIndicator":{
                loadTraillingStopLossIndicator(key);
                break;
            }
            case "TripleEMAIndicator":{
                loadTrippleEMAIndicator(key);
                break;
            }
            case "UlcerIndexIndicator":{
                loadUlcerIndexIndicator(key);
                break;
            }
            case "VWAPIndicator":{
                loadVWAPIndicator(key);
                break;
            }
            case "WMAIndicator":{
                loadWMAIndicator(key);
                break;
            }
            case "ZLEMAIndicator":{
                loadZLEMAIndicator(key);
                break;
            }
               case "RAVIIndicator":{
                loadRAVIIndicator(key);
                break;
            }
            case "ROCIndicator":{
                loadROCIndicator(key);
                break;
            }
            case "HMAIndicator":{
                loadHMAIndicator(key);
                break;
            }
            case "FisherIndicator":{
                loadFischerIndicator(key);
                break;
            }
            case "KAMAIndicator":{
                loadKAMAIndicator(key);
                break;
            }
            case "PreviousValueIndicator":{
                loadPreviousValueIndicator(key);
                break;
            }
            case "StochasticRSIIndicator":{
                loadStochasticRSIIndicator(key);
                break;
            }
            case "StochasticOscillatorK(OscillatorD)":{
                loadStochasticOscillatorKIndicatorStochasticOscillatorDIndicator(key);
                break;
            }

            default:
                // indicator not in xml, maybe it was added at runtime dynamically?
                ChartIndicator dynIndicator = tempChartIndicatorBackup.get(key);
                if(dynIndicator != null){
                    chartIndicatorMap.put(key, dynIndicator.clone()); // fake "reload" to notify all observers
                }else{ // there is no indicator with that key
                    throw new IllegalArgumentException(key+ " could not be loaded!");
                }


        }
    }

    /**
     *
     * @param key key colorOf an indicator (instance)
     * @return the id colorOf an indiator instance
     */
    private int getID(String key) {
        return Integer.parseInt(key.split("_")[1]);
    }

    /**
     *
     * @param key key colorOf an indicator (instance)
     * @return the identifier colorOf xml indicator
     */
    private String getIdentifier(String key){
        return key.split("_")[0];
    }


    private XYLineAndShapeRenderer createRenderer(Paint p, StrokeType stroke, ShapeType shape){
        boolean isShape = !shape.equals(ShapeType.NONE);
        return createRenderer(p, stroke.stroke, shape.shape,isShape);
    }

    private XYLineAndShapeRenderer createRenderer(String key, String color, String shape, String stroke) throws XPathExpressionException {
        Color c = FormatUtils.ColorAWTConverter.fromString(parameter.getParameter(key,color));
        StrokeType st = StrokeType.valueOf(parameter.getParameter(key,stroke));
        ShapeType sh = ShapeType.valueOf(parameter.getParameter(key,shape));
        return createRenderer(c,st,sh);
    }
    //TODO: add more features in xml: lines, Based on indicator
    //TODO: add createRenderer function for several lines like itchimoku needs
    private XYLineAndShapeRenderer createRenderer(Color color, StrokeType stroke, ShapeType shape){
        boolean isShape = !shape.equals(ShapeType.NONE);
        return createRenderer(color,stroke.stroke,shape.shape,isShape);
    }


    private XYLineAndShapeRenderer createRenderer(Paint p, Stroke s, Shape sh, boolean isShape){
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, isShape);
        if(isShape){
            renderer.setSeriesShape(0,sh);
        }
        renderer.setSeriesPaint(0,p);
        renderer.setSeriesStroke(0,s);
        return renderer;

    }
}
