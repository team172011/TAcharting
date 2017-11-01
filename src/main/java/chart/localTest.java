package chart;

import example.Loader;
import org.ta4j.core.Decimal;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.adx.AverageDirectionalMovementIndicator;
import org.ta4j.core.indicators.helpers.AverageDirectionalMovementDownIndicator;
import org.ta4j.core.indicators.helpers.AverageDirectionalMovementUpIndicator;
import org.ta4j.core.indicators.helpers.AverageTrueRangeIndicator;

import java.net.URL;

public class localTest {

    public static void main(String[] args){
        localTest instance = new localTest();
    }


    public localTest(){
        URL res = getClass().getClassLoader().getResource("coke_daily.csv");
        TimeSeries series = Loader.getDailyTimeSeries(res, "coke");

        //series = coke, daily, 2017-07-18 - 2017-09-21
        // ADX
        AverageDirectionalMovementIndicator adx = new AverageDirectionalMovementIndicator(series, 14);

        // Indicators for PDI and MDI calculation:
        AverageTrueRangeIndicator averageTrueRange = new AverageTrueRangeIndicator(series, 14);
        AverageDirectionalMovementDownIndicator admd = new AverageDirectionalMovementDownIndicator(series, 14);
        AverageDirectionalMovementUpIndicator admu = new AverageDirectionalMovementUpIndicator(series, 14);

        for (int i = series.getBeginIndex(); i<series.getEndIndex(); i++){
            Decimal pdi = admu.getValue(i).dividedBy(averageTrueRange.getValue(i)).multipliedBy(Decimal.HUNDRED);
            Decimal mdi = admd.getValue(i).dividedBy(averageTrueRange.getValue(i)).multipliedBy(Decimal.HUNDRED);
            System.out.println(series.getTick(i).getEndTime()+" PDI: "+pdi+" MDI: "+mdi+" ADX: "+adx.getValue(i));
        }
    }
}
