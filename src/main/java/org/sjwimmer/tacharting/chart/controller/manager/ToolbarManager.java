package org.sjwimmer.tacharting.chart.controller.manager;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ToolBar;
import javafx.stage.PopupWindow;
import org.sjwimmer.tacharting.chart.model.ChartIndicator;
import org.sjwimmer.tacharting.chart.model.ChartingContext;
import org.sjwimmer.tacharting.chart.model.IndicatorKey;

import java.util.Map;


public class ToolbarManager {

    public static void bindToolbarAndMenu(ToolBar bar, Map<IndicatorKey, CheckMenuItem> menuItemMap, ChartingContext context){
        ObservableMap<IndicatorKey, Button> mapProperty = FXCollections.observableHashMap();

        MapChangeListener<IndicatorKey, ChartIndicator> listener = change ->{
            if(change.wasRemoved()){
                bar.getItems().remove(mapProperty.get(change.getKey()));
                mapProperty.remove(change.getKey());
                menuItemMap.get(change.getKey()).setSelected(false);
            }
            if(change.wasAdded()){
                Button btn = new Button(change.getKey().toString());
                bar.getItems().add(btn);
                mapProperty.put(change.getKey(),btn);
                btn.setOnAction(event -> {
                    PopupWindow in = IndicatorSettingsManager.getPopUpWindow(change.getKey(),context);
                    in.show(btn.getScene().getWindow());
                });
            }
        };

        context.getChart().getCurrentSubplots().addListener(listener);
        context.getChart().getCurrentOverlays().addListener(listener);
    }

}
