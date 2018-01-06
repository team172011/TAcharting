package chart.settings;

import chart.parameters.Parameter;
import com.opencsv.CSVParser;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CsvSettingsManager {

    private final Logger logger = LoggerFactory.getLogger(CsvSettingsManager.class);
    private VBox rootPane =  new VBox(5);
    private static final Properties properties = new Properties();

    @FXML private ComboBox<String> seperatorBox;
    @FXML private ComboBox<String> lineEndBox;

    public CsvSettingsManager(){
        Dialog<Boolean> settingsDialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(("fxml/CsvSettings.fxml")));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(rootPane);
        rootPane.getStylesheets().add("fxml/Settings.css");
        settingsDialog.getDialogPane().setContent(rootPane);
        try{
            fxmlLoader.load();
            CsvProperties csvProperties = new CsvProperties();
            lineEndBox.setItems(FXCollections.observableArrayList("\\", "\\n"));
            lineEndBox.valueProperty().bindBidirectional(csvProperties.lineBreakProperty());
            seperatorBox.valueProperty().bindBidirectional(csvProperties.separatorProperty());
            seperatorBox.setItems(FXCollections.observableArrayList(",", " ", ";"));

            ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            settingsDialog.getDialogPane().getButtonTypes().addAll(save,cancel);
            settingsDialog.setResultConverter(button ->{
                if(button == save){
                    csvProperties.save();
                    return true;
                }
                return false;
            });

            settingsDialog.showAndWait();

        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    public static Properties getProperties(){
        if(properties.isEmpty()){
            loadProperties();
        }
        return properties;
    }

    //TODO: it is the same file for all...
    private static void loadProperties() {
        try{
            InputStream is = new FileInputStream(CsvSettingsManager.class.getClassLoader().getResource(Parameter.API_PROPERTIES_FILE).getFile());
            properties.load(is);
            is.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // TODO add position of info line and maybe possibility to set info line in properties
    class CsvProperties{
        private final StringProperty separator;
        private final StringProperty lineBreak;

        public CsvProperties(){
            separator = new SimpleStringProperty(getProperties().getProperty(Parameter.PROPERTY_CSV_SEPERATOR, String.valueOf(CSVParser.DEFAULT_SEPARATOR)));
            lineBreak = new SimpleStringProperty(getProperties().getProperty(Parameter.PROPERTY_CSV_ENDLINE, String.valueOf(CSVParser.DEFAULT_ESCAPE_CHARACTER)));
        }

        public void save(){
            getProperties().setProperty(Parameter.PROPERTY_CSV_ENDLINE, lineBreak.get());
            getProperties().setProperty(Parameter.PROPERTY_CSV_SEPERATOR, separator.get());
            logger.debug("Properties saved: {}, {}",lineBreak.get(),separator.get());
        }

        public String getSeparator() {
            return separator.get();
        }

        public StringProperty separatorProperty() {
            return separator;
        }

        public String getLineBreak() {
            return lineBreak.get();
        }

        public StringProperty lineBreakProperty() {
            return lineBreak;
        }
    }
}
