package org.sjwimmer.tacharting.chart.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.jfree.chart.plot.Marker;
import org.sjwimmer.tacharting.chart.controller.manager.BaseIndicatorParameterManager;
import org.sjwimmer.tacharting.chart.model.BaseIndicatorBox;
import org.sjwimmer.tacharting.chart.model.ChartIndicator;
import org.sjwimmer.tacharting.chart.model.IndicatorKey;
import org.sjwimmer.tacharting.chart.model.TaTimeSeries;
import org.sjwimmer.tacharting.chart.model.types.IndicatorType;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaChart2 extends BorderPane {

    private final WebView webView = new WebView();
    private final WebEngine webEngine = webView.getEngine();

    private final Map<TradingRecord, List<Marker>> mapTradingRecordMarker = new HashMap<>();;
    //private final List<XYPlot> currentSubPlots;

    private final SimpleObjectProperty<TaTimeSeries> currentSeries;
    private final ObservableMap<String, Strategy> currentStrategies = FXCollections.observableHashMap();
    private final ObservableMap<IndicatorKey, ChartIndicator> currentOverlayKeys = FXCollections.observableHashMap();
    private final ObservableMap<IndicatorKey, ChartIndicator> currentSubplotKeys = FXCollections.observableHashMap();

    public TaChart2(TaTimeSeries series){
        currentSeries = new SimpleObjectProperty<>(series);
        init();
    }

    public void init(){
        BaseIndicatorBox box = new BaseIndicatorBox(currentSeries.get(),new BaseIndicatorParameterManager());
        try {
            ChartIndicator ema = box.loadIndicator(new IndicatorKey(IndicatorType.EMA, 1));
            Button reload = new Button("reload");
            Button openBrowser = new Button("Show in Browser");
            reload.setOnAction(event -> init());
            openBrowser.setOnAction(event -> {
                System.out.println(webEngine.getDocument().getDocumentURI());
            });
            webEngine.load(getClass().getClassLoader().getResource("html/printTests.html").toString());


            getStyleClass().add("browser");
            //add the web view to the scene

            setCenter(webView);
            setBottom(new HBox(reload,openBrowser));
            String json = currentSeries.get().createJsonObject(60).toString();
            // wait until finished loading the site
            webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue == Worker.State.SUCCEEDED){
                    webEngine.executeScript("printData(\'"+json+"\')");
                    addIndicator(ema);
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addIndicator(ChartIndicator indicator){
        String json = indicator.createJsonObject(60).toString();
        System.out.println(json);
        System.out.println(webEngine.executeScript("addIndicator(\'"+json+"\')"));
    }
    
    public void removeIndicator(IndicatorKey key){

    }


    public void removeAllIndicators() {
        //TODO
    }

    public TaTimeSeries getSeries() {
        return null; //TODO
    }

    public ObservableMap<IndicatorKey, ChartIndicator> getCurrentSubplots() {
        return this.currentSubplotKeys;
    }

    public ObservableMap<IndicatorKey, ChartIndicator> getCurrentOverlays() {
        return this.currentOverlayKeys;
    }
}

