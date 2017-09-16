package indicators;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.SMAIndicator;
import eu.verdelhan.ta4j.indicators.helpers.ClosePriceIndicator;

public class DPOIndicator extends CachedIndicator<Decimal> {
    private final int timeFrame;
    private final int timeShift;
    private final Indicator<Decimal> price;
    private final SMAIndicator sma;

    public DPOIndicator(TimeSeries series, int timeFrame) {
        this(new ClosePriceIndicator(series), timeFrame);
    }

    public DPOIndicator(Indicator<Decimal> price, int timeFrame) {
        super(price);
        this.timeFrame = timeFrame;
        this.timeShift = timeFrame / 2 + 1;
        this.price = price;
        this.sma = new SMAIndicator(price, this.timeFrame);
    }

    protected Decimal calculate(int index) {
        return price.getValue(index).minus(sma.getValue(index-timeShift));
    }
}
