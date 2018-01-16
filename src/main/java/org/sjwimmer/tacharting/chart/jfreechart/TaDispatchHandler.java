package org.sjwimmer.tacharting.chart.jfreechart;

import javafx.scene.input.MouseEvent;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;

import java.awt.geom.Point2D;

public class TaDispatchHandler extends TaAbstractMouseHandlerFX {

    /** Records the mouse down location. */
    private Point2D mousePressedPoint;

    /**
     * Creates a new instance.
     *
     * @param id  the id ({@code null} not permitted).
     */
    public TaDispatchHandler(String id) {
        super(id, false, false, false, false);
    }

    /**
     * Handles a mouse pressed event by recording the location of the mouse
     * pointer (so that later we can check that the click isn't part of a
     * drag).
     *
     * @param canvas  the org.sjwimmer.tacharting.chart canvas.
     * @param e  the mouse event.
     */
    @Override
    public void handleMousePressed(TaChartCanvas canvas, MouseEvent e) {
        this.mousePressedPoint = new Point2D.Double(e.getX(), e.getY());
    }

    @Override
    public void handleMouseMoved(TaChartCanvas canvas, MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        ChartEntity entity = canvas.getRenderingInfo().getEntityCollection().getEntity(x, y);
        ChartMouseEventFX event = new ChartMouseEventFX(canvas.getChart(), e, entity);
        for (ChartMouseListenerFX listener : canvas.getChartMouseListeners()) {
            listener.chartMouseMoved(event);
        }
    }

    /**
     * Handles a mouse clicked event by setting the anchor point for the
     * canvas and redrawing the org.sjwimmer.tacharting.chart (the anchor point is a reference point
     * used by the org.sjwimmer.tacharting.chart to determine crosshair lines).
     *
     * @param canvas  the org.sjwimmer.tacharting.chart canvas ({@code null} not permitted).
     * @param e  the mouse event ({@code null} not permitted).
     */
    @Override
    public void handleMouseClicked(TaChartCanvas canvas, MouseEvent e) {
        if (this.mousePressedPoint == null) {
            return;
        }
        double x = e.getX();
        double y = e.getY();
        ChartEntity entity = canvas.getRenderingInfo().getEntityCollection().getEntity(x, y);
        ChartMouseEventFX event = new ChartMouseEventFX(canvas.getChart(), e, entity);
        for (ChartMouseListenerFX listener : canvas.getChartMouseListeners()) {
            listener.chartMouseClicked(event);
        }
    }

}
