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
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.sjwimmer.tacharting.chart.api.settings;


import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.types.YahooTimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class YahooSettingsManager{

    private final Logger logger = LoggerFactory.getLogger(YahooSettingsManager.class);
    private VBox rootPane = new VBox(5);
    private static final Properties properties = new Properties();

    @FXML private ComboBox<YahooTimePeriod> intervalBox;
    @FXML private DatePicker pickerFrom;
    @FXML private DatePicker pickerTo;

    public YahooSettingsManager() {
        Dialog<Boolean> settingDialog = new Dialog();
        settingDialog.setTitle("Yahoo Api Settings");
        settingDialog.setHeaderText("Yahoo connections and org.sjwimmer.tacharting.data settings");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(("fxml/charting-YahooSettings.fxml")));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(rootPane);
        settingDialog.getDialogPane().setContent(rootPane);
        rootPane.getStylesheets().add("fxml/charting-Settings.css");
        try{
            fxmlLoader.load();
            YahooProperties yahooProperties = new YahooProperties();
            intervalBox.setItems(FXCollections.observableArrayList(YahooTimePeriod.values()));
            intervalBox.valueProperty().bindBidirectional(yahooProperties.intervalProperty());

            pickerFrom.valueProperty().bindBidirectional(yahooProperties.fromProperty());
            pickerFrom.setShowWeekNumbers(true);
            pickerTo.valueProperty().bindBidirectional(yahooProperties.toProperty());
            pickerTo.setShowWeekNumbers(true);

            ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType btnSave = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

            settingDialog.getDialogPane().getButtonTypes().addAll(btnSave,btnCancel);
            settingDialog.setResultConverter( btnType -> {
                if (btnType == btnSave){
                    yahooProperties.save();
                    return true;
                } else {
                    return false;
                }
            } );
            settingDialog.showAndWait();
        } catch (IOException io){
            io.printStackTrace();
        }
    }

    private static void loadProperties() {
        try(InputStream is = new FileInputStream(Parameter.API_PROPERTIES_FILE)){
            properties.load(is);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Properties getProperties(){
        if(properties.isEmpty()){
            loadProperties();
        }
        return properties;
    }

    class YahooProperties{
        private final SimpleObjectProperty<LocalDate> from;
        private final SimpleObjectProperty<LocalDate> to;
        private final SimpleObjectProperty<YahooTimePeriod> interval;

        /**
         * Constructor.
         */
        YahooProperties(){
            interval = new SimpleObjectProperty<>(YahooTimePeriod.of(getProperties().getProperty(Parameter.PROPERTY_YAHOO_INTERVAL, "1d")));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern);
            LocalDate now = LocalDate.now();
            String fromS = (getProperties().getProperty(Parameter.PROPERTY_YAHOO_FROM,
                    LocalDate.now().minusYears(Parameter.DEFAULT_LOOK_BACK).format(formatter)));
            String toS = (getProperties().getProperty(Parameter.PROPERTY_YAHOO_TO,
                    ZonedDateTime.now().format(formatter)));

            from = new SimpleObjectProperty<>(LocalDate.parse(fromS, formatter));
            to = new SimpleObjectProperty<>(LocalDate.parse(toS, formatter));
        }

        public LocalDate getFrom() {
            return from.get();
        }

        public SimpleObjectProperty<LocalDate> fromProperty() {
            return from;
        }

        public LocalDate getTo() {
            return to.get();
        }

        public SimpleObjectProperty<LocalDate> toProperty() {
            return to;
        }

        public YahooTimePeriod getInterval() {
            return interval.get();
        }

        public SimpleObjectProperty<YahooTimePeriod> intervalProperty() {
            return interval;
        }

        public void save() {

                getProperties().setProperty(Parameter.PROPERTY_YAHOO_INTERVAL, (interval.get().toYahooString()));
                if(from.get().isBefore(to.get())) {
                    getProperties().setProperty(Parameter.PROPERTY_YAHOO_TO, to.get().format(DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern)));
                    getProperties().setProperty(Parameter.PROPERTY_YAHOO_FROM, from.get().format(DateTimeFormatter.ofPattern(TimeFormatType.YAHOO.pattern)));
                } else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,String.format("Properties not saved: from (%s) is after to (%s)",from,to));
                    alert.setTitle("Could not save dates");
                    alert.show();
                }
            try (FileOutputStream outputStream = new FileOutputStream(Parameter.API_PROPERTIES_FILE)){
                getProperties().store(outputStream, null);
                logger.debug("Properties saved: {}, {}, {}", from.get(), to.get(), interval.get().toString());
            } catch (IOException io){
                Alert alert = new Alert(Alert.AlertType.ERROR, io.getMessage());
                alert.setTitle("Could not save YAHOO_PROPS");
                alert.show();
                logger.warn("Could not load properties {}",io.getMessage());
            }
        }

    }
}
