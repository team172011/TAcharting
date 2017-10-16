package indicators;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.helpers.CumulatedGainsIndicator;
import eu.verdelhan.ta4j.indicators.helpers.CumulatedLossesIndicator;

public class CMOIndicator extends CachedIndicator<Decimal> {

    private final CumulatedGainsIndicator cumulatedGains;

    private final CumulatedLossesIndicator cumulatedLosses;

    /**
     * Constructor.
     * @param price a price indicator
     * @param timeFrame the time frame
     */
    public CMOIndicator(Indicator<Decimal> price, int timeFrame) {
        super(price);
        cumulatedGains = new CumulatedGainsIndicator(price, timeFrame);
        cumulatedLosses = new CumulatedLossesIndicator(price, timeFrame);
    }

    @Override
    protected Decimal calculate(int index) {
        Decimal sumOfGains = cumulatedGains.getValue(index);
        Decimal sumOfLosses = cumulatedLosses.getValue(index);
        return (sumOfGains.minus(sumOfLosses))
                .dividedBy((sumOfGains.plus(sumOfLosses))).multipliedBy(Decimal.HUNDRED);
    }
}
