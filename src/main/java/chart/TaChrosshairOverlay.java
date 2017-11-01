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

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

public class TaChrosshairOverlay extends CrosshairOverlay{

    @Override
    public void paintOverlay(Graphics2D g2, ChartPanel chartPanel) {
        Shape savedClip = g2.getClip();
        Rectangle2D dataArea = chartPanel.getScreenDataArea();
        g2.clip(dataArea);
        JFreeChart chart = chartPanel.getChart();
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();
        List<XYPlot> subplots = plot.getSubplots();


        ValueAxis xAxis = subplots.get(0).getDomainAxis();
        RectangleEdge xAxisEdge = subplots.get(0).getDomainAxisEdge();
        Iterator iterator = getDomainCrosshairs().iterator();
        while (iterator.hasNext()) {
            Crosshair ch = (Crosshair) iterator.next();
            if (ch.isVisible()) {
                double x = ch.getValue();
                double xx = xAxis.valueToJava2D(x, dataArea, xAxisEdge);
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    drawVerticalCrosshair(g2, dataArea, xx, ch);
                }
                else {
                    drawHorizontalCrosshair(g2, dataArea, xx, ch);
                }
            }
        }

        /* TODO: add horizontal crosshair with correct value. Currently adding the subplots shift this value
        maybe something like yy-(v*(subplots.size()-1))


        ValueAxis subyAxis = subplots.get(1).getRangeAxis();
        RectangleEdge subAxisEdge = subplots.get(1).getRangeAxisEdge();
        double v = subyAxis.valueToJava2D(subyAxis.getUpperBound(),dataArea, subAxisEdge);
        System.out.println(v);
        */

        RectangleEdge yAxisEdge = subplots.get(0).getRangeAxisEdge();
        ValueAxis yAxis = subplots.get(0).getRangeAxis();
        iterator = this.getRangeCrosshairs().iterator();
        while (iterator.hasNext()) {
            Crosshair ch = (Crosshair) iterator.next();
            if (ch.isVisible()) {
                double y = ch.getValue();
                double yy = yAxis.valueToJava2D(y, dataArea, yAxisEdge);
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    drawHorizontalCrosshair(g2, dataArea, yy, ch);
                }
                else {
                    drawVerticalCrosshair(g2, dataArea, yy, ch);
                }
            }
        }
        g2.setClip(savedClip);
    }
}
