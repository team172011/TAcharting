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
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_CSV;
import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_EXCEL;

public class ChartController{

    private final Logger logger = LoggerFactory.getLogger(ChartController.class);
    private TaChart chart;
    private final Map<IndicatorKey, CheckMenuItem> itemMap = new HashMap<>();
    private final ObservableMap<GeneralTimePeriod, List<SQLKey>> tableKey = FXCollections.observableHashMap();

    private final ToolbarPlotsListener plotsListener = new ToolbarPlotsListener();

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
    @FXML private Menu strategyMenu;

    @FXML private ToolBar toolBarIndicators;
    @FXML private ComboBox<Parameter.ApiProvider> choiceBoxAPI;

    @FXML private TextField fieldSearch;
    @FXML private Button btnSearch;
    @FXML private ProgressIndicator priProgress;
    @FXML private ToggleButton tbnStoreData;

    @FXML private TreeView<Key> tvWatchlist;

    public ChartController(){
    }

    @FXML
    public void initialize(){
        try{
            ImageView indicatorImage = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("icons/indicator.png")));
            indicatorsMenu.setGraphic(indicatorImage);
            ImageView strategyImage = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("icons/strategy.png")));
            strategyMenu.setGraphic(strategyImage);
        } catch (Exception e){
            logger.error(e.getMessage());
        }

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
        //colSymbol.setCellFactory(column -> new SymbolTableCell());

        buildWatchlist();

        choiceBoxAPI.setItems(FXCollections.observableArrayList(Parameter.ApiProvider.values()));
        choiceBoxAPI.setValue(Parameter.ApiProvider.Yahoo);

        // Bind tableView to output of SQLConnector
        if(this.sqlConnector == null){
            logger.debug("No SQLConnector set. Create default SqlLiteConnector.");
            sqlConnector = new SqlLiteConnector();
        }
        DataRequestService requestService = new DataRequestService();
        requestService.start();
    }

    /**
     * Build the treeView for the watchlist and add listener to {@link #tableKey}
     */
    private void buildWatchlist() {
        //TODO should not fail if ressource not available
        javafx.scene.image.Image image = new javafx.scene.image.Image(getClass().getClassLoader().getResourceAsStream("icons/watchlistEntry.png"));
        javafx.scene.image.Image imageNode = new javafx.scene.image.Image(getClass().getClassLoader().getResourceAsStream("icons/watchlistList.png"));


        final TreeItem<Key> root = new TreeItem<>(new Key("Default Watchlists"));
        root.setExpanded(true);

        tvWatchlist.setRoot(root);
        tvWatchlist.setContextMenu(buildContextMenu());
        tvWatchlist.getSelectionModel().selectedItemProperty().addListener((observable, o, n)->{
            if(n.getValue() instanceof SQLKey){ // is symbol entry was selected
                try {
                    TaTimeSeries series = sqlConnector.getSeries(((SQLKey) n.getValue()));
                    chart.getChartIndicatorBox().setTimeSeries(series);
                    TableColumn header = new TableColumn("Strategies");
                } catch (Exception sql){
                    sql.printStackTrace();
                }
            }
        });

        for (GeneralTimePeriod table: GeneralTimePeriod.values()){
            TreeItem<Key> it = new TreeItem<Key>(new Key(table.toString()),new ImageView(imageNode));
            root.getChildren().add(it);
            //tableKey.put(table,new ArrayList<>()); // init map with empty lists
        }

        tableKey.addListener((MapChangeListener<GeneralTimePeriod,List<SQLKey>>) listener -> {
            if(listener.wasRemoved() || listener.wasAdded()){
                for(TreeItem<Key> item: root.getChildren()){
                    if(GeneralTimePeriod.valueOf(item.getValue().toString()).equals(listener.getKey())){
                        item.getChildren().clear();
                        for (SQLKey key: tableKey.get(GeneralTimePeriod.valueOf(item.getValue().toString()))){
                            item.getChildren().add(new TreeItem<>(key,new ImageView(image)));

                        }
                    }
                }
            }
        });
    }

    private ContextMenu buildContextMenu(){

        final MenuItem itemRemove = new MenuItem("remove");
        itemRemove.setOnAction(value->{
            TreeItem item = tvWatchlist.getSelectionModel().getSelectedItem();
                if(item.getValue() instanceof SQLKey){
                    SQLKey key = (SQLKey) item.getValue();
                    logger.debug("Remove {} from database", key);
                    try{
                        sqlConnector.removeData(key);
                    }catch (Exception e){
                        logger.error(e.getMessage());
                    }

                }
        });

        final MenuItem itemUpdate = new MenuItem("update");
        itemUpdate.setOnAction(e->{
            updateDataFromSelectedApi();
        });

        final ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(itemUpdate, itemRemove);
        return menu;
    }

    /**
     * This function has to be called before showing the stage. It allows the user to add a customized <t>ChartIndicatorBox</t>
     * @param box the {@link BaseIndicatorBox ChartIndicatorBox} with ChartIndicators, TimeSeries
     *            and TradingRecords for the org.sjwimmer.tacharting.chart
     */
    public void setIndicatorBox(IndicatorBox box){
        Objects.requireNonNull(box);
        chart = new TaChart(box);
        chart.currentSubplotKeys.addListener(plotsListener);
        chart.currentOverlayKeys.addListener(plotsListener);
        VBox.setVgrow(chart, Priority.ALWAYS);
        vbxChart.getChildren().add(chart);
//        box.getIndicartors().addListener(this);
        buildMenuEntries(box);
        TaTimeSeries series = box.getTimeSeries();
        storeSeries(series);

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
     *
     * @param series the TaTimeSeries that should be stored in DB
     */
    public synchronized void storeSeries(final TaTimeSeries series){
            new Thread(()-> {
                try{
                    sqlConnector.insertData(series, false);
                    }  catch (SQLException sqle){
                        sqle.printStackTrace();
                }}).start();

    }

    /**
     * Build the menu with entries of all indicators from xml AND add custom indicators from the indicatorBox
     * @param box the ChartIndicatorBox
     */
    private void buildMenuEntries(IndicatorBox box){

        final IndicatorParameterManager propsManager = box.getPropertiesManager();
        for (Map.Entry<IndicatorKey, ChartIndicator> entry : chart.getChartIndicatorBox().getTempIndicators().entrySet()) {
            addToCategory(entry.getKey(), IndicatorCategory.CUSTOM);
        }
        final List<IndicatorKey> keys = propsManager.getAllKeys();
        for (IndicatorKey key: keys){
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
        strategyMenu.getItems().add(item);
    }

    private void addToCategory(IndicatorKey key, IndicatorCategory category){
        CheckMenuItem item = new CheckMenuItem(key.toString());
        item.setId(Integer.toString(key.getId()));
        itemMap.put(key, item);
        item.setOnAction((a)-> {
                if(item.isSelected()){
                    chart.plotIndicator(key);
                }else{
                    chart.removeIndicator(key);
                }
        });

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
            storeSeries(series);
            this.tableKey.get(series.getTimeFormatType()).add(series.getKey());
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
            this.tableKey.get(series.getTimeFormatType()).add(series.getKey());
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
                this.tableKey.get(series.getTimeFormatType()).add(series.getKey());
                if(tbnStoreData.isSelected()){
                    storeSeries(series);
                }
            }
            priProgress.setVisible(false);
        });

        yahooConnector.setOnFailed(vale->{
            yahooConnector.exceptionProperty().get().printStackTrace();
        });
    }

    public void addAlphaVantage(){
        logger.debug("Start AlphaVantage request...");
       //TODO: https://www.alphavantage.co/
    }

    public void clearToggleBar(){
        plotsListener.clearToggleBar();
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
     * Service to load all SQLKeys from database with help of a {@link SQLConnector}
     */
    class DataRequestService extends Service<Void>{


        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    try {
                        for(GeneralTimePeriod table: GeneralTimePeriod.values()){
                            List<SQLKey> keys = sqlConnector.getKeyList(table);
                            tableKey.put(table, keys);
                        }
                    } catch (SQLException e){
                        logger.error("Error while requesting key list from database: {}"+e.getMessage());
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        }
    }


    class ToolbarPlotsListener implements ListChangeListener<IndicatorKey>{

        private Map<IndicatorKey, Button> keyButton = new HashMap<>();
        private Map<IndicatorKey, Separator> keySeperator = new HashMap<>();

        /**
         * Update the ToolBar
         * Called every time an ChartIndicator has been added or removed to the
         * {@link BaseIndicatorBox chartIndicatorBox} colorOf the underlying {@link TaChart org.sjwimmer.tacharting.chart}
         *
         * @param change Change<? extends IndicatorKey, ? extends ChartIndicator>
         */
        @Override
        public void onChanged(Change<? extends IndicatorKey> change) {
            for (IndicatorKey key : change.getRemoved()) {
                toolBarIndicators.getItems().remove(keyButton.get(key));
                toolBarIndicators.getItems().remove(keySeperator.get(key));
                if (!change.wasAdded()) {
                    CheckMenuItem item = itemMap.get(key);
                    if (item != null) {
                        item.setSelected(false);
                    }
                }
            }

            for (IndicatorKey key : change.getAddedSubList()) {
                if (change.wasAdded()) {

                    Button btnSetup = new Button(key.toString());
                    btnSetup.setOnAction((event) -> {
                        IndicatorPopUpWindow in = IndicatorPopUpWindow.getPopUpWindow(key, chart.getChartIndicatorBox());
                        in.show(btnSetup, MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
                    });

                    keyButton.put(key, btnSetup);
                    Separator sep1 = new Separator(Orientation.VERTICAL);
                    keySeperator.put(key, sep1);
                    toolBarIndicators.getItems().add(btnSetup);
                    toolBarIndicators.getItems().add(sep1);
                }
            }
        }

        /**
         * removes all ChartIndicators from the org.sjwimmer.tacharting.chart and toggle bar that are in the toggle bar
         */
        public void clearToggleBar(){
            for (Map.Entry<IndicatorKey, Button> stringButtonEntry : keyButton.entrySet()) {
                chart.getChartIndicatorBox().removeTempIndicator(stringButtonEntry.getKey());
            }
        }
    }
}
