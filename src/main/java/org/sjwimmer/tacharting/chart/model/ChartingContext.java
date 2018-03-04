package org.sjwimmer.tacharting.chart.model;

import org.sjwimmer.tacharting.chart.controller.manager.IndicatorParameterManager;
import org.sjwimmer.tacharting.chart.view.TaChart2;

public class ChartingContext {

    private final TaChart2 chart;
    private final IndicatorBox box;
    private final IndicatorParameterManager manager;

    public ChartingContext(IndicatorBox box, TaChart2 chart){
        this.chart = chart;
        this.box = box;
        this.manager = box.getPropertiesManager();
    }

    public IndicatorBox getBox() {
        return box;
    }

    public IndicatorParameterManager getManager() {
        return manager;
    }

    public TaChart2 getChart() {
        return chart;
    }
}
