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
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.sjwimmer.tacharting.chart.model.types;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.bollinger.*;
import org.ta4j.core.indicators.candles.LowerShadowIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelLowerIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelMiddleIndicator;
import org.ta4j.core.indicators.keltner.KeltnerChannelUpperIndicator;
import org.ta4j.core.indicators.volume.MVWAPIndicator;
import org.ta4j.core.indicators.volume.NVIIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.indicators.volume.PVIIndicator;

/**
 * (In progress..)
 * Enum of all available indicators with names for gui, names for storing and class.
 *
 */
public enum IndicatorType {

    EMA("Exponential Moving Average", EMAIndicator.class, BaseType.INDICATOR),
    SMA("Simple Moving Average", SMAIndicator.class, BaseType.INDICATOR),
    BOLLINGER_BANDS_MIDDLE("Bollinger Bands Middle", BollingerBandsMiddleIndicator.class, BaseType.INDICATOR),
    BOLLINGER_BANDS_UPPER("Bollinger Bands Upper", BollingerBandsUpperIndicator.class, BaseType.INDICATOR),
    BOLLINGER_BANDS_LOWER("Bollinger Bands Lower", BollingerBandsLowerIndicator.class, BaseType.INDICATOR),
    BOLLINGER_BANDS_WIDTH( "Bollinger Bands Width", BollingerBandWidthIndicator.class, BaseType.INDICATOR),
    BOLLINGER_BANDS("Bollinger Bands",null, BaseType.INDICATOR),

    AMOUNT("Amount",AmountIndicator.class, BaseType.SERIES),
    AROON_UP("Aroon Up", AroonUpIndicator.class, BaseType.BOOTH),
    AROON_DOWN("Aroon Down", AroonDownIndicator.class, BaseType.BOOTH),
    AROONS("Aroon Up/Down",null, BaseType.BOOTH),
    AVERAGE_DIRECTIONAL_MOVEMENT("Average Directional Movement/ADX", ADXIndicator.class, BaseType.SERIES),

    CMO("CMO", CMOIndicator.class, BaseType.INDICATOR),
    CLOSE("Close Price", ClosePriceIndicator.class, BaseType.SERIES),

    KAMA("KAMA", KAMAIndicator.class, BaseType.INDICATOR),
    KELTNER_MIDDLE("Keltener Middle Chanel", KeltnerChannelMiddleIndicator.class, BaseType.BOOTH),
    KELTNER_Upper("Keltener Uper Chanel", KeltnerChannelUpperIndicator.class, BaseType.INDICATOR),
    KELTNER_LOWER("Keltener Lower Chanel", KeltnerChannelLowerIndicator.class, BaseType.INDICATOR),
    LOWER_SHADOW( "Lower Shadow", LowerShadowIndicator.class, BaseType.BOOTH),
    MACD("MACD", MACDIndicator.class, BaseType.INDICATOR),
    MAX("High Price", MaxPriceIndicator.class, BaseType.SERIES),
    MIN("Low Price", MinPriceIndicator.class, BaseType.SERIES),
    MVWAP("MVWAP", MVWAPIndicator.class, BaseType.INDICATOR),
    NVI("NVI", NVIIndicator.class,BaseType.SERIES),
    ON_BALANCE_VOLUMEN("On Balance Volumen", OnBalanceVolumeIndicator.class, BaseType.SERIES),
    OPEN("Open Price", OpenPriceIndicator.class, BaseType.SERIES),
    PERCENT_BI("Percent BI", PercentBIndicator.class, BaseType.INDICATOR),
    PREVIOUS_VALUE("Previous Value", PreviousValueIndicator.class, BaseType.INDICATOR),
    PVI("PVI", PVIIndicator.class, BaseType.SERIES),;
/*
    REAL_BODY("Real Body", RealBodyIndicator.class),
    SMOOTHED_RSI("Smoothed Relaive Strength", RSIIndicator.class),
    STOCHASTIC_RSI("Stochastic RSI", StochasticRSIIndicator.class),
    STOCHASTIC_OscillatorK("Stochastic Oscillator K", StochasticRSIIndicator.class), //TODO...
    TRIPLE_EMA("Triple EMA", TripleEMAIndicator.class),
    TRUE_RANGE("True Range", TRIndicator.class),
    ULCER_INDEX("Ulcer Index", UlcerIndexIndicator.class),
    UPPER_SHADOW("Upper Shadow", UpperShadowIndicator.class),
    VWAP("VWAP", VWAPIndicator.class),
    WMA("WMA", WMAIndicator.class),
    ZLEMA("ZLEMA",ZLEMAIndicator.class);
 */


    private final String displayName;
    private final Class<? extends Indicator> clazz;
    private final BaseType baseType;

    IndicatorType(String name, Class<? extends Indicator> clazz, BaseType baseType) {
        this.displayName = name;
        this.clazz = clazz;
        this.baseType = baseType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BaseType getBaseType() {
        return baseType;
    }
}
