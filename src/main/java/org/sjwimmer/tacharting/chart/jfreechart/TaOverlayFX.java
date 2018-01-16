package org.sjwimmer.tacharting.chart.jfreechart;

import org.jfree.chart.panel.Overlay;

import java.awt.*;

public interface TaOverlayFX extends Overlay {
    /**
     * Paints the content colorOf the overlay onto the specified org.sjwimmer.tacharting.chart canvas.
     *
     * @param g2  the graphics target ({@code null} not permitted).
     * @param chartCanvas  the org.sjwimmer.tacharting.chart canvas ({@code null} not permitted).
     */
    void paintOverlay(Graphics2D g2, TaChartCanvas chartCanvas);
}
