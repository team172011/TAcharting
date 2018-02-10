package org.sjwimmer.tacharting.chart.model;


import org.sjwimmer.tacharting.chart.model.types.BaseType;

/**
 * Abstract class for storing information about the wanted base of a
 * ChartIndicator that should be added to the Chart
 */
public abstract class AbstractBase {

    public final BaseType type;

    public AbstractBase(BaseType type){
        this.type = type;
    }
}
