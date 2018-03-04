package org.sjwimmer.tacharting.chart.model;


import javafx.scene.paint.Color;

public class IndicatorStyleParameter {


    private Color color;
    private boolean visible;

    public IndicatorStyleParameter(Color color, boolean visible){
        this.color = color;
        this.visible = visible;
    }

    public IndicatorStyleParameter(){
        this(Color.BLACK, true);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible){
        this.visible = visible;
    }
}
