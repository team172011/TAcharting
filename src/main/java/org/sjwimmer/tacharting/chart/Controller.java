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

package org.sjwimmer.tacharting.chart;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.sjwimmer.tacharting.chart.api.*;
import org.sjwimmer.tacharting.chart.api.settings.CsvSettingsManager;
import org.sjwimmer.tacharting.chart.api.settings.YahooSettingsManager;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.types.GeneralTimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.TradingRecord;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_CSV;
import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_EXCEL;

public class Controller implements MapChangeListener<String, ChartIndicator>{

    private final Logger logger = LoggerFactory.getLogger(Controller.class);
    private TaChart chart;
    private final Map<String, CheckMenuItem> itemMap = new HashMap<>();
    private final ObservableList<TimeSeriesTableCell> tableData = FXCollections.observableArrayList();

    private final SQLConnector sqlConnector;

    @FXML private VBox vbxChart;

    @FXML private Menu indicatorsMenu;
    @FXML private Menu candles;
    @FXML private Menu def;
    @FXML private Menu custom;
    @FXML private Menu bollinger;
    @FXML private Menu statistics;
    @FXML private Menu volume;
    @FXML private Menu ichimoku;
    @FXML private Menu helpers;
    @FXML private Menu keltner;
    @FXML private Menu strategy;
    @FXML private Menu tradingRecords;

    @FXML private ToolBar toolBarIndicators;
    @FXML private ComboBox<Parameter.ApiProvider> choiceBoxAPI;

    @FXML private TableView<TimeSeriesTableCell> tblSymbol;
    @FXML private TableColumn<TimeSeriesTableCell, String> colSymbol;
    @FXML private TextField fieldSearch;
    @FXML private ToggleButton tbnStoreData;

    public Controller(){
        sqlConnector = new SqlLiteConnector();
    }

