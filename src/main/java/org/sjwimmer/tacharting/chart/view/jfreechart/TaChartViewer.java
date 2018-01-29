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
package org.sjwimmer.tacharting.chart.view.jfreechart;


import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.interaction.ZoomHandlerFX;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.util.Args;
import org.jfree.chart.util.ExportUtils;
import org.sjwimmer.tacharting.chart.utils.CalculationUtils;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Customized ChartViewer with javafx CrosshairOverlay for this Region
 */
public class TaChartViewer extends Region {

    private TaChartCanvas canvas;

    private final Line xCrosshair;
    private final Line yCrosshair;
    private final Label xLabel;
    private final Label yLabel;

    /**
     * The zoom rectangle is used to display the zooming region when
     * doing a drag-zoom with the mouse.  Most of the time this rectangle
     * is not visible.
     */
    private Rectangle zoomRectangle;

    /** The context menu for the org.sjwimmer.tacharting.chart viewer. */
    private ContextMenu contextMenu;


    /**
     * Creates a new viewer to display the supplied org.sjwimmer.tacharting.chart in JavaFX.
     *
     * @param chart  the org.sjwimmer.tacharting.chart ({@code null} permitted).
     */
    public TaChartViewer(JFreeChart chart) {
        this(chart, true);
    }

    /**
     * Creates a new viewer instance.
     *
     * @param chart  the org.sjwimmer.tacharting.chart ({@code null} permitted).
     * @param contextMenuEnabled  enable the context menu?
     */
    public TaChartViewer(JFreeChart chart, boolean contextMenuEnabled) {
        this.canvas = new TaChartCanvas(chart);
        this.canvas.setTooltipEnabled(true);
        this.canvas.addMouseHandler(new TaZoomHandlerFX("zoom", this));
        setFocusTraversable(true);
        getChildren().add(this.canvas);

        this.zoomRectangle = new Rectangle(0, 0, new Color(0, 0, 1, 0.5));
        this.zoomRectangle.setManaged(false);
        this.zoomRectangle.setVisible(false);
        getChildren().add(this.zoomRectangle);

        this.contextMenu = createContextMenu();
        setOnContextMenuRequested((ContextMenuEvent event) -> {
            contextMenu.show(TaChartViewer.this.getScene().getWindow(),
                    event.getScreenX(), event.getScreenY());
        });

        getContextMenu().setOnShowing(
                e -> TaChartViewer.this.getCanvas().setTooltipEnabled(false));
        getContextMenu().setOnHiding(
                e -> TaChartViewer.this.getCanvas().setTooltipEnabled(true));

        this.xCrosshair = new Line(0,0,this.getPrefWidth(),0);
        this.yCrosshair = new Line(0,0,0,this.getPrefHeight());
        this.xCrosshair.setMouseTransparent(true);
        this.yCrosshair.setMouseTransparent(true);
        this.getChildren().add(xCrosshair);
        this.getChildren().add(yCrosshair);
        this.xLabel = new Label("");
        this.yLabel = new Label("");
        this.yLabel.setMouseTransparent(true);
        this.xLabel.setMouseTransparent(true);
        this.getChildren().add(xLabel);
        this.getChildren().add(yLabel);


        /**Custom Mouse Listener for the CrosshairOverlay */
        this.setOnMouseMoved( e ->{
            final double x = e.getX();
            final double y = e.getY();


            Rectangle2D dataArea = getCanvas().getRenderingInfo().getPlotInfo().getDataArea();

            if(x > dataArea.getMinX() && y > dataArea.getMinY() && x < dataArea.getMaxX() && y < dataArea.getMaxY()) {
                setCrosshairVisible(true);
                CombinedDomainXYPlot combinedDomainXYPlot = (CombinedDomainXYPlot) getCanvas().getChart().getPlot();
                XYPlot plot = (XYPlot) combinedDomainXYPlot.getSubplots().get(0);

                org.jfree.chart.axis.ValueAxis xAxis = plot.getDomainAxis();
                RectangleEdge xAxisEdge = plot.getDomainAxisEdge();

                xCrosshair.setStartY(dataArea.getMinY());
                xCrosshair.setStartX(x);
                xCrosshair.setEndY(dataArea.getMaxY());
                xCrosshair.setEndX(x);
                xLabel.setLayoutX(x);
                xLabel.setLayoutY(dataArea.getMinY());

                double value = xAxis.java2DToValue(e.getX(), dataArea, xAxisEdge);
                long itemLong = (long) (value);
                Date itemDate = new Date(itemLong);
                xLabel.setText(String.valueOf(new SimpleDateFormat().format(itemDate)));


                org.jfree.chart.axis.ValueAxis yAxis = plot.getRangeAxis();
                RectangleEdge yAxisEdge = plot.getRangeAxisEdge();
                Rectangle2D subDataArea = getCanvas().getRenderingInfo().getPlotInfo().getSubplotInfo(0).getDataArea();

                yCrosshair.setStartY(y);
                yCrosshair.setStartX(dataArea.getMinX());
                yCrosshair.setEndX(dataArea.getMaxX());
                yCrosshair.setEndY(y);
                yLabel.setLayoutY(y);
                yLabel.setLayoutX(dataArea.getMinX());
                String yValue = CalculationUtils.roundToString(yAxis.java2DToValue(y, subDataArea, yAxisEdge), 2);
                yLabel.setText(yValue);
            } else {
                setCrosshairVisible(false);
            }
        });
    }

