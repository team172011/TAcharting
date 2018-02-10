package org.sjwimmer.tacharting.chart.parameters;

import org.sjwimmer.tacharting.chart.model.*;
import org.sjwimmer.tacharting.chart.model.types.BaseType;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.sjwimmer.tacharting.chart.model.types.IndicatorType.*;

/**
 * Class stores a map of functions to create an {@link ChartIndicator} for each {@link IndicatorType}.
 * Indicators that should be created with help of a function from this map need a {@link AbstractBase}
 * that describes the underlying source for the indicator. For instance the {@link EMAIndicator} needs
 * another {@link Indicator} for that the exponential moving average should be calculated.
 * see also {@link IndicatorBase}, {@link SeriesBase}
 * Furthermore the function needs a Map of (String, {@link IndicatorParameter}) pairs that holds the
 * parameters for the corresponding indicator. It should also store the id of the indicator (if exists)
 * named as {@link Parameter#id}. Other common keys for this map should also be taken from the Parameters
 * class. For instance the EMAIndicator needs the <code>Time Frame</code> parameter from the xml file
 * that is stored in the map with the key named as {@link Parameter#tf
 * see also {@link IndicatorParameter}, {@link org.sjwimmer.tacharting.chart.api.IndicatorParameterManager#getParametersFor}
 *
 * */
public class CreateFunctions {

    public final static
    Map<IndicatorType, BiFunction<AbstractBase,Map<String, IndicatorParameter>, ChartIndicator>> functions
            = new HashMap<>();

    static {

        functions.put(EMA, (source, params) -> {
            if (source.type == BaseType.INDICATOR) {
                IndicatorBase base = (IndicatorBase) source;
                ChartIndicator basedOn = base.getIndicator();

                Indicator<Decimal> baseIndicator;
                if (((IndicatorBase) source).getName().equals("")) {
                    baseIndicator = basedOn.getIndicator(0);
                } else {
                    baseIndicator = basedOn.getIndicator(base.getName());
                }
                EMAIndicator ema = new EMAIndicator(baseIndicator, params.get(Parameter.tf).getInteger());
                basedOn.addIndicator(ema,
                        String.format("EMA (%s, %s)",
                                basedOn.getGeneralName(),
                                params.get("Time Frame")),
                        params.get("Color").getColor(),
                        params.get("Shape").getShape(),
                        params.get("Stroke").getStroke());

                return basedOn;
            }

            throw new IllegalArgumentException("Source not supported" + source.getClass() + " " + params + EMA);

        });

        functions.put(OPEN, (source, params) -> {
            if (source.type == BaseType.SERIES) {
                SeriesBase base = (SeriesBase) source;
                ChartIndicator indicator = new ChartIndicator(new IndicatorKey(OPEN, params.get(Parameter.id).getInteger()), ChartType.OVERLAY);
                indicator.addIndicator(new OpenPriceIndicator(base.getSeries()));
            }
            throw new IllegalArgumentException("Source not supported" + source.getClass() + " " + params + EMA);
        });

        functions.put(MAX, (source, params) -> {
            if (source.type == BaseType.SERIES) {
                SeriesBase base = (SeriesBase) source;
                ChartIndicator indicator = new ChartIndicator(new IndicatorKey(OPEN, params.get(Parameter.id).getInteger()), ChartType.OVERLAY);
                indicator.addIndicator(new MaxPriceIndicator(base.getSeries()));
            }
            throw new IllegalArgumentException("Source not supported" + source.getClass() + " " + params + EMA);
        });

        functions.put(MIN, (source, params) -> {
            if (source.type == BaseType.SERIES) {
                SeriesBase base = (SeriesBase) source;
                ChartIndicator indicator = new ChartIndicator(new IndicatorKey(OPEN, params.get(Parameter.id).getInteger()), ChartType.OVERLAY);
                indicator.addIndicator(new MinPriceIndicator(base.getSeries()));
            }
            throw new IllegalArgumentException("Source not supported" + source.getClass() + " " + params + EMA);
        });

        functions.put(CLOSE, (source, params) -> {
            if (source.type == BaseType.SERIES) {
                SeriesBase base = (SeriesBase) source;
                ChartIndicator indicator = new ChartIndicator(new IndicatorKey(OPEN, params.get(Parameter.id).getInteger()), ChartType.OVERLAY);
                indicator.addIndicator(new ClosePriceIndicator(base.getSeries()));
            }
            throw new IllegalArgumentException("Source not supported" + source.getClass() + " " + params + EMA);
        });

    }
}
