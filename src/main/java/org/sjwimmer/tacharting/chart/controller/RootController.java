package org.sjwimmer.tacharting.chart.controller;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import org.sjwimmer.tacharting.chart.api.*;
import org.sjwimmer.tacharting.chart.controller.manager.BaseIndicatorParameterManager;
import org.sjwimmer.tacharting.chart.controller.manager.CsvSettingsManager;
import org.sjwimmer.tacharting.chart.controller.manager.YahooSettingsManager;
import org.sjwimmer.tacharting.chart.model.*;
import org.sjwimmer.tacharting.chart.model.types.GeneralTimePeriod;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_CSV;
import static org.sjwimmer.tacharting.chart.parameters.Parameter.EXTENSION_FILTER_EXCEL;

public class RootController {
    private Logger logger = LoggerFactory.getLogger(RootController.class);
    private SQLConnector sqlConnector;
    private final ObservableMap<GeneralTimePeriod, List<SQLKey>> tableKey = FXCollections.observableHashMap();

    private final ObservableMap<SQLKey, ChartController> openCharts = FXCollections.observableHashMap();

    @FXML private TabPane tabCharts;
    @FXML private ComboBox<Parameter.ApiProvider> choiceBoxAPI;

    @FXML private TextField fieldSearch;
    @FXML private Button btnSearch;
    @FXML private ProgressIndicator priProgress;
    @FXML private ToggleButton tbnStoreData;

    @FXML private TreeView<Key> tvWatchlist;


    @FXML
    public void initialize() {
        // Bind tableView to output of SQLConnector
        if(this.sqlConnector == null){
            logger.debug("No SQLConnector set. Create default SqlLiteConnector.");
            sqlConnector = new SqlLiteConnector();
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
        ;


        choiceBoxAPI.setItems(FXCollections.observableArrayList(Parameter.ApiProvider.values()));
        choiceBoxAPI.setValue(Parameter.ApiProvider.Yahoo);

        openCharts.addListener((MapChangeListener<SQLKey,ChartController>) change -> {
            if(change.wasRemoved()){
                for(Tab tab : tabCharts.getTabs()){
                    if(tab.getText().equals(change.getKey().symbol)){
                        tabCharts.getTabs().remove(tab);
                    }
                }
            }

            if(change.wasAdded()) {
                ChartController added = change.getValueAdded();
                tabCharts.getTabs().add(new Tab(String.format("  %s  ",change.getKey().symbol), added));
            }
        });

        buildWatchlist();
        DataRequestService requestService = new DataRequestService();
        requestService.start();
    }

    public void addChart(IndicatorBox indicatorBox) {
        ChartController chartController = new ChartController(indicatorBox, this);
        openCharts.put(indicatorBox.getTimeSeries().getKey(), chartController);


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
     * Opens a FileChooser dialog and adds excel or csv ohlc org.sjwimmer.tacharting.data as TimeSeries to the current watchlist
     */
    public void openCsvExcelDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Csv/Excel File(s)");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(EXTENSION_FILTER_CSV, EXTENSION_FILTER_EXCEL);

        List<File> files = fileChooser.showOpenMultipleDialog(btnSearch.getScene().getWindow());
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



    /**
     * Build the treeView for the watchlist and add listener to {@link #tableKey}
     */
    private void buildWatchlist() {

        final TreeItem<Key> root = new TreeItem<>(new Key("Default Watchlists"));
        root.setExpanded(true);

        tvWatchlist.setRoot(root);
        tvWatchlist.setContextMenu(buildContextMenu());
        tvWatchlist.setOnMouseClicked(event -> {
            TreeItem<Key> item = tvWatchlist.getSelectionModel().getSelectedItem();
            if(event.getClickCount() == 2 && item.getValue() instanceof SQLKey){
                try{
                    TaTimeSeries series = sqlConnector.getSeries((SQLKey) item.getValue());
                    addChart(new BaseIndicatorBox(series, new BaseIndicatorParameterManager()));
                } catch (Exception e){
                    logger.error("Error while loading: {} with Connector: {}",
                            item.getValue().toString(),sqlConnector.getClass());
                }
            }
        });

        final Image imageNode = new Image(getClass().getClassLoader().getResourceAsStream("icons/watchlistList.png"));
        for (GeneralTimePeriod table: GeneralTimePeriod.values()){
            TreeItem<Key> it = new TreeItem<>(new Key(table.toString()),new ImageView(imageNode));
            root.getChildren().add(it);
        }

        final Image image = new Image(getClass().getClassLoader().getResourceAsStream("icons/watchlistEntry.png"));
        tableKey.addListener((MapChangeListener<GeneralTimePeriod,List<SQLKey>>) listener -> {
            if(listener.wasRemoved() || listener.wasAdded()){
                for(TreeItem<Key> item: root.getChildren()){
                    if(GeneralTimePeriod.valueOf(item.getValue().toString()).equals(listener.getKey())){
                        item.getChildren().clear();
                        for (SQLKey key: tableKey.get(GeneralTimePeriod.valueOf(item.getValue().toString()))){
                            item.getChildren().add(new TreeItem<>(key, new ImageView(image)));
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
        itemUpdate.setOnAction(e-> updateDataFromSelectedApi());

        final ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(itemUpdate, itemRemove);
        return menu;
    }

    /**
     * Service to load all SQLKeys from database with help of a {@link SQLConnector}
     */
    class DataRequestService extends Service<Void> {


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
}
