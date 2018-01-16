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

package chart;

import chart.api.YahooConnector;
import chart.parameters.IndicatorParameters;
import chart.parameters.Parameter;
import chart.settings.CsvSettingsManager;
import chart.settings.IndicatorPopUpWindow;
import chart.settings.YahooSettingsManager;
import chart.utils.FormatUtils;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static chart.parameters.Parameter.EXTENSION_FILTER_CSV;
import static chart.parameters.Parameter.EXTENSION_FILTER_EXCEL;

public class RootController implements MapChangeListener<String, ChartIndicator>{

    private final Logger logger = LoggerFactory.getLogger(RootController.class);
    private TaChart chart;
    private final Map<String, CheckMenuItem> itemMap = new HashMap<>();
    private final ObservableList<TimeSeriesTableCell> tableData = FXCollections.observableArrayList();

    @FXML BorderPane borderPane;

    @FXML Menu indicatorsMenu;
    @FXML Menu candles;
    @FXML Menu def;
    @FXML Menu custom;
    @FXML Menu bollinger;
    @FXML Menu statistics;
    @FXML Menu volume;
    @FXML Menu ichimoku;
    @FXML Menu helpers;
    @FXML Menu keltner;
    @FXML Menu strategy;
    @FXML Menu tradingRecords;

    @FXML ToolBar toolBarIndicators;
    @FXML ComboBox<Parameter.ApiProvider> choiceBoxAPI;

    @FXML private TableView<TimeSeriesTableCell> tblSymbol;
    @FXML TableColumn<TimeSeriesTableCell, String> colSymbol;
    @FXML TextField fieldSearch;