    @FXML
    public void initialize(){
        fieldSearch.textProperty().addListener((ov, oldValue, newValue) -> fieldSearch.setText(newValue.toUpperCase()));
        fieldSearch.setOnKeyPressed(event->{
            if(event.getCode() == KeyCode.ENTER){
                addYahoo();
            }
        });
        colSymbol.setCellValueFactory(new PropertyValueFactory<>("Name"));
        //colSymbol.setCellFactory(column -> new SymbolTableCell());
        colSymbol.getTableView().setItems(tableData);
        colSymbol.getTableView().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                TaTimeSeries series = colSymbol.getTableView().getSelectionModel().getSelectedItem().getTimeSeries();
                this.chart.getChartIndicatorBox().setTimeSeries(series);
            }
        });

        colSymbol.getTableView().setContextMenu(buildContextMenu());
        choiceBoxAPI.setItems(FXCollections.observableArrayList(Parameter.ApiProvider.values()));
        choiceBoxAPI.setValue(Parameter.ApiProvider.Yahoo);
        RequestData requestData = new RequestData();
        requestData.start();
    }

    private ContextMenu buildContextMenu(){
        MenuItem itemRemove = new MenuItem("remove");
        itemRemove.setOnAction(e -> {
            TimeSeriesTableCell selectedCell = colSymbol.getTableView().getSelectionModel().getSelectedItem();
            colSymbol.getTableView().getItems().remove(selectedCell);
        });
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(itemRemove);
        return menu;
    }


    /**
     * This function has to be called before showing the stage. It allows the user to add a customized <t>ChartIndicatorBox</t>
     * @param box the {@link ChartIndicatorBox ChartIndicatorBox} with ChartIndicators, TimeSeries
     *            and TradingRecords for the org.sjwimmer.tacharting.chart
     */
    public void setIndicatorBox(ChartIndicatorBox box){
        if (box != null) {
            chart = new TaChart(box);
            VBox.setVgrow(chart, Priority.ALWAYS);
            vbxChart.getChildren().add(chart);

            box.getChartIndicatorMap().addListener(this);
            buildMenuEntries(box);
            TaTimeSeries series = box.getTimeSeries();
            addToWatchlist(series);
        }
    }

    private void addToWatchlist(TaTimeSeries series) {
        if(tbnStoreData.isSelected()){
            try {
                sqlConnector.insertData(series, false);
            } catch (SQLException sqle){
                sqle.printStackTrace();
            }
        }
        Platform.runLater(()->tableData.add(new TimeSeriesTableCell(series)));
    }


    /**
     * Build the menu with entries colorOf all indicators from xml AND added indicators from the indicatorBox
     * @param box
     */
    private void buildMenuEntries(ChartIndicatorBox box){

        final IndicatorsPropertiesManager propsManager = box.getPropertiesManager();

        for (Map.Entry<String, ChartIndicator> entry : chart.getChartIndicatorBox().getChartIndicatorMap().entrySet()) {
            addToCategory(entry.getKey(), entry.getValue().getCategory());
        }

        final List<String> keys = propsManager.getAllKeys();
        for(String key: keys){
            try{
                Parameter.IndicatorCategory category = propsManager.getCategory(key);
                addToCategory(key, category);
            } catch (XPathExpressionException xpe){
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,xpe.getMessage()).show());
            }
        }

        for (Map.Entry<String, TradingRecord> entry : chart.getChartIndicatorBox().getAllTradingRecords().entrySet()) {
            addToTradingRecord(entry.getKey(), entry.getValue());
        }
    }

    private void addToTradingRecord(String key, TradingRecord value) {
        CheckMenuItem item = new CheckMenuItem(key);
        item.setOnAction(event -> { chart.plotTradingRecord(value, item.isSelected()); });
        tradingRecords.getItems().add(item);
    }

    private void addToCategory(String key, Parameter.IndicatorCategory category){
        final String[] el = key.split("_");
        String name = el[0];
        String id = "";
        if(el.length > 1){ // custom indicators or indicators that added during runtime may not have an id separator
            id = el[1];
        }
        CheckMenuItem item = new CheckMenuItem(String.format("%s [%s]", name, id));
        item.setId(key);
        itemMap.put(key,item);
        item.setOnAction((a)-> {
            try {
                if(item.isSelected()){
                    chart.getChartIndicatorBox().reloadIndicator(key);
                }
            } catch (XPathException xpe){
                //TODO: handle exception..
                xpe.printStackTrace();
            } });

        switch(category){
            case DEFAULT:{
                def.getItems().add(item);
                break;
            }
            case HELPERS:{
                helpers.getItems().add(item);
                break;
            }
            case VOLUME:{
                volume.getItems().add(item);
                break;
            }
            case CANDLES:{
                candles.getItems().add(item);
                break;
            }
            case ICHIMOKU:{
                candles.getItems().add(item);
                break;
            }
            case STATISTICS:{
                statistics.getItems().add(item);
                break;
            }
            case KELTNER:{
                keltner.getItems().add(item);
                break;
            }
            case BOLLINGER:{
                bollinger.getItems().add(item);
                break;
            }
            case STRATEGY:{
                strategy.getItems().add(item);
                break;
            }
            default:
                custom.getItems().add(item);
                break;
        }

    }

    //TODO write class to store this information
    private Map<String, Button> keyButton = new HashMap<>();
    private Map<String, Separator> keySeperator = new HashMap<>();

    /**
     * Update the ToolBar
     * Called every time an ChartIndicator has been added or removed to the
     * {@link ChartIndicatorBox chartIndicatorBox} colorOf the underlying {@link TaChart org.sjwimmer.tacharting.chart}
     *
     * @param change Change<? extends String, ? extends ChartIndicator>
     */
    @Override
    public void onChanged(Change<? extends String, ? extends ChartIndicator> change) {
        String key = change.getKey();

        if(change.wasRemoved()){
            toolBarIndicators.getItems().remove(keyButton.get(key));
            toolBarIndicators.getItems().remove(keySeperator.get(key));
            if(!change.wasAdded()) {
                CheckMenuItem item = itemMap.get(key);
                if(item!=null){
                    item.setSelected(false);
                }
            }
        }
        // it is possible that wasRemoved = wasAdded = true, e.g ObservableMap.put(existingKey, indicator)
        if(change.wasAdded()) {
            ChartIndicator indicator = change.getValueAdded();
            Button btnSetup = new Button(indicator.getGeneralName());
            btnSetup.setOnAction((event)->{
                IndicatorPopUpWindow in = IndicatorPopUpWindow.getPopUpWindow(key, chart.getChartIndicatorBox());
                in.show(btnSetup,MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
            });

            keyButton.put(key,btnSetup);
            Separator sep1 = new Separator(Orientation.VERTICAL);
            keySeperator.put(key, sep1);
            toolBarIndicators.getItems().add(btnSetup);
            toolBarIndicators.getItems().add(sep1);
        }
    }

    /**
     * removes all ChartIndicators from the org.sjwimmer.tacharting.chart and toggle bar that are in the toggle bar
     */
    public void clearToogelBar(){

        for (Map.Entry<String, Button> stringButtonEntry : keyButton.entrySet()) {
            chart.getChartIndicatorBox().removeIndicator(stringButtonEntry.getKey());
        }
    }

    /**
     * Opens a FileChooser dialog and adds excel or csv ohlc org.sjwimmer.tacharting.data as TimeSeries to the current watchlist
     */
    public void openCsvExcelDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Csv/Excel File(s)");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(EXTENSION_FILTER_CSV, EXTENSION_FILTER_EXCEL);

        List<File> files = fileChooser.showOpenMultipleDialog((vbxChart).getScene().getWindow());
        if(files == null) {
            return;
        }

        for(File file: files){
            if(file==null) {
                continue;
            }
            int extPoint = file.getName().lastIndexOf(".");
            String extension = file.getName().substring(extPoint+1);
            if(EXTENSION_FILTER_CSV.getExtensions().contains("*."+extension)){
                addCSV(file);
                continue;
            }
            if(EXTENSION_FILTER_EXCEL.getExtensions().contains("*."+extension)){
                addExcel(file);
            }
        }
    }

    //TODO: use CSVParserBuilder and Settings parameter to build parser (special case: file is from yahoo request)
    private void addCSV(File file){
        try{
            CSVConnector csvConnector = new CSVConnector();
            TaTimeSeries series = csvConnector.getSeries(file);
            addToWatchlist(series);
        } catch (Exception ioe){
            ioe.printStackTrace();
            //TODO: handle..
        }
    }

    public void settingCSV(){
        new CsvSettingsManager();
    }

    public void settingsYahoo(){
        new YahooSettingsManager();
    }

    public void settingsExcel(){

    }

    public void addExcel(File file){
        try{
            ExcelConnector excelConnector = new ExcelConnector();
            TaTimeSeries series = excelConnector.getSeries(file);
            addToWatchlist(series);
        } catch (Exception e){
            e.printStackTrace(); //TODO
        }
    }

    public void loadDataFromSelectedApi(){
        switch (choiceBoxAPI.valueProperty().get()){
            case Yahoo:{
                addYahoo();
                break;}
            case AlphaVantage: {

                break;
            }
            default: addYahoo();
        }
    }

    //TODO: store as csv to get possibility to reload the file
    private void addYahoo(){
        logger.debug("Start Yahoo request...");
        String symbol = fieldSearch.getText();

        if(!symbol.equals("")) {
            YahooConnector yahooConnector = new YahooConnector();
            try {
                TaTimeSeries series = yahooConnector.getSeries(symbol);
                addToWatchlist(series);
            } catch (Exception io) {
                io.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Could not found Symbol: " + symbol);
                alert.setTitle("Symbol not found");
                alert.show();
            }
        } else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION,"Empty input");
            alert.setTitle("Symbol not found");
            alert.show();
        }
    }

    public void addAlphaVantage(){
        logger.debug("Start AlphaVantage request...");
       //TODO: https://www.alphavantage.co/
    }

    /***** Table Cells and logic **************************************************************************************/

    /**
     * Symbol table cell (not needed at the moment)
     * @param <T>
     */
    class  SymbolTableCell <T extends String> extends  TableCell<TimeSeriesTableCell, T>{

        @Override
        protected void updateItem(T item, boolean empty){
            super.updateItem(item, empty);

            if(item == null){
                setStyle("");
                setText(null);
                return;
            }
            if(item.equals("")){
                setText("unnamed");
            }
            setText(item.toString());
        }
    }

    class RequestData extends Service<Void>{

        @Override
        protected Task<Void> createTask() {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {

                    try {
                        List<SQLKey> symbols = sqlConnector.getKeyList(GeneralTimePeriod.DAY);
                        int i = 0;
                        updateProgress(i, symbols.size()-1);
                        logger.debug("Request symbol list");
                        for(SQLKey key: symbols){
                            addToWatchlist(sqlConnector.getSeries(key));
                            logger.debug("Added '{}' to watchlist",key);
                            updateMessage("Added '{}' to watchlist");
                            updateProgress(++i, symbols.size()-1);
                        }
                    } catch (Exception e){
                        logger.error("Error while requesting data from database: {}"+e.getMessage());
                        e.printStackTrace();
                    }

                    return null;
                }
            };
            return task;
        }
    }
}
