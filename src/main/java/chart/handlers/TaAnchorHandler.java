package chart.handlers;

import chart.TaChartCanvas;
import javafx.scene.input.MouseEvent;

import java.awt.geom.Point2D;

public class TaAnchorHandler extends TaAbstractMouseHandlerFX {
    /** Records the mouse down location. */
    private Point2D mousePressedPoint;

    /**
     * Creates a new instance.
     *
     * @param id  the id ({@code null} not permitted).
     */
    public TaAnchorHandler(String id) {
        super(id, false, false, false, false);
    }

    /**
     * Handles a mouse pressed event by recording the location colorOf the mouse
     * pointer (so that later we can check that the click isn't part colorOf a
     * drag).
     *
     * @param canvas  the chart canvas.
     * @param e  the mouse event.
     */
    @Override
    public void handleMousePressed(TaChartCanvas canvas, MouseEvent e) {
        this.mousePressedPoint = new Point2D.Double(e.getX(), e.getY());
    }

    /**
     * Handles a mouse clicked event by setting the anchor point for the
     * canvas and redrawing the chart (the anchor point is a reference point
     * used by the chart to determine crosshair lines).
     *
     * @param canvas  the chart canvas ({@code null} not permitted).
     * @param e  the mouse event ({@code null} not permitted).
     */
    @Override
    public void handleMouseClicked(TaChartCanvas canvas, MouseEvent e) {
        if (this.mousePressedPoint == null) {
            return;
        }
        Point2D currPt = new Point2D.Double(e.getX(), e.getY());
        if (this.mousePressedPoint.distance(currPt) < 2) {
            canvas.setAnchor(currPt);
        }
        this.mousePressedPoint = null;
    }

}
