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

package org.sjwimmer.tacharting.chart.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.sjwimmer.tacharting.chart.api.*;
import org.sjwimmer.tacharting.chart.api.settings.CsvSettingsManager;
import org.sjwimmer.tacharting.chart.api.settings.YahooSettingsManager;
import org.sjwimmer.tacharting.chart.model.*;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.view.IndicatorPopUpWindow;
import org.sjwimmer.tacharting.chart.view.TaChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_CSV;
import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_EXCEL;

public class ChartController implements MapChangeListener<String, ChartIndicator>{

    private final Logger logger = LoggerFactory.getLogger(ChartController.class);
    private TaChart chart;
    private final Map<String, CheckMenuItem> itemMap = new HashMap<>();
    private final ObservableList<TaTimeSeries> tableData = FXCollections.observableArrayList();

    private SQLConnector sqlConnector;

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

    @FXML private TableView<TaTimeSeries> tblSymbol;
    @FXML private TableColumn<TaTimeSeries, String> colSymbol;
    @FXML private TextField fieldSearch;
    @FXML private Button btnSearch;
    @FXML private ProgressIndicator priProgress;
    @FXML private ToggleButton tbnStoreData;

    public ChartController(){
    }

    @FXML
    public void initialize(){
        fieldSearch.textProperty().addListener((ov, oldValue, newValue) -> fieldSearch.setText(newValue.toUpperCase()));
        fieldSearch.setOnKeyPressed(event->{
            if(event.getCode() == KeyCode.ENTER){
                loadDataFromSelectedApi(fieldSearch.getText().split("[;,]"));
                fieldSearch.clear();
            } });
        btnSearch.setOnAction(event ->{
            loadDataFromSelectedApi(fieldSearch.getText().split("[;,]"));
            fieldSearch.clear(); });
        btnSearch.disableProperty().bind(fieldSearch.textProperty().isEmpty());
        priProgress.setVisible(false);
        colSymbol.setCellValueFactory(new PropertyValueFactory<>("Name"));
        //colSymbol.setCellFactory(column -> new SymbolTableCell());
        tblSymbol.setItems(tableData);
        tblSymbol.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                TaTimeSeries series = colSymbol.getTableView().getSelectionModel().getSelectedItem();
                this.chart.getChartIndicatorBox().setTimeSeries(series);
            } });

        colSymbol.getTableView().setContextMenu(buildContextMenu());
        choiceBoxAPI.setItems(FXCollections.observableArrayList(Parameter.ApiProvider.values()));
        choiceBoxAPI.setValue(Parameter.ApiProvider.Yahoo);

        // Bind tableView to output of SQLConnector
        if(this.sqlConnector == null){
            logger.debug("No SQLConnector set. Create default SqlLiteConnector.");
            sqlConnector = new SqlLiteConnector();
        }
        DataRequestService requestService = new DataRequestService();
        tblSymbol.itemsProperty().bind(requestService.valueProperty());
        requestService.start();
    }

    /**
     * This function has to be called before showing the stage. It allows the user to add a customized <t>ChartIndicatorBox</t>
     * @param box the {@link BaseIndicatorBox ChartIndicatorBox} with ChartIndicators, TimeSeries
     *            and TradingRecords for the org.sjwimmer.tacharting.chart
     */
    public void setIndicatorBox(IndicatorBox box){
        Objects.requireNonNull(box);
        chart = new TaChart(box);
        VBox.setVgrow(chart, Priority.ALWAYS);
        vbxChart.getChildren().add(chart);

        box.getIndicartors().addListener(this);
        buildMenuEntries(box);
        TaTimeSeries series = box.getTimeSeries();
        storeData(series);
        addToWatchlist(series);
    }

    /**
     * Sets the {@link SQLConnector} for this controller. Allows the user to set up
     * his own <code>SQLConnector</code> to work with data of his own database
     * @param sqlConnector the {@link SQLConnector} implementing class
     */
    public void setSqlConnector(SQLConnector sqlConnector){
        this.sqlConnector = sqlConnector;
    }

    /**
     * Adds a {@link TaTimeSeries} to the watchlist that can be analysed
     * If the <tt>series</tt> is already stored, it will be replaced by the new one
     * @param series a TaTimeSeries
     */
    public void addToWatchlist(TaTimeSeries series) {
        if(tableData.contains(series)){
            int idx = tableData.indexOf(series);
            Platform.runLater(()->tableData.set(idx, series));
        } else {
            Platform.runLater(()->tableData.add(series));
        }
    }

    /**
     *
     * @param series the TaTimeSeries that should be stored in DB
     */
    public synchronized void storeData(final TaTimeSeries series){
            new Thread(()-> {
                try{
                    sqlConnector.insertData(series, false);
                    }  catch (SQLException sqle){
                        sqle.printStackTrace();
                }}).start();

    }

    private ContextMenu buildContextMenu(){
        final MenuItem itemRemove = new MenuItem("remove");
        itemRemove.setOnAction(e -> {
            TaTimeSeries selectedCell = colSymbol.getTableView().getSelectionModel().getSelectedItem();
            colSymbol.getTableView().getItems().remove(selectedCell); });

        final MenuItem itemRemoveDB = new MenuItem("remove from database");
        itemRemoveDB.setOnAction( e ->{
            List<TaTimeSeries> selectedCell = colSymbol.getTableView().getSelectionModel().getSelectedItems();
            try {
                for(TaTimeSeries series: selectedCell) {
                    sqlConnector.removeData(series);
                    colSymbol.getTableView().getItems().remove(series);
                }
            } catch (SQLException sqle){
                sqle.printStackTrace();
            }
        });

        final MenuItem itemUpdate = new MenuItem("update");
        itemUpdate.setOnAction(e->{
            updateDataFromSelectedApi();
        });

        final ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(itemUpdate, itemRemove, itemRemove);
        return menu;
    }

    /**
     * Build the menu with entries of all indicators from xml AND add custom indicators from the indicatorBox
     * @param box the ChartIndicatorBox
     */
    private void buildMenuEntries(IndicatorBox box){

        final IndicatorParameterManager propsManager = box.getPropertiesManager();
        for (Map.Entry<String, ChartIndicator> entry : chart.getChartIndicatorBox().getTempIndicators().entrySet()) {
            addToCategory(entry.getKey(), entry.getValue().getCategory());
        }
        final List<String> keys = propsManager.getAllKeys();
        for (String key: keys){
            try{
                IndicatorCategory category = propsManager.getCategory(key);
                addToCategory(key, category);
            } catch (XPathExpressionException xpe){
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,xpe.getMessage()).show());
            }
        }
        for (Map.Entry<String, Strategy> entry : chart.getChartIndicatorBox().getAllStrategies().entrySet()) {
            addToStrategies(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Adds the items with onActions for the strategies to the menu
     * @param key key/description of the strategy
     * @param strategy the strategy
     */
    private void addToStrategies(String key, Strategy strategy) {
        CheckMenuItem item = new CheckMenuItem(key);
        item.setOnAction(event -> {
            TimeSeriesManager seriesManager = new TimeSeriesManager(chart.getChartIndicatorBox().getTimeSeries());
            TradingRecord value = seriesManager.run(strategy);
            chart.plotTradingRecord(value, item.isSelected()); });
        tradingRecords.getItems().add(item);
    }

    private void addToCategory(String key, IndicatorCategory category){
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
     * {@link BaseIndicatorBox chartIndicatorBox} colorOf the underlying {@link TaChart org.sjwimmer.tacharting.chart}
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
    public void clearToggleBar(){
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


    public void loadDataFromSelectedApi(String... symbol){
        switch (choiceBoxAPI.valueProperty().get()){
            case Yahoo:{
                loadYahooData(symbol);
                break;}
            case AlphaVantage: {
                break;
            }
            default: loadYahooData();
        }
    }

    public void updateDataFromSelectedApi(){
        new Alert(Alert.AlertType.INFORMATION, "Currently not supported");
    }

    private void loadYahooData(String... symbol){

        logger.debug("Start Yahoo request...");
        String[] cleanSymbols = Arrays.stream(symbol).map(e->e.replaceAll("\\s+","")).toArray(String[]::new);

        YahooService yahooConnector = new YahooService(cleanSymbols);
        priProgress.setVisible(true);
        priProgress.progressProperty().bind(yahooConnector.progressProperty());
        yahooConnector.start();
        yahooConnector.setOnSucceeded(value->{
            for(TaTimeSeries series: yahooConnector.getValue()){
                addToWatchlist(series);
            }
            priProgress.setVisible(false);
        });
    }

    public void addAlphaVantage(){
        logger.debug("Start AlphaVantage request...");
       //TODO: https://www.alphavantage.co/
    }

    /** Table Cells and logic **************************************************************************************/

    /**
     * Symbol table cell (not needed at the moment)
     * @param <T>
     */
    class  SymbolTableCell <T extends String> extends  TableCell<TaTimeSeries, T>{

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
            setText(item);
        }
    }

    /**
     * Service to load the data from database with help of a {@link SQLConnector}
     */
    class DataRequestService extends Service<ObservableList<TaTimeSeries>>{

        @Override
        protected Task<ObservableList<TaTimeSeries>> createTask() {
            return new Task<ObservableList<TaTimeSeries>>() {
                @Override
                protected ObservableList<TaTimeSeries> call() throws Exception {
                    List<TaTimeSeries> seriesList = new ArrayList<>();
                    try {
                        List<SQLKey> symbols = sqlConnector.getKeyList(GeneralTimePeriod.DAY);
                        int i = 0;
                        updateProgress(i, symbols.size()-1);
                        logger.debug("Request symbol list");
                        for(SQLKey key: symbols){
                            try {
                                seriesList.add(sqlConnector.getSeries(key));
                            } catch (SQLException sqle){
                                logger.error(String.format(
                                        "Error while requesting data for symbol %s (%s) list from database"
                                        ,key.symbol,key.period));
                                sqle.printStackTrace();
                            }
                            logger.debug("Added '{}' to watchlist",key);
                            updateMessage(String.format("Added '%s' to watchlist",key));
                            updateProgress(++i, symbols.size()-1);
                        }
                    } catch (SQLException e){
                        logger.error("Error while requesting key list from database: {}"+e.getMessage());
                        e.printStackTrace();
                    }
                    return FXCollections.observableArrayList(seriesList);
                }
            };
        }
    }
}
