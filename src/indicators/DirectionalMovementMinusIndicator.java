package indicators;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.CachedIndicator;
import eu.verdelhan.ta4j.indicators.helpers.AverageDirectionalMovementDownIndicator;
import eu.verdelhan.ta4j.indicators.helpers.AverageTrueRangeIndicator;

public class DirectionalMovementMinusIndicator extends CachedIndicator<Decimal> {

    private final AverageDirectionalMovementDownIndicator averageDirectionalMovementDownIndicator;
    private final AverageTrueRangeIndicator trueRangeIndicator;
    private final int timeFrame;

    public DirectionalMovementMinusIndicator(TimeSeries series, int timeFrame) {
        super(series);
        this.timeFrame = timeFrame;
        this.averageDirectionalMovementDownIndicator = new AverageDirectionalMovementDownIndicator(series, timeFrame);
        this.trueRangeIndicator = new AverageTrueRangeIndicator(series, timeFrame);
    }

    @Override
    protected Decimal calculate(int index) {
        return (averageDirectionalMovementDownIndicator.getValue(index).dividedBy(trueRangeIndicator.getValue(index))).multipliedBy(Decimal.HUNDRED);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+" time frame: "+timeFrame;
    }
}