    private void setCrosshairVisible(boolean visible){
        xCrosshair.setVisible(visible);
        yCrosshair.setVisible(visible);
        xLabel.setVisible(visible);
        yLabel.setVisible(visible);
    }
    /**
     * Returns the org.sjwimmer.tacharting.chart that is being displayed by this viewer.
     *
     * @return The org.sjwimmer.tacharting.chart (possibly {@code null}).
     */
    public JFreeChart getChart() {
        return this.canvas.getChart();
    }

    /**
     * Sets the org.sjwimmer.tacharting.chart to be displayed by this viewer.
     *
     * @param chart  the org.sjwimmer.tacharting.chart ({@code null} not permitted).
     */
    public void setChart(JFreeChart chart) {
        Args.nullNotPermitted(chart, "org/sjwimmer/tacharting/chart");
        this.canvas.setChart(chart);
    }

    /**
     * Returns the {@link TaChartCanvas} embedded in this component.
     *
     * @return The {@code ChartCanvas} (never {@code null}).
     *
     * @since 1.0.20
     */
    public TaChartCanvas getCanvas() {
        return this.canvas;
    }

    /**
     * Returns the context menu for this component.
     *
     * @return The context menu for this component.
     */
    public ContextMenu getContextMenu() {
        return this.contextMenu;
    }

    /**
     * Returns the rendering info from the most recent drawing of the org.sjwimmer.tacharting.chart.
     *
     * @return The rendering info (possibly {@code null}).
     *
     * @since 1.0.19
     */
    public ChartRenderingInfo getRenderingInfo() {
        return getCanvas().getRenderingInfo();
    }


    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        this.canvas.setLayoutX(getLayoutX());
        this.canvas.setLayoutY(getLayoutY());
        this.canvas.setWidth(getWidth());
        this.canvas.setHeight(getHeight());
    }

    /**
     * Creates the context menu.
     *
     * @return The context menu.
     */
    private ContextMenu createContextMenu() {
        final ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);
        Menu export = new Menu("Export As");

        MenuItem pngItem = new MenuItem("PNG...");
        pngItem.setOnAction(e -> handleExportToPNG());
        export.getItems().add(pngItem);

        MenuItem jpegItem = new MenuItem("JPEG...");
        jpegItem.setOnAction(e -> handleExportToJPEG());
        export.getItems().add(jpegItem);

        if (ExportUtils.isOrsonPDFAvailable()) {
            MenuItem pdfItem = new MenuItem("PDF...");
            pdfItem.setOnAction(e -> handleExportToPDF());
            export.getItems().add(pdfItem);
        }
        if (ExportUtils.isJFreeSVGAvailable()) {
            MenuItem svgItem = new MenuItem("SVG...");
            svgItem.setOnAction(e -> handleExportToSVG());
            export.getItems().add(svgItem);
        }
        menu.getItems().add(export);
        return menu;
    }

    /**
     * A handler for the export to PDF option in the context menu.
     */
    private void handleExportToPDF() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export to PDF");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                "Portable Document Format (PDF)", "pdf");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            ExportUtils.writeAsPDF(this.canvas.getChart(), (int) getWidth(),
                    (int) getHeight(), file);
        }
    }

    /**
     * A handler for the export to SVG option in the context menu.
     */
    private void handleExportToSVG() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export to SVG");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                "Scalable Vector Graphics (SVG)", "svg");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            ExportUtils.writeAsSVG(this.canvas.getChart(), (int) getWidth(),
                    (int) getHeight(), file);
        }
    }

    /**
     * A handler for the export to PNG option in the context menu.
     */
    private void handleExportToPNG() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export to PNG");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
                "Portable Network Graphics (PNG)", "png");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                ExportUtils.writeAsPNG(this.canvas.getChart(), (int) getWidth(),
                        (int) getHeight(), file);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * A handler for the export to JPEG option in the context menu.
     */
    private void handleExportToJPEG() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export to JPEG");
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("JPEG", "jpg");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                ExportUtils.writeAsJPEG(this.canvas.getChart(), (int) getWidth(),
                        (int) getHeight(), file);
            } catch (IOException ex) {
                // FIXME: show a dialog with the error
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Sets the size and location of the zoom rectangle and makes it visible
     * if it wasn't already visible..  This method is provided for the use of
     * the {@link ZoomHandlerFX} class, you won't normally need to call it
     * directly.
     *
     * @param x  the x-location.
     * @param y  the y-location.
     * @param w  the width.
     * @param h  the height.
     */
    public void showZoomRectangle(double x, double y, double w, double h) {
        this.zoomRectangle.setX(x);
        this.zoomRectangle.setY(y);
        this.zoomRectangle.setWidth(w);
        this.zoomRectangle.setHeight(h);
        this.zoomRectangle.setVisible(true);
    }

    /**
     * Hides the zoom rectangle.  This method is provided for the use of the
     * {@link ZoomHandlerFX} class, you won't normally need to call it directly.
     */
    public void hideZoomRectangle() {
        this.zoomRectangle.setVisible(false);
    }

}

