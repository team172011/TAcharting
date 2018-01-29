package org.sjwimmer.tacharting.chart.view.jfreechart;

import javafx.scene.input.ScrollEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;

import java.awt.geom.Point2D;

public class TaScrollHandlerFX extends TaAbstractMouseHandlerFX implements TaMouseHandlerFX {
    /** The zoom factor. */
    private double zoomFactor = 0.1;

    /**
     * Creates a new instance with the specified ID.
     *
     * @param id  the handler ID ({@code null} not permitted).
     */
    public TaScrollHandlerFX(String id) {
        super(id, false, false, false, false);
        this.zoomFactor = 0.1;
    };

    /**
     * Returns the zoom factor.  The default value is 0.10 (ten percent).
     *
     * @return The zoom factor.
     */
    public double getZoomFactor() {
        return this.zoomFactor;
    }

    /**
     * Sets the zoom factor (a percentage amount by which the mouse wheel
     * movement will change the org.sjwimmer.tacharting.chart size).
     *
     * @param zoomFactor  the zoom factor.
     */
    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    @Override
    public void handleScroll(TaChartCanvas canvas, ScrollEvent e) {
        JFreeChart chart = canvas.getChart();
        Plot plot = chart.getPlot();
        if (plot instanceof Zoomable) {
            Zoomable zoomable = (Zoomable) plot;
            handleZoomable(canvas, zoomable, e);
        }
        else if (plot instanceof PiePlot) {
            PiePlot pp = (PiePlot) plot;
            pp.handleMouseWheelRotation((int) e.getDeltaY());
        }
    }

    /**
     * Handle the case where a plot implements the {@link Zoomable} interface.
     *
     * @param zoomable  the zoomable plot.
     * @param e  the mouse wheel event.
     */
    private void handleZoomable(TaChartCanvas canvas, Zoomable zoomable,
                                ScrollEvent e) {
        // don't zoom unless the mouse pointer is in the plot's org.sjwimmer.tacharting.data area
        ChartRenderingInfo info = canvas.getRenderingInfo();
        PlotRenderingInfo pinfo = info.getPlotInfo();
        Point2D p = new Point2D.Double(e.getX(), e.getY());
        if (pinfo.getDataArea().contains(p)) {
            Plot plot = (Plot) zoomable;
            // do not notify while zooming each axis
            boolean notifyState = plot.isNotify();
            plot.setNotify(false);
            int clicks = (int) e.getDeltaY();
            double zf = 1.0 + this.zoomFactor;
            if (clicks < 0) {
                zf = 1.0 / zf;
            }
            if (canvas.isDomainZoomable()) {
                zoomable.zoomDomainAxes(zf, pinfo, p, true);
            }
            if (canvas.isRangeZoomable()) {
                zoomable.zoomRangeAxes(zf, pinfo, p, true);
            }
            plot.setNotify(notifyState);  // this generates the change event too
        }
    }

}
