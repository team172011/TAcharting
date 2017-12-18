/*
 The MIT License (MIT)

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package chart;

import chart.types.IndicatorParameters;
import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.layout.BorderPane;
import org.ta4j.core.TradingRecord;

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RootController implements MapChangeListener<String, ChartIndicator>{

    private TaChart chart;
    private final Map<String, CheckMenuItem> itemMap = new HashMap<>();

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

    @FXML ToolBar toolBar;

    @FXML
    public void initialize(){
        //TODO check here..
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
            box.getChartIndicatorMap().addListener(this);
            buildMenuEntries(box);
        }
    }


    /**
     * Build the menu with entries of all indicators from xml AND added indicators from the indicatorBox
     * @param box
     */
    private void buildMenuEntries(ChartIndicatorBox box){
        final PropertiesManager propsManager = box.getPropertiesManager();

        Iterator<Map.Entry<String, ChartIndicator>> addedIndicators = chart.getChartIndicatorBox().getChartIndicatorMap().entrySet().iterator();
        while(addedIndicators.hasNext()){
            Map.Entry<String, ChartIndicator> entry = addedIndicators.next();
            addToCategory(entry.getKey(),entry.getValue().getCategory());
        }

        final List<String> keys = propsManager.getAllKeys();
        for(String key: keys){
            try{
                IndicatorParameters.TaCategory category = propsManager.getCategory(key);
                addToCategory(key, category);
            } catch (XPathExpressionException xpe){
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,xpe.getMessage()).show());
            }
        }

        Iterator<Map.Entry<String, TradingRecord>> it = chart.getChartIndicatorBox().getAllTradingRecords().entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, TradingRecord> entry = it.next();
            addToTradingRecord(entry.getKey(),entry.getValue());
        }
    }

    private void addToTradingRecord(String key, TradingRecord value) {
        CheckMenuItem item = new CheckMenuItem(key);
        item.setOnAction(event -> { chart.plotTradingRecord(value, item.isSelected()); });
        tradingRecords.getItems().add(item);
    }


    private void addToCategory(String key, IndicatorParameters.TaCategory category){
        String[] el = key.split("_");
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
    Map<String, Button> keyButton = new HashMap<>();
    Map<String, Separator> keySeperator = new HashMap<>();

    /**
     * Update the ToolBar
     * Called every time an ChartIndicator has been added or removed to the
     * {@link ChartIndicatorBox chartIndicatorBox} of the underlying {@link TaChart chart}
     *
     * @param change Change<? extends String, ? extends ChartIndicator>
     */
    @Override
    public void onChanged(Change<? extends String, ? extends ChartIndicator> change) {
        String key = change.getKey();

        if(change.wasRemoved()){
            toolBar.getItems().remove(keyButton.get(key));
            toolBar.getItems().remove(keySeperator.get(key));
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
            toolBar.getItems().add(btnSetup);
            toolBar.getItems().add(sep1);
        }
    }

    /**
     * removes all ChartIndicators from the chart and toggle bar that are in the toggle bar
     */
    public void clearToogelBar(){

        Iterator<Map.Entry<String, Button>> it = keyButton.entrySet().iterator();
        while (it.hasNext()){
            chart.getChartIndicatorBox().removeIndicator(it.next().getKey());
        }
    }
}
