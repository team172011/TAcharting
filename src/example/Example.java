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
import chart.TaTypes;
import eu.verdelhan.ta4j.*;
import eu.verdelhan.ta4j.indicators.EMAIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.CrossedUpIndicatorRule;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Example {

    public static void main(String[] args){

        // get a time series
        TimeSeries series = Loader.getDailyTimeSerie("C:\\Users\\simon\\Java_Projekte\\ta4j-charting\\src\\data\\aapl_daily.csv", "APPL");

        // define indicators
        ClosePriceIndicator cp = new ClosePriceIndicator(series);
        EMAIndicator ema20 = new EMAIndicator(cp,20);
        EMAIndicator ema60 = new EMAIndicator(cp, 60);

        // build strategy
        Rule entry = new CrossedUpIndicatorRule(ema20,ema60);
        Rule exit = new CrossedDownIndicatorRule(ema20, ema60);
        BaseStrategy strategy = new BaseStrategy(entry,exit);

        // initialize and add your individual indicators to chart
        TaChartIndicatorBox chartIndicatorBox = new TaChartIndicatorBox();
        chartIndicatorBox.initAllIndicators(series); // add all ta4j indicators to the box
        chartIndicatorBox.addChartIndicator(ema20, Color.GREEN,"myEMA Short (20)", false, TaTypes.categories.DEFAULT);
        chartIndicatorBox.addChartIndicator(ema60, Color.RED,"myEMA Long (60)", false, TaTypes.categories.DEFAULT);

        // or add your whole strategy as
        List<Indicator> strategyIndicators = new ArrayList<>();
        List<Paint> strategyColors = new ArrayList<>();
        List<String> names = new ArrayList<>();
        strategyIndicators.add(ema20);
        strategyIndicators.add(ema60);
        strategyColors.add(Color.MAGENTA);
        strategyColors.add(Color.YELLOW);
        names.add("myEma (20)");
        names.add("myEma (60)");
        chartIndicatorBox.addChartIndicator(strategyIndicators,strategyColors,names,"my Strategy Ema Short/Long", false, TaTypes.categories.DEFAULT);



        // run the strategy
        TimeSeriesManager manager = new TimeSeriesManager(series);
        TradingRecord record = manager.run(strategy);

        //plot series, trading record and other indicators
        TaChart taChartPanel = new TaChart(series,record,chartIndicatorBox);
        taChartPanel.setVisible(true);

    }
}


