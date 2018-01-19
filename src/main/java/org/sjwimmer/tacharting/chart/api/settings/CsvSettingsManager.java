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

import com.opencsv.CSVParser;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.parameters.TimeFormatType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CsvSettingsManager {

    private final Logger logger = LoggerFactory.getLogger(CsvSettingsManager.class);
    private VBox rootPane =  new VBox(5);
    private static final Properties properties = new Properties();

    @FXML private ComboBox<String> seperatorBox;
    @FXML private ComboBox<String> lineEndBox;
    @FXML private Button btnShowTimeIds;
    @FXML private Label lblLineEnd;

    public CsvSettingsManager(){
        Dialog<Boolean> settingsDialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(("fxml/charting-CsvSettings.fxml")));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(rootPane);
        rootPane.getStylesheets().add("fxml/charting-Settings.css");
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

            btnShowTimeIds.setOnAction((ActionEvent event) -> {

                Dialog dialog = new Dialog();

                TableView<TimeFormatColumn> list = new TableView<>();
                TableColumn<TimeFormatColumn, Integer> colId = new TableColumn<>("Id");
                TableColumn<TimeFormatColumn, String> colFormat= new TableColumn<>("Time Format");
                TableColumn<TimeFormatColumn, String> colComment= new TableColumn<>("Comment");
                colId.setCellValueFactory(new PropertyValueFactory<>("id"));
                colFormat.setCellValueFactory(new PropertyValueFactory<>("TimeFormat"));
                colComment.setCellValueFactory(new PropertyValueFactory<>("Comment"));
                list.getColumns().add(colId);
                list.getColumns().add(colFormat);
                list.getColumns().add(colComment);
                for(TimeFormatType tt: TimeFormatType.values()){
                    list.getItems().add(new TimeFormatColumn(tt));
                }
                StackPane pane = new StackPane(list);
                dialog.getDialogPane().setMaxHeight(200);
                dialog.getDialogPane().setContent(pane);
                Window window = dialog.getDialogPane().getScene().getWindow();
                window.setOnCloseRequest(event2 -> window.hide());
                dialog.showAndWait();
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

    private static void loadProperties() {
        try(InputStream is = new FileInputStream(Parameter.API_PROPERTIES_FILE)){
            properties.load(is);
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
            try(FileOutputStream outputStream = new FileOutputStream((Parameter.API_PROPERTIES_FILE))){
                getProperties().store(outputStream, null);
            } catch (IOException ioe){
                ioe.printStackTrace();
            }

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

    public class TimeFormatColumn {
        private final StringProperty timeFormat, comment;
        private final IntegerProperty id;

        public TimeFormatColumn(TimeFormatType tt){
            timeFormat = new SimpleStringProperty(tt.pattern);
            comment = new SimpleStringProperty(tt.comment);
            id = new SimpleIntegerProperty(tt.id);
        }

        public String getTimeFormat() {
            return timeFormat.get();
        }

        public StringProperty timeFormatProperty() {
            return timeFormat;
        }

        public int getId() {
            return id.get();
        }

        public IntegerProperty idProperty() {
            return id;
        }

        public String getComment() {
            return comment.get();
        }

        public StringProperty commentProperty() {
            return comment;
        }
    }
}
