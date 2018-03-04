package org.sjwimmer.tacharting.chart.controller.manager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.sjwimmer.tacharting.chart.model.ChartIndicator;
import org.sjwimmer.tacharting.chart.model.IndicatorKey;
import org.sjwimmer.tacharting.chart.model.types.BaseType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;

public class IndicatorManager extends Dialog<Pair<IndicatorKey,ChartIndicator>> {

    public IndicatorManager(IndicatorKey keyToPlot, ObservableList<IndicatorKey> subplots, ObservableList<IndicatorKey> overlays){
        ObservableList<IndicatorKey> spinnerList = FXCollections.observableArrayList();
        Dialog<IndicatorKey> dialogBasedOn = new Dialog<>();
        dialogBasedOn.setTitle(String.format("Adding %s",keyToPlot.getType().getDisplayName()));

        if(keyToPlot.getType().getBaseType() == BaseType.INDICATOR || keyToPlot.getType().getBaseType() == BaseType.BOOTH){
            dialogBasedOn.setHeaderText(String.format("Choose the base for %s",keyToPlot.getType().getDisplayName()));
            spinnerList.addAll(new IndicatorKey(IndicatorType.CLOSE,1),
                    new IndicatorKey(IndicatorType.OPEN,1),
                    new IndicatorKey(IndicatorType.MAX,1),
                    new IndicatorKey(IndicatorType.MIN,1));
            spinnerList.addAll(subplots);
            spinnerList.addAll(overlays);
            ComboBox<IndicatorKey> spinner = new ComboBox<>(spinnerList);
            dialogBasedOn.getDialogPane().setContent(new VBox(spinner));
            dialogBasedOn.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY,ButtonType.CLOSE);
            dialogBasedOn.showAndWait();
        }







    }
}
