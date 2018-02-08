package org.sjwimmer.tacharting.chart.parameters;

import org.sjwimmer.tacharting.chart.model.ChartIndicator;
import org.sjwimmer.tacharting.chart.model.IndicatorBase;
import org.sjwimmer.tacharting.chart.model.IndicatorKey;
import org.sjwimmer.tacharting.chart.model.IndicatorParameter;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorParameterType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.sjwimmer.tacharting.chart.model.types.IndicatorType.EMA;

public class CreateFunctions {

    public final static Map<IndicatorType, BiFunction<IndicatorBase,Map<String, IndicatorParameter>, ChartIndicator>> functions = new HashMap<>();

    static{

       functions.put(EMA, (source, params) -> {
            if(source instanceof Indicator) {

                Indicator<Decimal> indicator = source.getIndicator();
                IndicatorType basedOn = IndicatorType.getTypeOf(indicator);
                EMAIndicator ema = new EMAIndicator(indicator, (int) params.get("Time Frame").getValue());
                boolean subchart = (boolean) params.get(IndicatorParameterType.CHARTTYPE.name).getValue();

                IndicatorKey key = new IndicatorKey(EMA,basedOn.getDisplayName(),params.get("id").getInteger());
                ChartIndicator chartIndicator = new ChartIndicator(key);
                chartIndicator.addIndicator(ema,"ema",params.get("Color").getColor(),params.get("Shape").getShape(),
                        params.get("Stroke").getStroke(), ChartType.OVERLAY);

                return chartIndicator;
            }

            throw new IllegalArgumentException("Wrong types "+source+ " "+params+ EMA);

        });
    }
}
