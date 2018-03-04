/*
 GNU Lesser General Public License

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.sjwimmer.tacharting.chart.controller.manager;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.sjwimmer.tacharting.chart.model.BaseIndicatorBox;
import org.sjwimmer.tacharting.chart.model.ChartingContext;
import org.sjwimmer.tacharting.chart.model.IndicatorKey;
import org.sjwimmer.tacharting.chart.model.IndicatorParameter;
import org.sjwimmer.tacharting.chart.model.types.ChartType;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;
import org.sjwimmer.tacharting.chart.model.types.ShapeType;
import org.sjwimmer.tacharting.chart.model.types.StrokeType;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.utils.ConverterUtils;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IndicatorSettingsManager {

    private static Popup open;

    private final IndicatorKey key;
    private final ChartingContext context;


    @FXML private HBox hBox;
    @FXML private Label title;

    private final Map<String, IndicatorParameter> parameters = new HashMap<>();

    public static BorderPane getAsNode(IndicatorKey key, ChartingContext context){
        IndicatorSettingsManager window = new IndicatorSettingsManager(key,context);
        return window.buildPane(true, false, false);
    }


    public static Popup getPopUpWindow(IndicatorKey key, ChartingContext context){
        return new IndicatorSettingsManager(key, context).buildPopUpWindow();
    }

    private IndicatorSettingsManager(IndicatorKey key, ChartingContext context){
        this.context = context;
        this.key = key;
    }

    private Popup buildPopUpWindow() {
        if(open != null){
            open.hide();
        }
        Popup popup = new Popup();
        popup.getContent().add(buildPane(true,true,true));
        popup.setHideOnEscape(true);
        open = popup;
        /*PopupControl popupWindow = new PopupControl();
        popupWindow.getScene().setRoot(buildPane(true, true, true));
        popupWindow.setOnAutoHide(event -> open = null);
        popupWindow.setAutoHide(true);*/
        return popup;
    }

    private BorderPane buildPane(boolean saveButton, boolean dublicateButton, boolean cancelButton) {
        BorderPane pane = new BorderPane();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(("fxml/charting-IndicatorPopUpWindow.fxml")));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(pane);
        pane.getStylesheets().add("fxml/charting-IndicatorPopUpWindow.css");
        try {
            fxmlLoader.load();
            title.setText(key.getType().getDisplayName());
            title.getStyleClass().add("title");
            if(true){ // FIXME: only start building gui if the indicator is from xml, e.g if it has an valid key, id
                parameters.putAll(context.getManager().getParametersFor(key));
                VBox first = new VBox(3);
                first.setId("vbox");
                // iterate over all parameters and add label and a value setter
                for(Map.Entry<String,IndicatorParameter> i:parameters.entrySet()){
                    if(i.getKey().equals(Parameter.id)){
                        continue;
                    }
                    IndicatorParameter parameter = i.getValue();

                    Control valueSetter = getAndBind(parameter);
                    valueSetter.setId(parameter.getDescription());
                    Label name = new Label(parameter.getDescription());
                    name.getStyleClass().add("plable");
                    first.getChildren().add(name);
                    first.getChildren().add(valueSetter);
                    first.getChildren().add(new Separator(Orientation.HORIZONTAL));
                    if(first.getChildren().size() > 13){
                        hBox.getChildren().add(first);
                        first = new VBox(3);
                        first.setId("vbox");
                    }
                }
                if(first.getChildren().size()>0){
                    hBox.getChildren().add(first);
                }

                pane.setBottom(addButtonAction(saveButton, dublicateButton, cancelButton));
            } else {
                pane.setCenter(new Label("No settings available"));
                pane.setBottom(addButtonAction(false,false,true));
            }

        } catch (XPathExpressionException | IOException xpe){
            xpe.printStackTrace();
            pane.setCenter(new Label("No settings available"));
            pane.setBottom(addButtonAction(false,false,true));
        }
        return pane;
    }

    private Pane addButtonAction (boolean save, boolean duplicate, boolean remove){
        HBox buttonBox = new HBox(4);
        if(save){
            Button btnSave = new Button("Save");
            btnSave.setOnAction(event -> {
                try {
                    context.getChart().addIndicator(((BaseIndicatorBox)(context.getBox())).indicatorFunctions.get(key.getType()).apply(parameters));
                    parameters.remove(Parameter.id); // do not store id..
                    parameters.remove(Parameter.baseImpl);
                    context.getManager().setParameters(key, this.parameters);
                    if(open!=null){
                        open.hide();
                    }
                } catch (Exception xpe) {
                    //TODO handle..
                    xpe.printStackTrace();
                }
            });
            buttonBox.getChildren().add(btnSave);
        }

        if(duplicate){
            Button btnDuplicate = new Button("Duplicate");
            btnDuplicate.setOnAction(event->{
                try {
                    String newKey = context.getManager().duplicate(key);
                    Alert info = new Alert(Alert.AlertType.INFORMATION,newKey+" Added to the indicators menu", ButtonType.APPLY);
                    info.showAndWait();
                    open.hide();
                } catch (Exception e){
                    //TODO handle..
                    e.printStackTrace();
                }
            });
            buttonBox.getChildren().add(btnDuplicate);
        }

        if(remove){
            Button btnRemove = new Button("Remove");
            btnRemove.setOnAction(event -> {
                context.getChart().removeIndicator(key);
                if (open != null){
                    open.hide();}
            });
            buttonBox.getChildren().add(btnRemove);
        }

        return buttonBox;
    }

    private Control getAndBind(IndicatorParameter parameter) {
        switch (parameter.getType()){
            case COLOR:{
                ColorPicker colorPicker = new ColorPicker(parameter.getColor());
                Bindings.bindBidirectional(parameter.xmlProperty(),colorPicker.valueProperty(), ConverterUtils.ColorFxConverter);
                return colorPicker;
            }
            case SHAPE:{
                ComboBox<ShapeType> cbox = new ComboBox<ShapeType>(FXCollections.observableArrayList(ShapeType.values()));
                cbox.setValue(parameter.getShapeType());
                Bindings.bindBidirectional(parameter.xmlProperty(),cbox.valueProperty(),ConverterUtils.ShapeTypeConverter);
                return cbox;
            }
            case STROKE:{
                ComboBox<StrokeType> cbox = new ComboBox<StrokeType>(FXCollections.observableArrayList(StrokeType.values()));
                cbox.setValue(parameter.getStrokeType());
                Bindings.bindBidirectional(parameter.xmlProperty(),cbox.valueProperty(),ConverterUtils.StrokeTypeConverter);
                return cbox;
            }
            case BOOLEAN:{
                CheckBox checkBox = new CheckBox("yes/no:");
                checkBox.setSelected(parameter.getBoolean());
                Bindings.bindBidirectional(parameter.xmlProperty(),checkBox.selectedProperty(),ConverterUtils.BooleanypeConverter);
                return checkBox;
            }
            case CHARTTYPE:{
                ComboBox<ChartType> cbox = new ComboBox<ChartType>(FXCollections.observableArrayList(ChartType.values()));
                cbox.setValue(parameter.getChartType());
                Bindings.bindBidirectional(parameter.xmlProperty(),cbox.valueProperty(),ConverterUtils.ChartTypeConverter);
                return cbox;
            }
            case INDICATOR:{
                ObservableList<IndicatorKey> list = FXCollections.observableArrayList(context.getManager().getAllKeys());
                list.addAll(new IndicatorKey(IndicatorType.CLOSE,0), new IndicatorKey(IndicatorType.OPEN,0),
                        new IndicatorKey(IndicatorType.MAX,0), new IndicatorKey(IndicatorType.MIN,0));

                ComboBox<IndicatorKey> comboBox = new ComboBox<>(list);
                comboBox.setValue(parameter.getIndicatorKey());
                comboBox.setCellFactory(param -> new ListCell<IndicatorKey>(){
                    @Override
                    protected void updateItem(IndicatorKey key, boolean empty){
                        super.updateItem(key, empty);
                        if(key != null && !empty){
                            setText(key.toString());
                        }
                        setGraphic(null);
                    }
                });
                Bindings.bindBidirectional(parameter.xmlProperty(),comboBox.valueProperty(), ConverterUtils.IndicatorKeyConverter);
                return comboBox;
            }
            case SERIES:{
                return new Label("Based on Candlesticks");
            }
        }
        TextField textField = new TextField(parameter.getString());
        textField.textProperty().bindBidirectional(parameter.xmlProperty());
        return textField;
    }
}
