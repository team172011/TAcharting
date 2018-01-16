package org.sjwimmer.tacharting.chart.parameters;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.sjwimmer.tacharting.chart.utils.FormatUtils;

/**
 * Class represents an indicator parameter. A pair of {@link IndicatorParameterType} and a {@code value}. The value (
 * saved as String) can be transformed to the data type described by the IndicatorParameterType through the
 * {@link #getValue()} function
 */
public class IndicatorParameter{

    private final StringProperty description;
    private final ObjectProperty<IndicatorParameterType> type;
    private final StringProperty value;

    public IndicatorParameter(String description, IndicatorParameterType type, String value){
        if(value==null || type==null || description==null){
            throw new IllegalArgumentException(String.format(
                    "Parameters of IndicatorParameter must not be null {} {} {}",
                    description.toString(),type.toString(),value.toString()));
        }
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
                return FormatUtils.ColorAWTConverter.fromString(value.get());
            }
            case SHAPE:{
                return ShapeType.valueOf(value.get());
            }
            case DOUBLE:{
               return Double.parseDouble(value.get());
            }
            case STRING:{
                return value;
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

