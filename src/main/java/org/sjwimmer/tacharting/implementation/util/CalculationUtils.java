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
package org.sjwimmer.tacharting.implementation.util;

import java.util.Currency;

import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.ta4j.core.num.Num;

public class CalculationUtils {


    public static String roundToString(Double value, int d){
        return String.format("%."+d+"f",value);
    }

    public static Num integerToCurrencyValue(int value, Currency currency){
        int fractionDigits = currency.getDefaultFractionDigits();
        double base = Math.pow(10,fractionDigits);

        return Parameter.numFunction.apply(value/base);
    }

    public static Num stringToCurrencyValue(String stringValue, Currency currency){
        String value = stringValue.replaceAll("\\s","");
        return integerToCurrencyValue(Integer.parseInt(value), currency);
    }

    public static int currencyValueToInteger(Num value, Currency currency){
        int fractionDigits = currency.getDefaultFractionDigits();
        int base = (int)Math.pow(10,fractionDigits);

        return (int)value.doubleValue()*base;
    }
}
