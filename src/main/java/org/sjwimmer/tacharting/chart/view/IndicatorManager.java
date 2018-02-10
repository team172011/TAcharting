package org.sjwimmer.tacharting.chart.view;

import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.sjwimmer.tacharting.chart.model.ChartIndicator;
import org.sjwimmer.tacharting.chart.model.IndicatorKey;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;

public class IndicatorManager extends Dialog<Pair<IndicatorKey,ChartIndicator>> {

    public IndicatorManager(IndicatorKey keyToPlot, ObservableList<IndicatorKey> keysOnPlot){

        keysOnPlot.addAll(new IndicatorKey(IndicatorType.CLOSE,1),
                new IndicatorKey(IndicatorType.OPEN,1),
                new IndicatorKey(IndicatorType.MAX,1),
                new IndicatorKey(IndicatorType.MIN,1));
        Dialog<IndicatorKey> dialogBasedOn = new Dialog<>();
        dialogBasedOn.setTitle(String.format("Adding %s",keyToPlot.getType().getDisplayName()));
        dialogBasedOn.setHeaderText(String.format("Choose the base for %s",keyToPlot.getType().getDisplayName()));

        ComboBox<IndicatorKey> spinner = new ComboBox<>(keysOnPlot);


        dialogBasedOn.getDialogPane().setContent(new VBox(spinner));
        dialogBasedOn.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY,ButtonType.CLOSE);

        dialogBasedOn.showAndWait();
    }
}
