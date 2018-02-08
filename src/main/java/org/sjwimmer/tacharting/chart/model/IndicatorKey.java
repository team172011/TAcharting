package org.sjwimmer.tacharting.chart.model;

import org.sjwimmer.tacharting.chart.model.types.IndicatorType;

import java.util.Objects;

/**
 * Key to identify an indicator instance on the chart/menu/indicatorBox
 * Such as EMAIndicator_1, EMAIndicator2
 */
public class IndicatorKey extends Key {

    private final IndicatorType type;
    private final int id;

    public IndicatorKey(IndicatorType name,String adding, int id) {
        super(String.format("%s %s [%s]",name.getDisplayName(),adding.equals("")?"":"("+adding+")", id));
        this.type = name;
        this.id = id;
    }

    public IndicatorKey(IndicatorType name, int id) {
        this(name, "", id);
    }

    public IndicatorType getType() {
        return type;
    }

    public int getId() {
        return id;
    }


    @Override
    public boolean equals(Object other){
        if(!(other instanceof IndicatorKey)){
            return false;
        }
        IndicatorKey otherKey = (IndicatorKey)other;
        return otherKey.getId() == id && otherKey.getType()==type;
    }

    @Override
    public int hashCode(){
        return Objects.hash(type,id);
    }
}
