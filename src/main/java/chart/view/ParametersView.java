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

import chart.TaChartIndicatorBox;
import chart.TaPropertiesManager;
import chart.types.IndicatorParameters;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * View for the entries of the indicator setting
 */
public class ParametersView extends JFrame {

    private TaPropertiesManager taParameter;

    public ParametersView(TaChartIndicatorBox indicatorBox, TaChartMenuBar menuBar){
        super("Indicator Settings");
        this.taParameter = indicatorBox.getPropertiesManager();


        JTabbedPane parametersPanel = new JTabbedPane();
        for(IndicatorParameters.TaCategory c: IndicatorParameters.TaCategory.values()) { // create tab for each category
            try {
                List<String> keys = taParameter.getKeysForCategory(c);
                JPanel tabCategory = new JPanel(new GridLayout(keys.size(),1,1,15));
                for (String key: keys){ // add all indicator settings for this category
                    IndicatorEntryView entryView = new IndicatorEntryView(key, menuBar, indicatorBox);
                    tabCategory.add(entryView.getRootPanel());
                }
                JScrollPane scrollPane = new JScrollPane(tabCategory);
                parametersPanel.addTab(c.toString(), scrollPane);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        setContentPane(parametersPanel);
        pack();
    }
}
