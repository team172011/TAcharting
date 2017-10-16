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

package chart.view;

import chart.TaChart;
import chart.TaChartIndicator;
import chart.TaChartIndicatorBox;
import chart.types.IndicatorParameters;
import eu.verdelhan.ta4j.TradingRecord;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class TaChartMenuBar extends JMenuBar implements Observer{

    private JMenu tradingMenu;
    private JMenu settingsMenu;
    private JMenu indicatorsMenu;
    private JMenuItem notifications;

    private TaChart taChart;
    private TaChartIndicatorBox indicatorBox;

    public TaChartMenuBar(TaChartIndicatorBox indicatorBox, TaChart taChart) {
        this.taChart = taChart;
        this.indicatorBox = indicatorBox;
        indicatorBox.addObserver(this);
        settingsMenu = new JMenu("Settings");
        notifications = new JMenuItem("Indicator Parameters");
        tradingMenu = new JMenu("Trading Records");
        indicatorsMenu = new JMenu("Indicators");
        notifications.addActionListener(new NotificationsListener(indicatorBox, this));
        settingsMenu.add(notifications);
        add(settingsMenu);
        add(indicatorsMenu);
        add(tradingMenu);
        updateMenuBar(indicatorBox,taChart);
    }

    public void updateMenuBar(TaChartIndicatorBox indicatorBox, TaChart taChart){
        tradingMenu.removeAll();
        for (MenuElement element: indicatorsMenu.getSubElements()){
            ((JPopupMenu) element).removeAll();
        }

        for(IndicatorParameters.TaCategory c : IndicatorParameters.TaCategory.values()){
            indicatorsMenu.add(c.getMenueElement());
        }

        Iterator<Map.Entry<String, TaChartIndicator>> it = indicatorBox.getChartIndicatorMap().entrySet().iterator();
        SubPlotListener subPlotListener = new SubPlotListener(taChart);
        OverlayListener overlayListener = new OverlayListener(taChart);

        while(it.hasNext()){
            Map.Entry<String, TaChartIndicator> indicatorEntry = it.next();
            TaChartIndicator ci = indicatorEntry.getValue();
            TaCheckBoxItem entry = new TaCheckBoxItem(ci.getGeneralName());

            if (ci.isSubchart()) {
                subPlotListener.setMenuEntry(entry,indicatorEntry.getKey());
            }
            else {
                overlayListener.setMenuEntry(entry, indicatorEntry.getKey());
            }
            indicatorsMenu.getItem(ci.getCategory().getId()).add(entry);
        }

        // add tradingRecords
        Iterator<Map.Entry<String, TradingRecord>> itTrade = indicatorBox.getAllTradingRecords().entrySet().iterator();
        while(itTrade.hasNext()){
            Map.Entry<String, TradingRecord> recordEntry = itTrade.next();
            TaCheckBoxItem entry = new TaCheckBoxItem(recordEntry.getKey());
            entry.addActionListener(new TradingRecordListener(taChart, entry, recordEntry.getValue()));
            this.tradingMenu.add(entry);
        }
        this.repaint();
    }

    //TODO: just remove/replace necessary elements
    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Updated");
        updateMenuBar(indicatorBox, taChart);
    }


    /**
     * Simple Listener to open the view for the parameter settings
     */
    class NotificationsListener implements ActionListener{
        private TaChartIndicatorBox indicatorBox;
        private TaChartMenuBar menuBar;

        NotificationsListener(TaChartIndicatorBox indicatorBox, TaChartMenuBar menuBar){
            this.indicatorBox = indicatorBox;
            this.menuBar = menuBar;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ParametersView taParametersView = new ParametersView(indicatorBox, menuBar);
            taParametersView.setVisible(true);
        }
    }

    /**
     * Abstract Listener for storing and plotting charts on TaChart
     */
    abstract class PlotListener implements ActionListener{
        protected TaChart taChart;
        protected Map<TaCheckBoxItem, String> mapItemToPlot;

        public PlotListener(TaChart taChart){
            mapItemToPlot = new HashMap<>();
            this.taChart=taChart;
        }

        public void setMenuEntry(TaCheckBoxItem taCheckBoxItem, String identifier){
            taCheckBoxItem.addActionListener(this);
            this.mapItemToPlot.put(taCheckBoxItem,identifier);
        }

        protected List<String> getSelectedIdentifiers(){
            Iterator<Map.Entry<TaCheckBoxItem, String>> it = mapItemToPlot.entrySet().iterator();
            List<String> selectedIdentifiers = new ArrayList<>();
            while(it.hasNext()){
                Map.Entry<TaCheckBoxItem, String> entry = it.next();
                if (entry.getKey().isSelected()){
                    selectedIdentifiers.add(entry.getValue());
                }
            }

            return selectedIdentifiers;
        }
    }

    /**
     * PlotListener implementation for overlays
     */
    class OverlayListener extends PlotListener{

        OverlayListener(TaChart taChart){
            super(taChart);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.taChart.plotOverlays(getSelectedIdentifiers());
        }
    }

    /**
     * PlotListener implementation for sub plots
     */
    class SubPlotListener extends PlotListener{

        SubPlotListener(TaChart taChart){
            super(taChart);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            this.taChart.plotSubPlots(getSelectedIdentifiers());
        }
    }


    /**
     * ActionListener to plot a record an a TaChart
     */
    class TradingRecordListener implements ActionListener{

        private TaChart taChart;
        private TradingRecord record;
        private TaCheckBoxItem checkBoxItem;

        public TradingRecordListener(TaChart taChart, TaCheckBoxItem checkBoxItem, TradingRecord record){
            this.taChart = taChart;
            this.record = record;
            this.checkBoxItem = checkBoxItem;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            taChart.plotTradingRecord(record,checkBoxItem.isSelected());
        }
    }
}
