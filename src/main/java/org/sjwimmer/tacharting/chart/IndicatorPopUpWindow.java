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
package org.sjwimmer.tacharting.chart;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.sjwimmer.tacharting.chart.parameters.ChartType;
import org.sjwimmer.tacharting.chart.parameters.IndicatorParameter;
import org.sjwimmer.tacharting.chart.parameters.ShapeType;
import org.sjwimmer.tacharting.chart.parameters.StrokeType;
import org.sjwimmer.tacharting.chart.utils.ConverterUtils;
import org.sjwimmer.tacharting.chart.utils.FormatUtils;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IndicatorPopUpWindow extends PopupControl {

    private static IndicatorPopUpWindow open;

    private final IndicatorsPropertiesManager propertiesManager;
    private final String key;
    private final ChartIndicatorBox indicatorBox;


    private  BorderPane borderPane;
    @FXML private HBox hBox;
    @FXML private Label title;
    @FXML private Button btnSave;
    @FXML private Button btnDuplicate;
    @FXML private Button btnRemove;

    private final List<IndicatorParameter> parameters = new ArrayList<>(); //TODO: simplify with observable list

    private IndicatorPopUpWindow(String key, ChartIndicatorBox indicatorBox){
        this.propertiesManager = indicatorBox.getPropertiesManager();
        this.key = key;
        this.indicatorBox = indicatorBox;
        borderPane = new BorderPane();
        setAutoHide(true);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(("fxml/charting-IndicatorPopUpWindow.fxml")));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(borderPane);
        borderPane.getStylesheets().add("fxml/charting-IndicatorPopUpWindow.css");

        try {
            fxmlLoader.load();
            String[] el = key.split("_");
            title.setText(el[0]);
            title.getStyleClass().add("title");
            if(el.length > 1){ // only start building gui if the indicator is from xml, e.g if it has an id
                parameters.addAll(propertiesManager.getParametersFor(key));
                VBox first = new VBox(3);
                // iterate over all parameters and add label and a value setter
                for(int i = 0; i<parameters.size(); i++){
                    first.setId("vbox");
                    IndicatorParameter parameter = parameters.get(i);
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
                    }
                }
                if(first.getChildren().size()>0){
                    hBox.getChildren().add(first);
                }

                addButtonAction(true, true);
                setOnAutoHide(event -> hide());
                getScene().setRoot(borderPane);
            } else {
                borderPane.setCenter(new Label("No settings available"));
                addButtonAction(false, false);
            }

        } catch (XPathExpressionException | IOException xpe){
            xpe.printStackTrace();
            borderPane.setCenter(new Label("No settings available"));
            addButtonAction(false, false);
        }
    }

    private void addButtonAction (boolean save, boolean duplicate){
        if(save){
            btnSave.setOnAction(event -> {

                try {
                    for (IndicatorParameter parameter : parameters) {
                        final String valueToStore ;
                        switch (parameter.getType()){
                            case COLOR:{
                                valueToStore = ConverterUtils.ColorAWTConverter.toString((java.awt.Color)parameter.getValue());
                                break;
                            } default:{
                                valueToStore = parameter.getValue().toString();
                                break;
                            }
                        }
                        propertiesManager.setParameter(key, parameter.getDescription(), valueToStore);
                        indicatorBox.reloadIndicator(key);
                        hide();
                    }
                } catch (XPathExpressionException | IOException | TransformerException xpe) {
                    //TODO handle..
                    xpe.printStackTrace();
                } catch (XPathException e) {
                    e.printStackTrace();
                }
            });

            } else {
            btnSave.setDisable(true);
        }

        if(duplicate){
            btnDuplicate.setOnAction(event->{
                try {
                    propertiesManager.duplicate(key);
                    hide();
                    //TODO inform new entry in menu
                } catch (Exception e){
                    //TODO handle..
                    e.printStackTrace();
                }
            });
        } else {
            btnDuplicate.setDisable(true);
        }

        btnRemove.setOnAction(event -> { indicatorBox.removeIndicator(key);hide(); });

        }

    public static IndicatorPopUpWindow getPopUpWindow(String key, ChartIndicatorBox chartIndicatorBox){
        if (open != null) {
            open.hide();
        }
        open = new IndicatorPopUpWindow(key, chartIndicatorBox);
        return open;
    }

    @Override
    public void hide(){
        super.hide();

    }


    @Override
    protected Skin<?> createDefaultSkin() {
        return new IndicatorPupUpWindwoSkin(this, borderPane);
    }

    class IndicatorPupUpWindwoSkin implements Skin<PopupControl>{

        private PopupControl popup;
        private Pane content;

        public IndicatorPupUpWindwoSkin(final PopupControl control, final Pane content){
            this.popup = control;
            this.content = content;
            content.idProperty().bind(popup.idProperty());
            content.styleProperty().bind(popup.styleProperty());
            content.getStyleClass().addAll(popup.getStyleClass());
        }

        @Override
        public PopupControl getSkinnable() {
            return popup;
        }

        @Override
        public Node getNode() {
            return content;
        }

        @Override
        public void dispose() {

        }
    }


    private Control getAndBind(IndicatorParameter parameter) {
        Object value = parameter.getValue();
        switch (parameter.getType()){
            case COLOR:{
                ColorPicker colorPicker = new ColorPicker(FormatUtils.awtColorToJavaFX((java.awt.Color)value));
                Bindings.bindBidirectional(parameter.valueProperty(),colorPicker.valueProperty(), ConverterUtils.ColorFxConverter);
                return colorPicker;
            }
            case SHAPE:{
                ComboBox<ShapeType> cbox = new ComboBox<ShapeType>(FXCollections.observableArrayList(ShapeType.values()));
                cbox.setValue((ShapeType)value);
                Bindings.bindBidirectional(parameter.valueProperty(),cbox.valueProperty(),ConverterUtils.ShapeTypeConverter);
                return cbox;
            }
            case STROKE:{
                ComboBox<StrokeType> cbox = new ComboBox<StrokeType>(FXCollections.observableArrayList(StrokeType.values()));
                Bindings.bindBidirectional(parameter.valueProperty(),cbox.valueProperty(),ConverterUtils.StrokeTypeConverter);
                cbox.setValue((StrokeType)value);
                return cbox;
            }
            case BOOLEAN:{
                CheckBox checkBox = new CheckBox("yes/no:");
                checkBox.setSelected((boolean)value);
                Bindings.bindBidirectional(parameter.valueProperty(),checkBox.selectedProperty(),ConverterUtils.BooleanypeConverter);
                return checkBox;
            }
            case CHARTTYPE:{
                ComboBox<ChartType> cbox = new ComboBox<ChartType>(FXCollections.observableArrayList(ChartType.values()));
                cbox.setValue((ChartType)value);
                Bindings.bindBidirectional(parameter.valueProperty(),cbox.valueProperty(),ConverterUtils.ChartTypeConverter);
                return cbox;
            }
        }
        TextField textField = new TextField(value.toString());
        textField.textProperty().bindBidirectional(parameter.valueProperty());
        return textField;
    }
}
