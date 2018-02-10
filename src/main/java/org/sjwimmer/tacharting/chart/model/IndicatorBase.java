package org.sjwimmer.tacharting.chart.model;

public class IndicatorBase extends AbstractBase{

    private final ChartIndicator indicator;
    private final String name;


    public IndicatorBase(ChartIndicator indicator){
        super(BaseType.INDICATOR);
        this.indicator = indicator;
        this.name = "";
    }

    /**
     *
     * @param indicator the {@link ChartIndicator} that is the base added indicator
     * @param name the name of the ta4j indiator in indicator (default is the first that was added)
     */
    public IndicatorBase(ChartIndicator indicator, String name){
        super(BaseType.INDICATOR);
        this.indicator = indicator;
        this.name = name;
    }

    public ChartIndicator getIndicator(){
        return indicator;
    }

    public String getName() {
        return name;
    }
}
