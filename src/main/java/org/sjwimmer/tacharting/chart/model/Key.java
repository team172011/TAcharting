package org.sjwimmer.tacharting.chart.model;

public class Key {

    private String description;

    public Key(String description){
        this.description = description;
    }

    @Override
    public String toString(){
        return description;
    }

    protected void setDescription(String string){
        this.description = string;
    }
}
