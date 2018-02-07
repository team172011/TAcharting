package org.sjwimmer.tacharting.chart.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorParameterType;
import org.sjwimmer.tacharting.chart.model.types.ShapeType;
import org.sjwimmer.tacharting.chart.model.types.StrokeType;
import org.sjwimmer.tacharting.chart.utils.ConverterUtils;

import java.util.Objects;

/**
 * Class represents an indicator parameter. A pair of {@link IndicatorParameterType} and a {@code value}. The value (
 * saved as String) can be transformed to the type described by the IndicatorParameterType through the
 * {@link #getValue()} function.
 * This class is used to transport and transform input from gui to properties files or inversely
 */
public class IndicatorParameter{

    private final StringProperty description;
    private final ObjectProperty<IndicatorParameterType> type;
    private final StringProperty value;

    public IndicatorParameter(String description, IndicatorParameterType type, String value){
        Objects.requireNonNull(description,"Must be not null");
        Objects.requireNonNull(type,"Must be not null");
        Objects.requireNonNull(value,"Must be not null");
        this.description = new SimpleStringProperty(description);
        this.type = new SimpleObjectProperty<IndicatorParameterType>(type);
        this.value = new SimpleStringProperty(value);
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public IndicatorParameterType getType() {
        return type.get();
    }

    public ObjectProperty<IndicatorParameterType> typeProperty() {
        return type;
    }

    public void setType(IndicatorParameterType type) {
        this.type.set(type);
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public Object getValue(){
        switch (type.get()){
            case COLOR:{
                return ConverterUtils.ColorAWTConverter.fromString(value.get());
            }
            case SHAPE:{
                return ShapeType.valueOf(value.get());
            }
            case DOUBLE:{
               return Double.parseDouble(value.get());
            }
            case STRING:{
                return value.get();
            }
            case STROKE:{
                return StrokeType.valueOf(value.get());
            }
            case BOOLEAN:{
                return Boolean.valueOf(value.get());
            }
            case INTEGER:{
                return Integer.parseInt(value.get());
            }
            case CHARTTYPE:{
                return ChartType.valueOf(value.get());
            }
        }
        return value.get();
    }


}

