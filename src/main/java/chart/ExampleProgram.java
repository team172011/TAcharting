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

package chart;

import chart.parameters.IndicatorParameters.IndicatorCategory;
import chart.parameters.IndicatorParameters.TaShape;
import example.Loader;
import javafx.stage.Stage;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class ExampleProgram extends AbstractProgram {

    /**
     * This method can be overwritten to get custom {@link ChartIndicatorBox} with custom {@link Indicator indicators},
     * {@link Strategy strategies} and {@link TradingRecord}
     * @return a {@link ChartIndicatorBox} for the Chart that is used in the {@link #start(Stage) start(Stage) function}
     */
    @Override
    public ChartIndicatorBox createIndicatorBox() {

        // load a TimeSeries
        ClassLoader cl = getClass().getClassLoader();
        URL file = cl.getResource("fb_daily.csv");
        TimeSeries series = Loader.getDailyTimeSeries(file, "fb");

        // define indicators
        ClosePriceIndicator cp = new ClosePriceIndicator(series);
        EMAIndicator ema20 = new EMAIndicator(cp,20);
        EMAIndicator ema60 = new EMAIndicator(cp, 60);

        // build a strategy
        Rule entry = new CrossedUpIndicatorRule(ema20,ema60);
        Rule exit = new CrossedDownIndicatorRule(ema20, ema60);
        BaseStrategy strategyLong = new BaseStrategy(entry,exit);
        BaseStrategy strategyShort = new BaseStrategy(exit, entry);

        // initialize and add your individual indicators to chart
        ChartIndicatorBox chartIndicatorBox = new ChartIndicatorBox(series);

        XYLineAndShapeRenderer emaShortRenderer = new XYLineAndShapeRenderer(); // specify how the lines should be rendered
        emaShortRenderer.setSeriesShape(0, TaShape.NONE.getShape());
        emaShortRenderer.setSeriesPaint(0,Color.RED);

        XYLineAndShapeRenderer emaLongRenderer = new XYLineAndShapeRenderer();
        emaLongRenderer.setSeriesShape(0,TaShape.NONE.getShape());
        emaLongRenderer.setSeriesPaint(0,Color.GREEN);

        chartIndicatorBox.addIndicator("ema1",ema20, "myEMA Short (20)",emaShortRenderer, false, IndicatorCategory.CUSTOM);
        chartIndicatorBox.addIndicator("ema2",ema60, "myEMA Long (60)",emaLongRenderer, false, IndicatorCategory.CUSTOM);

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
        chartIndicatorBox.addIndicator("Strategy1",strategyIndicators, names,"my Strategy Ema Short/Long",
                strategieRenderer,false, IndicatorCategory.STRATEGY);

        // run the strategies and add strategies
        TimeSeriesManager manager = new TimeSeriesManager(series);
        TradingRecord record = manager.run(strategyLong);
        TradingRecord record2 = manager.run(strategyShort, Order.OrderType.SELL);
        chartIndicatorBox.addTradingRecord("My Record Long", record);
        chartIndicatorBox.addTradingRecord("My Record Short", record2);
        return chartIndicatorBox;
    }
}


