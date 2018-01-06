package chart.settings;

import chart.parameters.Parameter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Properties;

public class YahooSettingsManager{

    private final Logger logger = LoggerFactory.getLogger(YahooSettingsManager.class);
    private VBox rootPane = new VBox(5);
    private static final Properties properties = new Properties();

    @FXML private ComboBox<Parameter.YahooInterval> intervalBox;
    @FXML private DatePicker pickerFrom;
    @FXML private DatePicker pickerTo;

    public YahooSettingsManager() {
        Dialog<Boolean> settingDialog = new Dialog();
        settingDialog.setTitle("Yahoo Api Settings");
        settingDialog.setHeaderText("Yahoo connections and data settings");

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(("fxml/YahooSettings.fxml")));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(rootPane);
        settingDialog.getDialogPane().setContent(rootPane);
        rootPane.getStylesheets().add("fxml/Settings.css");
        try{
            fxmlLoader.load();
            YahooProperties yahooProperties = new YahooProperties();
            intervalBox.setItems(FXCollections.observableArrayList(Parameter.YahooInterval.values()));
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
        try{
            InputStream is = new FileInputStream(YahooSettingsManager.class.getClassLoader().getResource(Parameter.API_PROPERTIES_FILE).getFile());
            properties.load(is);
            is.close();
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
        private final SimpleObjectProperty<Parameter.YahooInterval> interval;

        /**
         * Constructor.
         */
        YahooProperties(){
            interval = new SimpleObjectProperty<>(Parameter.YahooInterval.valueOf(getProperties().getProperty(Parameter.PROPERTY_YAHOO_INTERVAL, "daily")));
            LocalDate now = LocalDate.now();
            to = new SimpleObjectProperty<>(Parameter.TimeFormat.yahoo.format(getProperties().getProperty(Parameter.PROPERTY_YAHOO_TO, now.format(Parameter.FORMATTER_yyy_MM_dd))).toLocalDate());
            from = new SimpleObjectProperty<>(Parameter.TimeFormat.yahoo.format(getProperties().getProperty(Parameter.PROPERTY_YAHOO_FROM, now.minusYears(1).format(Parameter.FORMATTER_yyy_MM_dd))).toLocalDate());
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

        public Parameter.YahooInterval getInterval() {
            return interval.get();
        }

        public SimpleObjectProperty<Parameter.YahooInterval> intervalProperty() {
            return interval;
        }

        public void save() {
            try {
                getProperties().setProperty(Parameter.PROPERTY_YAHOO_INTERVAL, interval.get().toString());
                if(from.get().isBefore(to.get())) {
                    getProperties().setProperty(Parameter.PROPERTY_YAHOO_TO, to.get().format(Parameter.FORMATTER_yyy_MM_dd));
                    getProperties().setProperty(Parameter.PROPERTY_YAHOO_FROM, from.get().format(Parameter.FORMATTER_yyy_MM_dd));
                } else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Cannot save dates");
                    alert.setContentText(String.format("Properties not saved: from (%s) is after to (%s)",from,to));
                    alert.show();
                }
                getProperties().store(new FileOutputStream(getClass().getClassLoader().getResource(Parameter.API_PROPERTIES_FILE).getFile()), null);
                logger.debug("Properties saved: {}, {}, {}", from.get(), to.get(), interval.get().toString());
            } catch (IOException io){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Could not save YAHOO_PROPS");
                alert.setHeaderText("Could not save YAHOO_PROPS");
                alert.setContentText(io.getMessage());
                alert.show();
                logger.warn("Could not load properties");
            }
        }

    }
}
