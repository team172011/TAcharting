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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.sjwimmer.tacharting.chart.controller.manager.IndicatorParameterManager;
import org.sjwimmer.tacharting.chart.model.*;
import org.sjwimmer.tacharting.chart.model.types.IndicatorCategory;
import org.sjwimmer.tacharting.chart.view.TaChart2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Strategy;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartController extends VBox{

    private final RootController rootController;
    private final Logger logger = LoggerFactory.getLogger(ChartController.class);
    private TaChart2 chart;
    private final IndicatorBox box;
    private final Map<IndicatorKey, CheckMenuItem> itemMap = new HashMap<>();

    @FXML private ToolBar toolBarIndicators;
    @FXML private Button clearAll;

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

    public ChartController(IndicatorBox box, RootController rootController){
        this.box = box;
        this.rootController = rootController;

        URL url = getClass().getClassLoader().getResource("fxml/charting-vbxChart.fxml");
        FXMLLoader loader = new FXMLLoader(url);

            loader.setRoot(this);
            loader.setController(this);
        try {
            loader.load();
        } catch (IOException ioe){
            ioe.printStackTrace();
            logger.error(ioe.getMessage());
        }
    }

    @FXML
    public void initialize(){
        initChart(box);
        try{
            ImageView indicatorImage = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("icons/indicator.png")));
            indicatorsMenu.setGraphic(indicatorImage);
            ImageView strategyImage = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("icons/strategy.png")));
            strategyMenu.setGraphic(strategyImage);
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        clearAll.setOnAction(event -> clearToggleBar());

        if(box == null){
            logger.debug("No IndicatorBox set. Create default IndicatorBox with default IndicatorPropertiesManager");
            //TODO: add default box: box = new BaseIndicatorBox();
        }

    }

    /**
     * This function has to be called before showing the stage. It allows the user to add a customized <t>ChartIndicatorBox</t>
     * @param box the {@link BaseIndicatorBox ChartIndicatorBox} with ChartIndicators, TimeSeries
     *            and TradingRecords for the org.sjwimmer.tacharting.chart
     */
    private void initChart(IndicatorBox box){
        chart = new TaChart2(box.getTimeSeries());
        VBox.setVgrow(chart, Priority.ALWAYS);
        getChildren().add(1, chart);
        buildMenuEntries(box);
        //ToolbarManager.bindToolbarAndMenu(toolBarIndicators, itemMap, new ChartingContext(chart, box));
        TaTimeSeries series = box.getTimeSeries();
        rootController.storeSeries(series);
    }

    /**
     * Build the menu with entries of all indicators from xml AND add custom indicators from the indicatorBox
     * @param box the ChartIndicatorBox
     */
    private void buildMenuEntries(IndicatorBox box){

        final IndicatorParameterManager propsManager = box.getPropertiesManager();
        for (Map.Entry<IndicatorKey, ChartIndicator> entry : box.getTempIndicators().entrySet()) {
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
        for (Map.Entry<String, Strategy> entry : box.getAllStrategies().entrySet()) {
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
            if(item.isSelected()){
                //chart.addStrategy(key, strategy);
            } else{
                //chart.removeStrategy(key);
            }
        });
        strategyMenu.getItems().add(item);
    }

    private void addToCategory(IndicatorKey key, IndicatorCategory category){
        CheckMenuItem item = new CheckMenuItem(key.toString());
        item.setId(Integer.toString(key.getId()));
        itemMap.put(key, item);
        item.setOnAction(event -> {
                if(item.isSelected()){
                    //ChartingContext context = new ChartingContext(chart,box);
                    //Optional<ChartIndicator> indicator = CreateFunctions.dialogFunctions.get(key.getType()).apply(context, key).showAndWait();
                    //indicator.ifPresent(chartIndicator -> chart.addIndicator(chartIndicator));
                }else{
                    //chart.removeIndicator(key);
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


    public void clearToggleBar(){
        chart.removeAllIndicators();
    }

    public TaTimeSeries getSeries() {
        return chart.getSeries();
    }
}
