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
import java.awt.event.MouseEvent;

/** Custom CheckBox class, stays open by clicking on*/
public class TaCheckBoxItem extends JCheckBoxMenuItem {

    private static final long serialVersionUID = 1L;

    public TaCheckBoxItem() {
    }

    public TaCheckBoxItem(Icon icon) {
        super(icon);
    }

    public TaCheckBoxItem(String text) {
        super(text);
    }

    public TaCheckBoxItem(Action a) {
        super(a);
    }

    public TaCheckBoxItem(String text, Icon icon) {
        super(text, icon);
    }

    public TaCheckBoxItem(String text, boolean b) {
        super(text, b);
    }

    public TaCheckBoxItem(String text, Icon icon, boolean b) {
        super(text, icon, b);
    }

    @Override
    protected void processMouseEvent(MouseEvent evt) {
        if (evt.getID() == MouseEvent.MOUSE_RELEASED && contains(evt.getPoint())) {
            doClick();
            setArmed(true);
        } else {
            super.processMouseEvent(evt);
        }
    }
}
