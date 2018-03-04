package org.sjwimmer.tacharting.chart.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorParameterType;
import org.sjwimmer.tacharting.chart.model.types.ShapeType;
import org.sjwimmer.tacharting.chart.model.types.StrokeType;
import org.sjwimmer.tacharting.chart.utils.ConverterUtils;

import java.awt.*;
import java.util.Objects;

/**
 * Class represents an indicator parameter. A pair of {@link IndicatorParameterType} and a {@code objectProperty} or {@code stringProperty}.
 * The property can be transformed to the type described by the field {@link #type}
 *
 * This class is used to transport and transform input from gui to properties files or inversely and to transport
 * parameters that are only needed via runtime (for instance a ChartIndicator or a TimeSeries).
 */
public class IndicatorParameter{

    private final StringProperty description;
    private final ObjectProperty<IndicatorParameterType> type;
    private final ObjectProperty<Object> objectProperty;
    private final StringProperty xmlProperty;


    public IndicatorParameter(String description, IndicatorParameterType type, Object objectProperty){
        Objects.requireNonNull(description,"Must be not null");
        Objects.requireNonNull(type,"Must be not null");
        Objects.requireNonNull(objectProperty,"Must be not null");

        if (objectProperty instanceof String){
            this.description = new SimpleStringProperty(description);
            this.type = new SimpleObjectProperty<>(type);
            this.objectProperty = new SimpleObjectProperty<>(null);
            this.xmlProperty = new SimpleStringProperty((String)objectProperty);
            return;
        }
        this.description = new SimpleStringProperty(description);
        this.type = new SimpleObjectProperty<>(type);
        this.objectProperty = new SimpleObjectProperty<>(objectProperty);
        this.xmlProperty = new SimpleStringProperty(null); //TODO: Objects are only stored and loaded at runtime can not be stored to xml
    }

    public String getDescription() {
        return description.get();
    }

    public IndicatorParameterType getType() {
        return type.get();
    }

    /**Getter for Parameters that have a string representation and can be stored in xml ****/

    public Color getColor(){
       return ConverterUtils.ColorFxConverter.fromString(getString());
    }

    public java.awt.Shape getShape(){
        return ShapeType.valueOf(getString()).shape;
    }

    public Stroke getStroke(){
        return StrokeType.valueOf(getString()).stroke;
    }

    public ShapeType getShapeType(){
        return ShapeType.valueOf(getString());
    }

    public StrokeType getStrokeType(){
        return StrokeType.valueOf(getString());
    }

    public ChartType getChartType(){
        return ChartType.valueOf(getString());
    }

    public int getInteger(){
        return Integer.parseInt(getString());
    }

    public Double getDouble(){
        return Double.parseDouble(getString());
    }

    public String getString(){
        return xmlProperty.get();
    }

    public boolean getBoolean(){
        return Boolean.valueOf(getString());
    }


    public IndicatorKey getIndicatorKey(){
        return ConverterUtils.IndicatorKeyConverter.fromString(getString());
    }

    /** Getter for Params that are only needed at runtime and can not be stored to xml *******/

    public ChartIndicator getChartIndicator(){
        return (ChartIndicator) objectProperty.get();
    }

    public TaTimeSeries getSeries() {
        return (TaTimeSeries) objectProperty.get();
    }

    public String getXmlProperty() {
        return xmlProperty.get();
    }

    public StringProperty xmlProperty() {
        return xmlProperty;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<IndicatorParameterType> typeProperty() {
        return type;
    }

    public ObjectProperty<Object> objectProperty() {
        return objectProperty;
    }
}