    @FXML
    public void initialize(){
        fieldSearch.textProperty().addListener((ov, oldValue, newValue) -> fieldSearch.setText(newValue.toUpperCase()));
        colSymbol.setCellValueFactory(new PropertyValueFactory<>("Name"));
        //colSymbol.setCellFactory(column -> new SymbolTableCell());
        colSymbol.getTableView().setItems(tableData);
        colSymbol.getTableView().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                TimeSeries series = colSymbol.getTableView().getSelectionModel().getSelectedItem().getTimeSeries();
                this.chart.getChartIndicatorBox().setTimeSeries(series);
            }
        });

        colSymbol.getTableView().setContextMenu(buildContextMenu());
        choiceBoxAPI.setItems(FXCollections.observableArrayList(Parameter.ApiProvider.values()));
        choiceBoxAPI.setValue(Parameter.ApiProvider.Yahoo);
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
     * This function has to be called before showing the chart stage
     * @param box the {@link ChartIndicatorBox ChartIndicatorBox} with ChartIndicators, TimeSeries
     *            and TradingRecords for the chart
     */
    public void setIndicatorBox(ChartIndicatorBox box){
        if (box != null) {
            chart = new TaChart(box);
            borderPane.setCenter(chart);
            chart.getStylesheets().add(getClass().getClassLoader().getResource("charting-chartStackPane.css").toExternalForm());
            box.getChartIndicatorMap().addListener(this);
            buildMenuEntries(box);
            Platform.runLater(()->tableData.add(new TimeSeriesTableCell(box.getTimeSeries())));
        }
    }


    /**
     * Build the menu with entries colorOf all indicators from xml AND added indicators from the indicatorBox
     * @param box
     */
    private void buildMenuEntries(ChartIndicatorBox box){

        final PropertiesManager propsManager = box.getPropertiesManager();

        for (Map.Entry<String, ChartIndicator> entry : chart.getChartIndicatorBox().getChartIndicatorMap().entrySet()) {
            addToCategory(entry.getKey(), entry.getValue().getCategory());
        }

        final List<String> keys = propsManager.getAllKeys();
        for(String key: keys){
            try{
                IndicatorParameters.IndicatorCategory category = propsManager.getCategory(key);
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


    private void addToCategory(String key, IndicatorParameters.IndicatorCategory category){
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
     * {@link ChartIndicatorBox chartIndicatorBox} colorOf the underlying {@link TaChart chart}
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
     * removes all ChartIndicators from the chart and toggle bar that are in the toggle bar
     */
    public void clearToogelBar(){

        for (Map.Entry<String, Button> stringButtonEntry : keyButton.entrySet()) {
            chart.getChartIndicatorBox().removeIndicator(stringButtonEntry.getKey());
        }
    }

    /**
     * Opens a FileChooser dialog and adds excel or csv ohlc data as TimeSeries to the current watchlist
     */
    public void addEcxelOrCSV(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Csv/Excel File(s)");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(EXTENSION_FILTER_CSV, EXTENSION_FILTER_EXCEL);

        List<File> files = fileChooser.showOpenMultipleDialog((borderPane).getScene().getWindow());
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

            CSVReader reader = new CSVReaderBuilder(new FileReader(file)).withCSVParser(new CSVParser()).build();
            String line[];
            line = reader.readNext();
            Map<Parameter.Columns, Integer> headers = FormatUtils.getHeaderMap(Arrays.asList(line));
            line = reader.readNext();
            String name = line[0];
            Parameter.TimeFormat timeFormat = Parameter.TimeFormat.from(line[1]);

            List<Tick> ticks = new ArrayList<>();
            while((line = reader.readNext()) != null) {
                ticks.add(FormatUtils.extractOHLCData(headers,timeFormat,line));
            }
            if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
                Collections.reverse(ticks);
            }
            Platform.runLater(()-> tableData.add(new TimeSeriesTableCell(new BaseTimeSeries(name==null?"unnamed":name,ticks))));
        } catch (IOException ioe){
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
        try {
            FileInputStream inputStream = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(inputStream);
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();

            // first row with header description
            Row infoRow = rowIterator.next();
            Iterator<Cell> cellIterator = infoRow.cellIterator();


            ArrayList<String> headerLine = new ArrayList<>();
            while (cellIterator.hasNext()){
                Cell cell = cellIterator.next();
                headerLine.add(cell.getStringCellValue());
            }

            Map<Parameter.Columns, Integer> headerMap = FormatUtils.getHeaderMap(headerLine);

            // second row with name and time ofFormat
            infoRow = rowIterator.next();
            String name = infoRow.getCell(0).getStringCellValue();
            String timeFormat = infoRow.getCell(1).getStringCellValue();

            Parameter.TimeFormat timeFormat1 = Parameter.TimeFormat.from(timeFormat);

            List<Tick> ticks = new ArrayList<>();


            while (rowIterator.hasNext()){
                Row row = rowIterator.next();
                cellIterator = row.cellIterator();
                ArrayList<String> list = new ArrayList<>();
                while(cellIterator.hasNext()){
                    list.add(cellIterator.next().getStringCellValue());
                }
                Tick tick = FormatUtils.extractOHLCData(headerMap,timeFormat1,list.toArray(new String[list.size()]));
                ticks.add(tick);
            }
            if(ticks.get(ticks.size()-1).getEndTime().isBefore(ticks.get(0).getEndTime())){
                Collections.reverse(ticks);
            }
            Platform.runLater(()-> tableData.add(new TimeSeriesTableCell(new BaseTimeSeries(name==null?"unnamed":name,ticks))));

        } catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cannot read excel");
            alert.setHeaderText(file.getName()+" could not be read");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
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
    public void addYahoo(){
        logger.debug("Start Yahoo request...");
        String symbol = fieldSearch.getText();

        if(!symbol.equals("")) {
            YahooConnector yahooConnector = new YahooConnector();
            try {
                File file = yahooConnector.request(symbol);
                addCSV(file);
                file.delete();
            } catch (IOException io) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Symbol not found");
                alert.setHeaderText(null);
                alert.setContentText("Could not found Symbol: " + symbol);

                alert.showAndWait();
            }
        } else{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Symbol not found");
            alert.setHeaderText(null);
            alert.setContentText("Empty input");

            alert.showAndWait();
        }
    }

    public void addAlphaVantage(){
        logger.debug("Start AlphaVantage request...");
       //TODO: https://www.alphavantage.co/
    }

    /****************************************************************************************/
    // Table Cells and logic
    /****************************************************************************************/

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
            if(item.toString().equals("")){
                setText("unnamed");
            }
            setText(item.toString());
        }
    }
}
