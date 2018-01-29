package org.sjwimmer.tacharting.chart.view.jfreechart;

import javafx.scene.input.MouseEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TaPanHandlerFX extends TaAbstractMouseHandlerFX {

    /** The last mouse location seen during panning. */
    private Point2D panLast;

    private double panW;
    private double panH;

    /**
     * Creates a new instance that requires no modifier keys.
     *
     * @param id  the id ({@code null} not permitted).
     */
    public TaPanHandlerFX(String id) {
        this(id, false, false, false, false);
    }

    /**
     * Creates a new instance that will be activated using the specified
     * combination of modifier keys.
     *
     * @param id  the id ({@code null} not permitted).
     * @param altKey  require ALT key?
     * @param ctrlKey  require CTRL key?
     * @param metaKey  require META key?
     * @param shiftKey   require SHIFT key?
     */
    public TaPanHandlerFX(String id, boolean altKey, boolean ctrlKey,
                        boolean metaKey, boolean shiftKey) {
        super(id, altKey, ctrlKey, metaKey, shiftKey);
    }

    /**
     * Handles a mouse pressed event by recording the initial mouse pointer
     * location.
     *
     * @param canvas  the JavaFX canvas ({@code null} not permitted).
     * @param e  the mouse event ({@code null} not permitted).
     */
    @Override
    public void handleMousePressed(TaChartCanvas canvas, MouseEvent e) {
        Plot plot = canvas.getChart().getPlot();
        if (!(plot instanceof Pannable)) {
            canvas.clearLiveHandler();
            return;
        }
        Pannable pannable = (Pannable) plot;
        if (pannable.isDomainPannable() || pannable.isRangePannable()) {
            Point2D point = new Point2D.Double(e.getX(), e.getY());
            Rectangle2D dataArea = canvas.findDataArea(point);
            if (dataArea != null && dataArea.contains(point)) {
                this.panW = dataArea.getWidth();
                this.panH = dataArea.getHeight();
                this.panLast = point;
                canvas.setCursor(javafx.scene.Cursor.MOVE);
            }
        }
        // the actual panning occurs later in the mouseDragged() method
    }

    /**
     * Handles a mouse dragged event by calculating the distance panned and
     * updating the axes accordingly.
     *
     * @param canvas  the JavaFX canvas ({@code null} not permitted).
     * @param e  the mouse event ({@code null} not permitted).
     */
    @Override
    public void handleMouseDragged(TaChartCanvas canvas, MouseEvent e) {
        if (this.panLast == null) {
            //handle panning if we have a start point else unregister
            canvas.clearLiveHandler();
            return;
        }

        JFreeChart chart = canvas.getChart();
        double dx = e.getX() - this.panLast.getX();
        double dy = e.getY() - this.panLast.getY();
        if (dx == 0.0 && dy == 0.0) {
            return;
        }
        double wPercent = -dx / this.panW;
        double hPercent = dy / this.panH;
        boolean old = chart.getPlot().isNotify();
        chart.getPlot().setNotify(false);
        Pannable p = (Pannable) chart.getPlot();
        PlotRenderingInfo info = canvas.getRenderingInfo().getPlotInfo();
        if (p.getOrientation().isVertical()) {
            p.panDomainAxes(wPercent, info, this.panLast);
            p.panRangeAxes(hPercent, info, this.panLast);
        }
        else {
            p.panDomainAxes(hPercent, info, this.panLast);
            p.panRangeAxes(wPercent, info, this.panLast);
        }
        this.panLast = new Point2D.Double(e.getX(), e.getY());
        chart.getPlot().setNotify(old);
    }

    @Override
    public void handleMouseReleased(TaChartCanvas canvas, MouseEvent e) {
        //if we have been panning reset the cursor
        //unregister in any case
        if (this.panLast != null) {
            canvas.setCursor(javafx.scene.Cursor.DEFAULT);
        }
        this.panLast = null;
        canvas.clearLiveHandler();
    }

}
