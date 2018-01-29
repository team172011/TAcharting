package org.sjwimmer.tacharting.chart.model.types;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

/**
 * Enum for different types of {@link Shape Shapes} with that a ChartIndicator can be rendered with
 */
public enum ShapeType {

    SMALL_RECTANGLES(new Rectangle(0,0,2,2)),
    BIG_RECTANGLES(new Rectangle(0, 0,5,5)),
    LINE(new Line2D.Double(0,0,2,2)),
    NONE(new Line2D.Double()),
    CIRCLE(new Ellipse2D.Double(0,0,5,5));

    public final Shape shape;

    ShapeType(Shape shape) {
        this.shape = shape;
    }
}
