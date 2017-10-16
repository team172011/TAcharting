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

package example;

import chart.TaChart;
import chart.TaChartIndicatorBox;
import chart.types.IndicatorParameters.TaCategory;
import chart.types.IndicatorParameters.TaShape;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Example {
    public TimeSeries series;

    public static void main(String[] args){


        Example example1 = new Example();
    }

    public Example(){
        // get a time series
        ClassLoader cl = getClass().getClassLoader();
        URL file = cl.getResource("data/fb_daily.csv");
        series = Loader.getDailyTimeSerie(file, "fb");

        createChart();

    }

    public void createChart() {

        // define indicators
        ClosePriceIndicator cp = new ClosePriceIndicator(series);
        EMAIndicator ema20 = new EMAIndicator(cp,20);
        EMAIndicator ema60 = new EMAIndicator(cp, 60);

        // build strategy
        Rule entry = new CrossedUpIndicatorRule(ema20,ema60);
        Rule exit = new CrossedDownIndicatorRule(ema20, ema60);
        BaseStrategy strategyLong = new BaseStrategy(entry,exit);
        BaseStrategy strategyShort = new BaseStrategy(exit, entry);

        // initialize and add your individual indicators to chart
        TaChartIndicatorBox chartIndicatorBox = new TaChartIndicatorBox(series);
        //chartIndicatorBox.initIndicatorsFromPropertyFile(); // add all ta4j indicators to the box

        XYLineAndShapeRenderer emaShortRenderer = new XYLineAndShapeRenderer(); // specify how the lines should be rendered
        emaShortRenderer.setSeriesShape(0, TaShape.NONE.getShape());
        emaShortRenderer.setSeriesPaint(0,Color.RED);

        XYLineAndShapeRenderer emaLongRenderer = new XYLineAndShapeRenderer();
        emaLongRenderer.setSeriesShape(0,TaShape.NONE.getShape());
        emaLongRenderer.setSeriesPaint(0,Color.GREEN);

        chartIndicatorBox.addChartIndicator("ema1",ema20, "myEMA Short (20)",emaShortRenderer, false, TaCategory.DEFAULT);
        chartIndicatorBox.addChartIndicator("ema2",ema60, "myEMA Long (60)",emaLongRenderer, false, TaCategory.DEFAULT);

        // or add your whole strategy as
        List<Indicator> strategyIndicators = new ArrayList<>();
        List<String> names = new ArrayList<>();
        strategyIndicators.add(ema20);
        strategyIndicators.add(ema60);
        names.add("myEma (20)");
        names.add("myEma (60)");
        XYLineAndShapeRenderer strategieRenderer = new XYLineAndShapeRenderer();
        strategieRenderer.setSeriesShape(0,TaShape.NONE.getShape()); // specify how the both lines should be rendered
        strategieRenderer.setSeriesPaint(0,Color.RED);
        strategieRenderer.setSeriesShape(1,TaShape.NONE.getShape());
        strategieRenderer.setSeriesPaint(1,Color.GREEN);
        chartIndicatorBox.addChartIndicator("Strategy1",strategyIndicators, names,"my Strategy Ema Short/Long",
                strategieRenderer,false, TaCategory.DEFAULT);

        // run the strategies
        TimeSeriesManager manager = new TimeSeriesManager(series);
        TradingRecord record = manager.run(strategyLong);
        TradingRecord record2 = manager.run(strategyShort, Order.OrderType.SELL);
        chartIndicatorBox.addTradingRecord("My Record Long", record);
        chartIndicatorBox.addTradingRecord("My Record Short", record2);

        //plot series, trading record and other indicators
        TaChart taChartPanel = new TaChart(series,chartIndicatorBox);
        taChartPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        taChartPanel.setVisible(true);
    }
}


