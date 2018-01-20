package org.sjwimmer.tacharting.chart.types;

public enum ChartType {
    OVERLAY,
    SUBCHART;

    public boolean toBoolean(){
        if(this==OVERLAY){
            return false;
        }
        return true;
    }

}
