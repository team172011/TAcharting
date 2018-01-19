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
package org.sjwimmer.tacharting.chart.utils;

import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.sjwimmer.tacharting.chart.parameters.*;

/**
 * Class for several transformations.<p/>
 * In the most cases this class will be needed to transform a String value (from property,
 * database or another file, to the corresponding java object. Mapping between String value and java object is defined
 * by enums (see {@link ChartType}, {@link ShapeType}, {@link StrokeType}, {@link TimeFormatType}), or static variables
 * (see {@link Parameter}
 * This class stores a bunch of static static {@link StringConverter StringConverters} to allow theses
 * transformations.
 */
public class ConverterUtils {

    public static StringConverter<Color> ColorFxConverter = new StringConverter<javafx.scene.paint.Color>() {
        @Override
        public String toString(javafx.scene.paint.Color color) {
            return String.format("%s,%s,%s,%s",color.getRed(),color.getGreen(),color.getBlue(),color.getBrightness());
        }

        @Override
        public javafx.scene.paint.Color fromString(String color) {
            String[] rgb = color.split(",");
            if (rgb.length == 3) {
                return javafx.scene.paint.Color.rgb((int)Float.parseFloat(rgb[0])*255, (int)Float.parseFloat(rgb[1])*255,(int)Float.parseFloat(rgb[2])*255);
            } else if (rgb.length == 4) {
                return javafx.scene.paint.Color.rgb((int)Float.parseFloat(rgb[0])*255,(int)Float.parseFloat(rgb[1])*255, (int)Float.parseFloat(rgb[2])*255, Float.parseFloat(rgb[3]));
            }
            return javafx.scene.paint.Color.rgb(0,0,255,1); // default
        }
    };

    public static StringConverter<java.awt.Color> ColorAWTConverter = new StringConverter<java.awt.Color>() {

        @Override
        public String toString(java.awt.Color color) {
            return String.format("%s,%s,%s,%s",(float)color.getRed()/255,(float)color.getGreen()/255,(float)color.getBlue()/255,(float)color.getAlpha()/255);
        }

        @Override
        public java.awt.Color fromString(String color) {
            try {
                String[] rgb = color.split(",");
                if (rgb.length == 3) {
                    return new java.awt.Color(Float.parseFloat(rgb[0]), Float.parseFloat(rgb[1]), Float.parseFloat(rgb[2]));
                } else if (rgb.length == 4) {
                    return new java.awt.Color(Float.parseFloat(rgb[0]), Float.parseFloat(rgb[1]), Float.parseFloat(rgb[2]), Float.parseFloat(rgb[3]));
                }
            } catch (IllegalArgumentException ille){
                ille.printStackTrace();
                return new java.awt.Color(0.0f, 0.0f,0.1f, 0.1f); // default
            }
            return new java.awt.Color(0.0f, 0.0f,0.1f, 0.1f); // default
        }
    };


    public static StringConverter<ShapeType> ShapeTypeConverter = new StringConverter<ShapeType>() {
        @Override
        public String toString(ShapeType object) {
            return object.toString();
        }

        @Override
        public ShapeType fromString(String string) {
            string = string.toUpperCase().replace("\\s","");
            return ShapeType.valueOf(string);
        }
    };

    public static StringConverter<StrokeType> StrokeTypeConverter = new StringConverter<StrokeType>() {
        @Override
        public String toString(StrokeType object) {
            if(object==null){
                return null; // Don't know why object is null at start /TODO
            }
            return object.toString();
        }

        @Override
        public StrokeType fromString(String string) {
            string = string.toUpperCase().replace("\\s","");
            return StrokeType.valueOf(string);
        }
    };

    public static StringConverter<Boolean> BooleanypeConverter = new StringConverter<Boolean>() {
        @Override
        public String toString(Boolean object) {
            return object.toString();
        }

        @Override
        public Boolean fromString(String string) {
            return Boolean.valueOf(string);
        }
    };

    public static StringConverter<ChartType> ChartTypeConverter = new StringConverter<ChartType>() {
        @Override
        public String toString(ChartType object) {
            return object.toString();
        }

        @Override
        public ChartType fromString(String string) {
            return ChartType.valueOf(string);
        }
    };
}
