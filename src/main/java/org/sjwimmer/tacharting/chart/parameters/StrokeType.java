package org.sjwimmer.tacharting.chart.parameters;

import java.awt.*;

public enum StrokeType {
    SMALL_LINE(new BasicStroke(1f)),
    DOT_DOT_LINE(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 4.0f, 3.0f, 3.0f}, 0.0f)),
    LINE_LINE(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 5.0f, new float[]{10.0f, 5.0f}, 0.0f)),
    DOTS(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 4.0f, 3.0f, 3.0f}, 0.0f)),
    BIG_DOTS(new BasicStroke(0.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.50f, new float[]{1.0f, 1.0f}, 0.0f)),
    NONE(new BasicStroke());

    public final Stroke stroke;

    StrokeType(Stroke stroke){
        this.stroke = stroke;
    }
}
