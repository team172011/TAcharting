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
package org.sjwimmer.tacharting.chart.model.types;

import org.sjwimmer.tacharting.chart.model.ChartIndicator;

/**
 * Enum for the types of parameters that could be needed to create an chart indicator instance
 * see {@link ChartIndicator ChartIndicator}
 */

public enum IndicatorParameterType{
    BOOLEAN("Boolean"),
    INTEGER("Integer"),
    DOUBLE("Double"),
    STRING("String"),
    SHAPE("Shape"),
    STROKE("Stroke"),
    COLOR("Color"),
    SERIES("Series"),
    INDICATOR("Indicator"),
    CHARTTYPE("ChartType");

    public final String name;

    IndicatorParameterType(String name){
        this.name = name;
    }
}
