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

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.indicators.AbstractIndicator;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * An Wrapper for the indicators displaying on a jfreeCharts chart panel.
 * An TaChartIndicator can consist of several ta4j-indiactors (e.g. bollinger bands...)
 */
public class TaChartIndicator {

    private List<Indicator> indicators;
    private List<Paint> indicatorPaints;
    private List<String> indicatorsNames;
    private String generalName;
    private boolean isSubchart;

    private TaTypes.categories categorie = TaTypes.categories.DEFAULT;
    private TaCheckBoxItem menuEntry;

    /**Simple Constructor to add indicator to main chart*/
    public TaChartIndicator(Indicator indicator){
        this(indicator, getRandomColor(), indicator.toString(), false, TaTypes.categories.DEFAULT);
    }

    /**Simple Constructor to add TaChart to main or as a sub chart*/
    public TaChartIndicator(Indicator indicator, boolean isSubchart) {
        this(indicator, getRandomColor(), indicator.toString(), isSubchart, TaTypes.categories.DEFAULT);
    }

    /**Simple Constructor to add TaChart to main or as a sub chart*/
    public TaChartIndicator(Indicator indicator, boolean isSubchart, TaTypes.categories c) {
        this(indicator, getRandomColor(), indicator.toString(), isSubchart, c);
    }

    /**
     * Constructor for creating a chart indicator that is just one ta4j-indicator
     * @param indicator indicator that is needed for plotting the chart indicator
     * @param paint the paint object for plotting the indicator (e.g. the color)
     * @param name the name of the indicator (e.g. MAC)
     * @param isSubchart flag if the chart indicator should be in a sub chart and not on the main chart
     */
    public TaChartIndicator(Indicator indicator, Paint paint, String name, boolean isSubchart, TaTypes.categories c){
        indicators = new ArrayList<>();
        indicatorPaints = new ArrayList<>();
        indicatorsNames = new ArrayList<>();

        this.generalName = name;
        indicators.add(indicator);
        indicatorPaints.add(paint);
        indicatorsNames.add(name);
        this.isSubchart = isSubchart;
        menuEntry = new TaCheckBoxItem(generalName);
        this.categorie = c;
    }

    /**Simple Constructor to add indicator (that consists of several ta4j indicator) to main chart*/
    public TaChartIndicator(List<Indicator> indicators){
        this(indicators, getRandomColor(indicators.size()), getSimpleName(indicators), indicators.get(0).toString(), false, TaTypes.categories.DEFAULT);
    }

    /**Simple Constructor to add indicator (that consists of several ta4j indicator) to main or sub chart*/
    public TaChartIndicator(List<Indicator> indicators, boolean isSubchart){
        this(indicators, getRandomColor(indicators.size()), getSimpleName(indicators), indicators.get(0).toString(), isSubchart, TaTypes.categories.DEFAULT);
    }

    /**Simple Constructor to add indicator (that consists of several ta4j indicator) to main or sub chart*/
    public TaChartIndicator(List<Indicator> indicators, boolean isSubchart, TaTypes.categories c){
        this(indicators, getRandomColor(indicators.size()), getSimpleName(indicators), indicators.get(0).toString(), isSubchart, c);
    }

    /**
     * Constructor for creating a chart indicator that consists of several ta4j-indicators
     * @param indicators a list of (sub) indicators that are needed for plotting the chart indicator
     * @param paints color for evey sub indicator
     * @param names name for every sub indicator (e.g. Upper Bollinger Band)
     * @param generalName the name of the indicator (e.g. Bollinger Bands (20,2) )
     * @param isSubchart flag if the chart indicator should be in a sub chart and not on the main chart
     */
    public TaChartIndicator(List<Indicator> indicators, List<Paint> paints, List<String> names, String generalName, boolean isSubchart, TaTypes.categories c){
        if(indicators.size() != paints.size() || paints.size() != names.size())
            throw new IllegalArgumentException("Different number of indicator, size and/or names. Must be equal");
        this.generalName = generalName;
        this.indicators = indicators;
        this.indicatorPaints = paints;
        this.indicatorsNames = names;
        this.menuEntry = new TaCheckBoxItem(generalName);
        this.isSubchart = isSubchart;
        this.categorie = c;
    }

    public String getGeneralName(){
        return generalName;
    }
    public String getName(int index){
        return indicatorsNames.get(index);
    }

    public void setName(String name){
        setName(name, 0);
    }

    /**
     * Change the display name of the indicator at specific index
     * @param name new name of the indicator
     * @param index index of the indicator in this chart indicator
     */
    public void setName(String name, int index){
        this.indicatorsNames.set(index, name);
    }

    public Paint getPaint(int index){
        return indicatorPaints.get(index);
    }

    public void setPaint(Paint paint){
        setPaint(paint, 0);
    }

    public void setPaint(Paint paint, int index){
        this.indicatorPaints.set(index, paint);
    }

    public List<Indicator> getIndicatorList(){
        return this.indicators;
    }
    public Indicator getIndicator(){
        return getIndicator(0);
    }

    public Indicator getIndicator(int index){
        return indicators.get(index);
    }

    public void setIndicator(AbstractIndicator<Decimal> indicator){
        setIndicator(indicator, 0);
    }

    /**
     * Change the indicator at the specific index
     * @param indicator new indicator for this chart indicator
     * @param index index of the old indicator that should be replaced
     */
    public void setIndicator(AbstractIndicator<Decimal> indicator, int index){
        this.indicators.set(index, indicator);
    }


    public boolean isSubchart(){
        return isSubchart;
    }

    public int getIndicatorsCount(){
        return  indicators.size();
    }

    public JMenuItem getMenuEntry(){
        return menuEntry;
    }

    public TaTypes.categories getCategory(){
        return this.categorie;
    }

    private static Color getRandomColor(){
        Random r = new Random();
        return new Color(r.nextFloat(),r.nextFloat(),r.nextFloat());
    }

    private static List<Paint> getRandomColor(int num) {
        List<Paint> colorList = new ArrayList<>();
        for (int i = 0; i < num; i++)
            colorList.add(getRandomColor());
        return colorList;
    }

    private static List<String> getSimpleName(List<Indicator> indicators){
        List<String> nameList = new ArrayList<>();
        for (Indicator indicator : indicators)
            nameList.add(indicator.toString());
        return  nameList;
    }

}
