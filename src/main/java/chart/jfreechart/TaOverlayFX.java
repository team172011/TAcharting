package chart.jfreechart;

import org.jfree.chart.panel.Overlay;

import java.awt.*;

public interface TaOverlayFX extends Overlay {
    /**
     * Paints the content colorOf the overlay onto the specified chart canvas.
     *
     * @param g2  the graphics target ({@code null} not permitted).
     * @param chartCanvas  the chart canvas ({@code null} not permitted).
     */
    void paintOverlay(Graphics2D g2, TaChartCanvas chartCanvas);
}
