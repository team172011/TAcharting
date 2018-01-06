package chart.handlers;

import chart.TaChartCanvas;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;

public class TaTooltipHandler extends TaAbstractMouseHandlerFX {

    /**
     * Creates a new instance with the specified ID.
     *
     * @param id  the handler id ({@code null} not permitted).
     */
    public TaTooltipHandler(String id) {
        super(id, false, false, false, false);
    }

    /**
     * Handles a mouse moved event by updating the tooltip.
     *
     * @param canvas  the chart canvas ({@code null} not permitted).
     * @param e  the mouse event.
     */
    @Override
    public void handleMouseMoved(TaChartCanvas canvas, MouseEvent e) {
        if (!canvas.isTooltipEnabled()) {
            return;
        }
        String text = getTooltipText(canvas, e.getX(), e.getY());
        canvas.setTooltip(text, e.getScreenX(), e.getScreenY());
    }

    /**
     * Returns the tooltip text.
     *
     * @param canvas  the canvas that is displaying the chart.
     * @param x  the x-coordinate of the mouse pointer.
     * @param y  the y-coordinate of the mouse pointer.
     *
     * @return String The tooltip text (possibly {@code null}).
     */
    private String getTooltipText(TaChartCanvas canvas, double x, double y) {
        ChartRenderingInfo info = canvas.getRenderingInfo();
        if (info == null) {
            return null;
        }
        EntityCollection entities = info.getEntityCollection();
        if (entities == null) {
            return null;
        }
        ChartEntity entity = entities.getEntity(x, y);
        if (entity == null) {
            return null;
        }
        return entity.getToolTipText();
    }

}
