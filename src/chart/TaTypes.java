/*
 The MIT License (MIT)

 Copyright (c) 2017 Wimmer, Simon-Justus

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package chart;

import javax.swing.*;

public class TaTypes {


    public enum categories{
        DEFAULT(new JMenu("(default)"), 0),
        BOLLINGER(new JMenu("Bollinger"), 1 ),
        CANDELS(new JMenu("Candels"), 2),
        HELPERS(new JMenu("Helpers"), 3),
        ICHIMOKU(new JMenu("Ichimoku"), 4),
        KELTNER(new JMenu("Keltner"), 5),
        STATISTICS(new JMenu("Statistics"), 6),
        VOLUME(new JMenu("Volume"), 7);


        private JMenu item;
        private int id;

        categories(JMenu item, int id){
            this.item = item;
            this.id = id;
        }

        public JMenu getMenueElement(){
            return item;
        }

        public int getId(){
            return id;
        }
    }
}
