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

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.*;
import eu.verdelhan.ta4j.indicators.bollinger.*;
import eu.verdelhan.ta4j.indicators.candles.LowerShadowIndicator;
import eu.verdelhan.ta4j.indicators.candles.RealBodyIndicator;
import eu.verdelhan.ta4j.indicators.candles.UpperShadowIndicator;
import eu.verdelhan.ta4j.indicators.helpers.*;
import eu.verdelhan.ta4j.indicators.ichimoku.IchimokuKijunSenIndicator;
import eu.verdelhan.ta4j.indicators.ichimoku.IchimokuSenkouSpanAIndicator;
import eu.verdelhan.ta4j.indicators.ichimoku.IchimokuSenkouSpanBIndicator;
import eu.verdelhan.ta4j.indicators.ichimoku.IchimokuTenkanSenIndicator;
import eu.verdelhan.ta4j.indicators.keltner.KeltnerChannelLowerIndicator;
import eu.verdelhan.ta4j.indicators.keltner.KeltnerChannelMiddleIndicator;
import eu.verdelhan.ta4j.indicators.keltner.KeltnerChannelUpperIndicator;
import eu.verdelhan.ta4j.indicators.statistics.*;
import eu.verdelhan.ta4j.indicators.volume.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TaChartIndicatorBox {
    List<TaChartIndicator> taChartIndicatorList;


    /**
     * Constructor
     */
    public TaChartIndicatorBox(){
        taChartIndicatorList = new ArrayList<>();
    }

    /**
     * Creates and add all ta4j indicators with generic type Decimal to the box.
     * @param series the time serie that will be plotted and analyzed.
     */
    public void initAllIndicators(TimeSeries series){

        // closePrice
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
        addChartIndicator(closePriceIndicator,false, TaTypes.categories.HELPERS);

        // simple moving average
        addChartIndicator(new SMAIndicator(closePriceIndicator, 20),Color.BLUE,"SMA (20)",false,
                TaTypes.categories.DEFAULT);

        // exponential moving average
        addChartIndicator(new EMAIndicator(closePriceIndicator, 20),Color.BLUE,"EMA (20)",false,
                TaTypes.categories.DEFAULT);

        // TaChartIndicator that need several indicators
        List<Indicator> indicatorList = new ArrayList<>();
        List<Paint> colorList = new ArrayList<>();
        List<String> namesList = new ArrayList<>();

        // Bollinger Bands
        StandardDeviationIndicator standardDeviation = new StandardDeviationIndicator(closePriceIndicator, 20);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(closePriceIndicator);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, standardDeviation);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, standardDeviation);

        indicatorList.add(bbm);
        indicatorList.add(bbu);
        indicatorList.add(bbl);

        colorList.add(Color.YELLOW);
        colorList.add(Color.BLUE);
        colorList.add(Color.BLUE);

        namesList.add("Middle Band");
        namesList.add("Upper Band");
        namesList.add("Lower Band");
        addChartIndicator(indicatorList,colorList,namesList,"Bollinger Bands (20, 2)",false,
                TaTypes.categories.BOLLINGER);

        // Bollinger bands width
        addChartIndicator(new BollingerBandWidthIndicator(bbu, bbm, bbl), Color.BLUE,"Bollinger Band Width", true,
                TaTypes.categories.BOLLINGER);

        //Percent BI
        PercentBIndicator pbi = new PercentBIndicator(closePriceIndicator, 20, eu.verdelhan.ta4j.Decimal.TWO);
        addChartIndicator(pbi  , Color.LIGHT_GRAY,"Percent BI (20)", true,
                TaTypes.categories.BOLLINGER);

        // Lower Shadown Indicator
        addChartIndicator(new LowerShadowIndicator(series), true, TaTypes.categories.CANDELS);

        // Real Body Indicator
        addChartIndicator(new RealBodyIndicator(series), true, TaTypes.categories.CANDELS);

        //Upper Shadow Indicator
        addChartIndicator(new UpperShadowIndicator(series), true, TaTypes.categories.CANDELS);

        //Amount Indicator
        addChartIndicator(new AmountIndicator(series), true, TaTypes.categories.HELPERS);

        //Average Directional Movement Down and Up
        List<Indicator> ilAdx = new ArrayList<>();
        List<Paint> clAdx = new ArrayList<>();
        List<String> nlAdx = new ArrayList<>();

        ilAdx.add(new AverageDirectionalMovementDownIndicator(series, 20));
        ilAdx.add(new AverageDirectionalMovementUpIndicator(series, 20));
        clAdx.add(Color.RED);
        clAdx.add(Color.GREEN);
        nlAdx.add("ADX Down (20)");
        nlAdx.add("ADX UP (20)");
        addChartIndicator(ilAdx,clAdx,nlAdx,"ADX UP/DOWN (20)",true, TaTypes.categories.HELPERS);

        // Average Gain indicator
        addChartIndicator(new AverageGainIndicator(closePriceIndicator, 20), true,
                TaTypes.categories.HELPERS);

        // Average Loss indicator
        addChartIndicator(new AverageLossIndicator(closePriceIndicator, 20), true,
                TaTypes.categories.HELPERS);

        // Average True Range indicator
        addChartIndicator(new AverageTrueRangeIndicator(series, 20), true,
                TaTypes.categories.HELPERS);

        // Close Location Value indicator
        addChartIndicator(new CloseLocationValueIndicator(series), true,
                TaTypes.categories.HELPERS);

        // Constant Indicator TODO: does not work, time series is null
        // buildChartIndicator(new ConstantIndicator(closePriceIndicator.getValue(0)),Color.RED,"Constant First CP", false);

        // Cumulated Gains Indicator
        addChartIndicator(new CumulatedGainsIndicator(closePriceIndicator,20), true,
                TaTypes.categories.HELPERS);

        // Cumulated Losses Indicator
        addChartIndicator(new CumulatedLossesIndicator(closePriceIndicator,20), true,
                TaTypes.categories.HELPERS);

        // Directional Up and Down Indicator

        List<Indicator> ilDud = new ArrayList<>();
        ilDud.add(new DirectionalDownIndicator(series,20));
        ilDud.add(new DirectionalUpIndicator(series,20));
        List<Paint> clDud = new ArrayList<>();
        clDud.add(Color.RED);
        clDud.add(Color.GREEN);
        List<String> nlDud = new ArrayList<>();
        nlDud.add("Directional Down (20)");
        nlDud.add("Directional Up (20)");
        addChartIndicator(ilDud, clDud, nlDud, "Directional Up/Down (20)", true,
                TaTypes.categories.HELPERS);


        //Directional Movement Down and UP
        List<Indicator> ilDmud = new ArrayList<>();
        ilDmud.add(new DirectionalMovementDownIndicator(series));
        ilDmud.add(new DirectionalMovementUpIndicator(series));
        List<Paint> clDmud = new ArrayList<>();
        clDmud.add(Color.RED);
        clDmud.add(Color.GREEN);
        List<String> nlmDud = new ArrayList<>();
        nlmDud.add("Directional Movement Down");
        nlmDud.add("Directional Movement Up");
        addChartIndicator(ilDmud, clDmud, nlmDud, "Directional Movement Up/Down", true,
                TaTypes.categories.HELPERS);

        // Highest Value Indicator
        addChartIndicator(new HighestValueIndicator(closePriceIndicator, 20),Color.CYAN, "Highest Value (20)",false, TaTypes.categories.HELPERS);

        // Lowest Value Indicator
        addChartIndicator(new LowestValueIndicator(closePriceIndicator, 20),Color.red,"Lowest Value (20)",false, TaTypes.categories.HELPERS);

        // Max Price Indicator
        addChartIndicator(new MaxPriceIndicator(series),Color.orange,"Max Price Indicator",false, TaTypes.categories.HELPERS);

        // Mean Deviation Indicator
        addChartIndicator(new MeanDeviationIndicator(closePriceIndicator, 20),Color.ORANGE,
                "Mean Deciation (20)",false, TaTypes.categories.HELPERS);

        // Mean Price Indicator
        addChartIndicator(new MedianPriceIndicator(series),false, TaTypes.categories.HELPERS);

        // Median Price Indicator
        addChartIndicator(new MedianPriceIndicator(series),false, TaTypes.categories.HELPERS);

        // Open price Indicator
        addChartIndicator(new OpenPriceIndicator(series),false, TaTypes.categories.HELPERS);

        // Previous Value Indicator
        addChartIndicator(new PreviousValueIndicator(closePriceIndicator, 5), Color.RED,
                "Previous Value (cp, 5)", false, TaTypes.categories.HELPERS);

        // Price Variantion Indicator
        addChartIndicator(new PriceVariationIndicator(series),true, TaTypes.categories.HELPERS);

        // Smoothed Average Gain Indicator
        addChartIndicator(new SmoothedAverageGainIndicator(closePriceIndicator,20),true, TaTypes.categories.HELPERS);

        // Smoothed Average Gain Indicator
        addChartIndicator(new SmoothedAverageLossIndicator(closePriceIndicator,20),true, TaTypes.categories.HELPERS);

        // Trade Count //TODO: integer does not work
        //addChartIndicator(new TradeCountIndicator(series),true, TaTypes.categories.HELPERS);

        // True Range Indicator
        addChartIndicator(new TrueRangeIndicator(series),true, TaTypes.categories.HELPERS);

        // Typical Price Indicator
        addChartIndicator(new TypicalPriceIndicator(series),false, TaTypes.categories.HELPERS);

        // Volume Indicator
        addChartIndicator(new VolumeIndicator(series),true, TaTypes.categories.HELPERS);

        //ichimoku
        List<Indicator> ilIchi = new ArrayList<>();
        IchimokuKijunSenIndicator kijunSen = new IchimokuKijunSenIndicator(series, 26);
        IchimokuTenkanSenIndicator tenkanSen = new IchimokuTenkanSenIndicator(series, 26);

        ilIchi.add(new IchimokuSenkouSpanAIndicator(series, tenkanSen, kijunSen));
        ilIchi.add(new IchimokuSenkouSpanBIndicator(series, 52));
        ilIchi.add(kijunSen);
        ilIchi.add(tenkanSen);
        List<Paint> clIchi = new ArrayList<>();
        clIchi.add(Color.GREEN);
        clIchi.add(Color.RED);
        clIchi.add(Color.RED.brighter());
        clIchi.add(Color.BLUE);
        List<String> nlIchi = new ArrayList<>();
        nlIchi.add("Senkou Span A (TenkanSen, KijunSen)"); // no idea what i am doing^^
        nlIchi.add("Senkou Span B (52) ");
        nlIchi.add("KijunSen (26)");
        nlIchi.add("TenkanSen (26) ");
        addChartIndicator(ilIchi,clIchi,nlIchi,"Ichimoku All", false, TaTypes.categories.ICHIMOKU);

        addChartIndicator(kijunSen, false, TaTypes.categories.ICHIMOKU);
        addChartIndicator(tenkanSen, false, TaTypes.categories.ICHIMOKU);

        //keltner
        KeltnerChannelMiddleIndicator kcM = new KeltnerChannelMiddleIndicator(series,20);
        KeltnerChannelLowerIndicator kcL = new KeltnerChannelLowerIndicator(kcM,Decimal.TWO,20);
        KeltnerChannelUpperIndicator kcU = new KeltnerChannelUpperIndicator(kcM,Decimal.TWO,20);

        List<Indicator> ilKelt = new ArrayList<>();
        List<Paint> clKelt = new ArrayList<>();
        List<String> nlKelt = new ArrayList<>();
        ilKelt.add(kcL);
        ilKelt.add(kcM);
        ilKelt.add(kcU);
        clKelt.add(Color.BLUE);
        clKelt.add(Color.BLUE.brighter());
        clKelt.add(Color.BLUE);
        nlKelt.add("Keltner Lower");
        nlKelt.add("Keltner Middle");
        nlKelt.add("Keltner Uppet");
        addChartIndicator(ilKelt, clKelt, nlKelt,"Keltner Channel (20, 2, 20)",false, TaTypes.categories.KELTNER);
        addChartIndicator(kcM, Color.BLUE.brighter(),"Keltner Middle (2, 20)",false, TaTypes.categories.KELTNER);
        addChartIndicator(kcU, Color.BLUE.brighter(),"Keltner Upper (2, 20)",false, TaTypes.categories.KELTNER);
        addChartIndicator(kcL, Color.BLUE.brighter(),"Keltner Lower (2, 20)",false, TaTypes.categories.KELTNER);

        // Correlation Coefficient Indicator
        addChartIndicator(new CorrelationCoefficientIndicator(closePriceIndicator,new MinPriceIndicator(series),20),
                Color.RED, "Correlation Coefficient (cp, minP, 20),",true, TaTypes.categories.STATISTICS);

        // Covariance Indicator
        addChartIndicator(new CovarianceIndicator(closePriceIndicator,new MinPriceIndicator(series),20),
                Color.RED, "Covariance Indicator (cp, minP, 20),",true, TaTypes.categories.STATISTICS);

        // Period Growth Rate Indicator
        addChartIndicator(new PeriodicalGrowthRateIndicator(closePriceIndicator,20),Color.BLUE,"Period Growth Rate (cp, 20)",true, TaTypes.categories.STATISTICS);

        // Simple Linear Regression Indicator
        addChartIndicator(new SimpleLinearRegressionIndicator(closePriceIndicator,5),Color.MAGENTA, "Simple Linear Regression(cp, 5)",true, TaTypes.categories.STATISTICS);

        // Standard Deviatation Indicator
        addChartIndicator(new StandardDeviationIndicator(closePriceIndicator, 20),
                Color.MAGENTA, "Standard Deviatation (cp, 20)", true, TaTypes.categories.STATISTICS);

        addChartIndicator(new StandardErrorIndicator(closePriceIndicator,20), Color.RED,"Standard Error (cp, 20)", true, TaTypes.categories.STATISTICS);

        addChartIndicator(new VarianceIndicator(closePriceIndicator,20), Color.RED,"Variance (cp, 20)", true, TaTypes.categories.STATISTICS);

        //Accerleration Deceleration Indicator
        addChartIndicator(new AccelerationDecelerationIndicator(series,20,50), Color.YELLOW,"Accel. Decel.(20,50)", true, TaTypes.categories.DEFAULT);

        // Arron Up/Down in one subplot
        List<Indicator> ilAroon = new ArrayList<>();
        List<Paint> clAroon = new ArrayList<>();
        List<String> nlAroon = new ArrayList<>();
        ilAroon.add(new AroonDownIndicator(series, 20));
        ilAroon.add(new AroonUpIndicator(series, 20));
        nlAroon.add("Aroon Down 20");
        nlAroon.add("Aroon Up 20");
        clAroon.add(Color.RED);
        clAroon.add(Color.GREEN);
        addChartIndicator(ilAroon,clAroon,nlAroon,"Aroon UP/DOWN (20)",true,
                TaTypes.categories.DEFAULT);

        // Average Directional Movement
        addChartIndicator(new AverageDirectionalMovementDownIndicator(series, 20),
                Color.ORANGE,"ADX (20)",true, TaTypes.categories.DEFAULT);

        // Awesome Oscillator
        addChartIndicator(new AwesomeOscillatorIndicator(closePriceIndicator),
                Color.ORANGE,"Awesome Oscillator (5, 34)",true, TaTypes.categories.DEFAULT);

        // CCIIndicator
        addChartIndicator(new CCIIndicator(series, 20),
                Color.ORANGE,"CCIIndicator (20)",true, TaTypes.categories.DEFAULT);

        // ChandelierExitLongIndicator
        addChartIndicator(new ChandelierExitLongIndicator(series), false);

        // ChandelierExitShortIndicator
        addChartIndicator(new ChandelierExitShortIndicator(series), false);

        // CMO Indicator
        addChartIndicator(new CMOIndicator(closePriceIndicator,20),Color.GREEN,"CMO (cp, 20)",
                true, TaTypes.categories.DEFAULT);

        // Coppock Curve Indicator
        addChartIndicator(new CoppockCurveIndicator(closePriceIndicator,14,11,10),
                Color.GREEN,"Coppock Curve (14, 11, 10)", true, TaTypes.categories.DEFAULT);

        // Directional Movement Indicator
        addChartIndicator(new DirectionalMovementIndicator(series, 20),
                Color.GREEN,"Directional Movement (20)", true, TaTypes.categories.DEFAULT);

        // Double EMA Indicator
        addChartIndicator(new DoubleEMAIndicator(closePriceIndicator, 20),
                Color.YELLOW,"DoubleEMA (20)", false, TaTypes.categories.DEFAULT);

        // DPO Indicator TODO: error because index can get to high
        addChartIndicator(new indicators.DPOIndicator(closePriceIndicator,10),
                Color.YELLOW,"DPO (20)", true, TaTypes.categories.DEFAULT);

        // Fisher Indicator
        addChartIndicator(new FisherIndicator(closePriceIndicator, 20),
                Color.YELLOW,"Fisher (20, 0.33, 0.67)", true, TaTypes.categories.DEFAULT);

        // HMA Indicator
        addChartIndicator(new HMAIndicator(closePriceIndicator, 20),
                Color.YELLOW,"HMA (cp, 20)", false, TaTypes.categories.DEFAULT);

        // KAMA Indicator
        addChartIndicator(new KAMAIndicator(closePriceIndicator,10,2,50),
                Color.YELLOW,"KAMA (cp, 10, 2, 50)", false, TaTypes.categories.DEFAULT);

        // MACD Indicator
        addChartIndicator(new MACDIndicator(closePriceIndicator, 10,50 ),
                Color.YELLOW,"MAC (cp, 10, 50)", true, TaTypes.categories.DEFAULT);

        // Mass Index Indicator
        addChartIndicator(new MassIndexIndicator(series,20,10),
                Color.YELLOW,"Mass Index Indicator (20, 10)", true, TaTypes.categories.DEFAULT);

        // Parabolic Sar Indicator
        addChartIndicator(new ParabolicSarIndicator(series, 20),
                Color.YELLOW,"Parabolic Sar (20)", false, TaTypes.categories.DEFAULT);

        // PPO Indicator
        addChartIndicator(new PPOIndicator(closePriceIndicator, 10, 50),
                Color.YELLOW,"PPO (10,50)", false, TaTypes.categories.DEFAULT);


        // Random Walk High and Low
        List<Indicator> ilRw = new ArrayList<>();
        List<String> nlRw = new ArrayList<>();
        List<Paint> clRw = new ArrayList<>();

        ilRw.add(new RandomWalkIndexHighIndicator(series, 20));
        ilRw.add(new RandomWalkIndexLowIndicator(series, 20));
        nlRw.add("Random Walk High");
        nlRw.add("Random Walk Low");
        clRw.add(Color.GREEN);
        clRw.add(Color.RED);
        addChartIndicator(ilRw,clRw,nlRw,"Random Walk (20)", true, TaTypes.categories.DEFAULT);

        // RAVI Indicator
        addChartIndicator(new RAVIIndicator(closePriceIndicator, 20, 50),
                Color.YELLOW,"RAVI (20,50)", true, TaTypes.categories.DEFAULT);

        // ROC Indicator
        addChartIndicator(new ROCIndicator(closePriceIndicator, 20),
                Color.YELLOW,"ROC (20)", true, TaTypes.categories.DEFAULT);

        // RSI Indicator
        addChartIndicator(new RSIIndicator(closePriceIndicator, 20),
                Color.YELLOW,"RSI (20)", true, TaTypes.categories.DEFAULT);

        // SmoothedRSIIndicator
        addChartIndicator(new SmoothedRSIIndicator(closePriceIndicator, 20),
                Color.YELLOW,"SmoothedRSI (20)", true, TaTypes.categories.DEFAULT);

        // Stochastic RSI Indicator
        addChartIndicator(new StochasticRSIIndicator(closePriceIndicator, 20),
                Color.YELLOW,"Stochastic RSIIndicator (20)", true, TaTypes.categories.DEFAULT);

        // StochasticOscillatorKIndicator StochasticOscillatorDIndicator
        List<Indicator> ilStKd = new ArrayList<>();
        List<String> nlStKd = new ArrayList<>();
        List<Paint> clStKd = new ArrayList<>();
        StochasticOscillatorKIndicator stk = new StochasticOscillatorKIndicator(series, 20);
        StochasticOscillatorDIndicator std = new StochasticOscillatorDIndicator(stk);

        ilStKd.add(stk);
        ilStKd.add(std);
        nlStKd.add("Stoch. O. K");
        nlStKd.add("Stoch. O. D (K)");
        clStKd.add(Color.YELLOW);
        clStKd.add(Color.MAGENTA);
        addChartIndicator(ilStKd, clStKd,nlStKd,"Stochastic Oscilator D K", true, TaTypes.categories.DEFAULT);

        addChartIndicator(new StochasticOscillatorDIndicator(closePriceIndicator),
                Color.YELLOW,"Stochastic Oscillator D (cp)", true, TaTypes.categories.DEFAULT);
        addChartIndicator(new StochasticOscillatorKIndicator(series, 20),
                Color.YELLOW,"Stochastic Oscillator K (20)", true, TaTypes.categories.DEFAULT);

        // TrailingStopLossIndicator
        addChartIndicator(new TrailingStopLossIndicator(closePriceIndicator,Decimal.valueOf(0.1)),
                Color.YELLOW,"Trailing Stop Loss (0.1)", false, TaTypes.categories.DEFAULT);

        // Triple EMAIndicator
        addChartIndicator(new TripleEMAIndicator(closePriceIndicator, 20),
                Color.YELLOW,"Triple EMA (20)", false, TaTypes.categories.DEFAULT);

        // UlcerIndexIndicator
        addChartIndicator(new UlcerIndexIndicator(closePriceIndicator, 20),
                Color.YELLOW,"Ulcer Index (20)", true, TaTypes.categories.DEFAULT);

        // WMAIndicator
        addChartIndicator(new WMAIndicator(closePriceIndicator, 20),
                Color.YELLOW,"WMA (20)", false, TaTypes.categories.DEFAULT);

        // ZLEMAIndicator
        addChartIndicator(new ZLEMAIndicator(closePriceIndicator, 20),
                Color.YELLOW,"ZLEMA (20)", false, TaTypes.categories.DEFAULT);

        // AccumulationDistributionIndicator
        addChartIndicator(new AccumulationDistributionIndicator(series),
                Color.YELLOW,"Accumulation Distribution", true, TaTypes.categories.VOLUME);

        // AccumulationDistributionIndicator
        addChartIndicator(new ChaikinMoneyFlowIndicator(series,20),
                Color.YELLOW,"Chaikin Money (20)", true, TaTypes.categories.VOLUME);


        // AccumulationDistributionIndicator
        VWAPIndicator vwap = new VWAPIndicator(series, 20);
        addChartIndicator(vwap, Color.YELLOW,"VWAP (20)", true, TaTypes.categories.VOLUME);

        // MVWAP + VWAP
        List<Indicator> ilVwap = new ArrayList<>();
        List<String> nlVwap = new ArrayList<>();
        List<Paint> clVwap = new ArrayList<>();

        ilVwap.add(vwap);
        ilVwap.add(new MVWAPIndicator(vwap,100));
        nlVwap.add("VWAP (20)");
        nlVwap.add("MVWAP (VMAP, 20)");
        clVwap.add(Color.MAGENTA);
        clVwap.add(Color.GREEN);
        addChartIndicator(ilVwap,clVwap,nlVwap,"MVWAP+VWAP (20, 100)",true, TaTypes.categories.VOLUME);

        // AccumulationDistributionIndicator
        addChartIndicator(new NVIIndicator(series), Color.BLUE,"NVI", true, TaTypes.categories.VOLUME);

        // OnBalanceVolumeIndicator
        addChartIndicator(new OnBalanceVolumeIndicator(series),Color.BLUE,"On Balance Volume", true, TaTypes.categories.VOLUME);

        // PVIIndicator
        addChartIndicator(new PVIIndicator(series),Color.BLUE,"PVI", true, TaTypes.categories.VOLUME);



    }

    /**
     * Build and add an chart indicator to charts indicator list (random color, default name)
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on sub chart
     */
    public void addChartIndicator(Indicator indicator, boolean isSubchart){
         taChartIndicatorList.add(new TaChartIndicator( indicator, isSubchart));
    }

    /**
     * Build and add an chart indicator to charts indicator list (random color, default name)
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on su bchart
     * @param c the category of the chart
     */
    public void addChartIndicator(Indicator indicator, boolean isSubchart, TaTypes.categories c){
        taChartIndicatorList.add(new TaChartIndicator( indicator, isSubchart, c));
    }

    /**
     * Build and add an chart indicator to charts indicator list
     * @param indicator the ta4j indicator
     * @param isSubchart flag if indicator should be plotted on sub chart
     * @param name the name of the indicator that should be displayed
     * @param paint the paint of the indicator on the chart
     * @param c the category of the chart
     */
    public void addChartIndicator(Indicator indicator, Paint paint, String name, boolean isSubchart, TaTypes.categories c){
        taChartIndicatorList.add(new TaChartIndicator(indicator, paint, name, isSubchart, c));
    }

    /**
     * Adds an chart indicator to the charts indicator list that consists of several ta4j indicators
     * or a strategy
     * @param indicator indicators for chart indicator or strategy
     * @param isSubchart flag if indicator should be plotted on sub chart
     */
    public void addChartIndicator(List<Indicator>indicator, boolean isSubchart){
        taChartIndicatorList.add(new TaChartIndicator(indicator, isSubchart));
    }

    /**
     * Adds an chart indicator to the charts indicator list that consists of several ta4j indicators
     * or a strategy
     * @param indicator indicators for chart indicator or strategy
     * @param isSubchart flag if indicator should be plotted on sub chart
     * @param c the category of the chart indicator
     */
    public void addChartIndicator(List<Indicator>indicator, boolean isSubchart, TaTypes.categories c){
        taChartIndicatorList.add(new TaChartIndicator(indicator, isSubchart, c));
    }

    /**
     * Adds an chart indicator to the charts indicator list that consists of several ta4j indicators
     * or a strategy
     * @param indicator a list of indicators for a chart indicator or a strategy
     * @param paint a list of paint objects the describe the chart indicators
     * @param name a list of names that describe the indicators of the chart indicator/strategy
     * @param generalName the name of the indicator that should be displayed
     * @param isSubchart flag if indicator should be plotted on sub chart
     * @param c fthe category of the chart indicator
     */
    public void addChartIndicator(List<Indicator> indicator, List<Paint> paint, List<String> name, String generalName, boolean isSubchart, TaTypes.categories c){
        taChartIndicatorList.add(new TaChartIndicator(indicator, paint, name, generalName, isSubchart, c));
    }

    /**
     * Get all indicators that are stored in this box
     * @return all ChartIndicators that are stored in this box
     */
    public List<TaChartIndicator> getTaChartIndicatorList() {
        return taChartIndicatorList;
    }
}
