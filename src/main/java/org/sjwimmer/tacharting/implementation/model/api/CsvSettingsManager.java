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
package org.sjwimmer.tacharting.implementation.model.api;

import com.opencsv.CSVParser;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.sjwimmer.tacharting.chart.model.types.TimeFormatType;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
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
    @FXML private ComboBox<String> stringQuoteBox;
    @FXML private Label lblLineEnd;
    @FXML private TableView<ExampleRow> tblExample;
    @FXML private TableColumn<ExampleRow, String> colTimeFormat;
    @FXML private TableColumn<ExampleRow, String> colTimeFormatID;
    @FXML private TableColumn<ExampleRow, String> colComment;
    @FXML private TableColumn<ExampleRow, String> colExample;

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
            stringQuoteBox.setItems(FXCollections.observableArrayList("\"", "\'"));
            stringQuoteBox.valueProperty().bindBidirectional(csvProperties.quoteProperty());
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

            colTimeFormatID.setCellValueFactory(new PropertyValueFactory<>("id"));
            colTimeFormat.setCellValueFactory(new PropertyValueFactory<>("TimeFormat"));
            colComment.setCellValueFactory(new PropertyValueFactory<>("Comment"));
            colExample.setCellValueFactory(new PropertyValueFactory<>("Example"));

            Callback<TableColumn<ExampleRow, String>, TableCell<ExampleRow,String>> cellFactory =
                    new Callback<TableColumn<ExampleRow, String>, TableCell<ExampleRow, String>>() {
                @Override
                public TableCell<ExampleRow, String> call(TableColumn<ExampleRow, String> param) {
                    final TableCell<ExampleRow, String> cell = new TableCell<ExampleRow, String>(){
                        final Button btn = new Button("Show Example");
                        @Override
                        public void updateItem(String string, boolean empty){
                            super.updateItem(string,empty);
                            if(empty){
                                setGraphic(null);
                                setText(null);
                            } else {
                                btn.setOnAction(event -> {
                                    ExampleRow row = tblExample.getItems().get(getIndex());
                                    int id = row.getId();
                                    String path = null;
                                    if(id == TimeFormatType.yyyy_MM_ddHmsz.id){
                                        path = getClass().getClassLoader().getResource("aapl_hourly.csv").getPath();
                                    } else if(id == TimeFormatType.yyyyMMdd.id){
                                        path = getClass().getClassLoader().getResource("aapl_daily.csv").getPath();
                                    } else if(id == TimeFormatType.YAHOO.id){
                                        path = getClass().getClassLoader().getResource("example3.csv").getPath();
                                    } else if (id == TimeFormatType.EODATA.id){
                                        path = getClass().getClassLoader().getResource("AAAP_daily_eodata.csv").getPath();
                                    }
                                    try {
                                        if(Parameter.OS.contains("win")){
                                            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler"  + path);
                                        } else if(Parameter.OS.contains("nix")||Parameter.OS.contains("nux")){
                                            Runtime.getRuntime().exec("xdg-open " + path);
                                        }
                                        else{
                                            final String path2 = path;
                                            Platform.runLater(() ->
                                                    new Alert(Alert.AlertType.INFORMATION,
                                                    "No editor found for your System. Inspect file: "+path2).show());
                                        }

                                    } catch (IOException | NullPointerException e){
                                        e.printStackTrace();
                                        new Alert(Alert.AlertType.INFORMATION,
                                                "No editor found. Inspect file: " + path).show();
                                    }
                                });
                                setGraphic(btn);
                                setText(null);
                            }
                        }
                    };
                    return cell;
                }
            };
            colExample.setCellFactory(cellFactory);


            for(TimeFormatType tt: TimeFormatType.values()){
                tblExample.getItems().add(new ExampleRow(tt));
            }

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

    class CsvProperties{
        private final StringProperty separator;
        private final StringProperty quote;

        public CsvProperties(){
            separator = new SimpleStringProperty(getProperties()
                    .getProperty(Parameter.PROPERTY_CSV_SEPARATOR, String.valueOf(CSVParser.DEFAULT_SEPARATOR)));
            quote = new SimpleStringProperty(getProperties()
                    .getProperty(Parameter.PROPERTY_CSV_QUOTE, String.valueOf(CSVParser.DEFAULT_ESCAPE_CHARACTER)));
        }

        public void save(){
            getProperties().setProperty(Parameter.PROPERTY_CSV_QUOTE, quote.get());
            getProperties().setProperty(Parameter.PROPERTY_CSV_SEPARATOR, separator.get());
            try(FileOutputStream outputStream = new FileOutputStream((Parameter.API_PROPERTIES_FILE))){
                getProperties().store(outputStream, null);
            } catch (IOException ioe){
                ioe.printStackTrace();
            }

            logger.debug("Properties saved: {}, {}", quote.get(),separator.get());
        }

        public String getSeparator() {
            return separator.get();
        }

        public StringProperty separatorProperty() {
            return separator;
        }

        public String getQuote() {
            return quote.get();
        }

        public StringProperty quoteProperty() {
            return quote;
        }
    }

    public class ExampleRow {
        private final StringProperty timeFormat, comment;
        private final IntegerProperty id;

        public ExampleRow(TimeFormatType tt){
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
