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

import javafx.stage.Stage;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.sjwimmer.tacharting.chart.api.BaseIndicatorParameterManager;
import org.sjwimmer.tacharting.chart.api.IndicatorParameterManager;
import org.sjwimmer.tacharting.chart.model.BaseIndicatorBox;
import org.sjwimmer.tacharting.chart.model.IndicatorBox;
import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.model.types.ShapeType;
import org.sjwimmer.tacharting.example.Loader;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

import java.awt.*;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;


public class ProgramStart extends AbstractProgram {

    /**
     * This method can be overwritten to get custom {@link BaseIndicatorBox} with custom {@link Indicator indicators},
     * {@link Strategy strategies} and {@link TradingRecord}. It is also possible to add a custom {@link IndicatorParameterManager}
     * to load and store the indicator parameters in custom way
     * @return a {@link BaseIndicatorBox} for the Chart that is used in the {@link #start(Stage) start(Stage) function}
     */
    @Override
    public IndicatorBox createIndicatorBox() {

        TimeSeries series = Loader.getDailyTimeSeries("fb_daily.csv");

        // define indicators
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        // build a strategy
        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd);
        Rule exitRule = new CrossedDownIndicatorRule(macd, emaMacd);
        Strategy myStragety = new  BaseStrategy(entryRule, exitRule);


        // initialize and add your indicators to the ChartIndicatorBox
        TaTimeSeries taTimeSeries = new TaTimeSeries(series, Currency.getInstance("USD"), GeneralTimePeriod.DAY);
        IndicatorBox chartIndicatorBox = new BaseIndicatorBox(taTimeSeries, new BaseIndicatorParameterManager());

        // two indicators in one subplot:
        XYLineAndShapeRenderer macRenderer = new XYLineAndShapeRenderer(); // specify how the lines should be rendered
        macRenderer.setSeriesShape(0, ShapeType.NONE.shape);
        macRenderer.setSeriesPaint(0,Color.RED);
        macRenderer.setSeriesShape(1,ShapeType.NONE.shape);
        macRenderer.setSeriesPaint(1,Color.GREEN);

        List<Indicator> indicatorList = Arrays.asList(macd,emaMacd);
        List<String> nameList = Arrays.asList("macd","emaMacd");
        //chartIndicatorBox.addIndicator("Straregy_1",indicatorList,nameList,"My macd/emaMacd Strategy",macRenderer,true,IndicatorCategory.CUSTOM);


        // add strategies
        chartIndicatorBox.addStrategy("My ema/emaMacd Strategy", myStragety);
        return chartIndicatorBox;
    }
}


