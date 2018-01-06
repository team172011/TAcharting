package chart;

import javafx.beans.property.SimpleObjectProperty;
import org.ta4j.core.TimeSeries;

public class TimeSeriesTableCell {

    final SimpleObjectProperty<TimeSeries> timeSeries;

    public TimeSeriesTableCell(TimeSeries timeSeries){
        this.timeSeries = new SimpleObjectProperty<>(timeSeries);
    }


    public String getName(){
        return timeSeries.get().getName();
    }

    public TimeSeries getTimeSeries(){
        return timeSeries.get();
    }
}
