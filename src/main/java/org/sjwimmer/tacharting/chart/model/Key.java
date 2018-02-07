package org.sjwimmer.tacharting.chart.model;

public class Key {

    private final String description;

    public Key(String description){
        this.description = description;
    }

    @Override
    public String toString(){
        return description;
    }
}
