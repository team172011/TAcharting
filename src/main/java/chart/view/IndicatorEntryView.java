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

package chart.view;

import chart.TaChartIndicatorBox;
import chart.TaPropertiesManager;
import chart.types.IndicatorParameters;

import javax.swing.*;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class IndicatorEntryView {

    private JButton btn_add;
    private JButton btn_save;
    private JPanel rootPanel;
    private JPanel settingsPanel;
    private JLabel lbl_title;
    private JButton btn_duplicate;

    private TaChartIndicatorBox indicatorBox;
    private TaPropertiesManager propertiesManager;

    /**
     * Uses a gui form to create an entry for a specific indicator
     * @param key the key of the indicator
     * @param indicatorBox a TaChartIndicatorBox instance
     */
    public IndicatorEntryView(String key, TaChartMenuBar menuBar, TaChartIndicatorBox indicatorBox){
        try {
            this.indicatorBox = indicatorBox;
            this.propertiesManager = indicatorBox.getPropertiesManager();
            String[] keyPair = key.split("_");
            lbl_title.setText(String.format("<html>   %s (%s)<br><font size=\"2\">   %s</font></html>", keyPair[0], keyPair[1], propertiesManager.getDescription(key)));
            Map mapNameParameter = propertiesManager.getParametersFor(key);
            Iterator<Map.Entry> it = mapNameParameter.entrySet().iterator();
            settingsPanel.setLayout(new GridLayout(mapNameParameter.size(),2));
            SaveButtonActionListener saveButtonActionListener = new SaveButtonActionListener(key);
            while(it.hasNext()){
                Map.Entry entry = it.next();
                String parameterName = (String) entry.getKey();
                String parameterValue = (String) entry.getValue();
                String parameterType = propertiesManager.getParameterType(key, parameterName);
                settingsPanel.add(new JLabel(parameterName));
                JComponent valueSetter = IndicatorParameters.getComponent(parameterType, parameterValue);
                valueSetter.setName(parameterName);

                saveButtonActionListener.addEntry(valueSetter);
                settingsPanel.add(valueSetter);
            }

            btn_save.addActionListener(saveButtonActionListener);

            AddButtonActionListener addButtonActionListener = new AddButtonActionListener(key);
            btn_add.addActionListener(addButtonActionListener);

            btn_duplicate.addActionListener(new DuplicateButtonActionListener(key));
        } catch (XPathException xe){
            xe.printStackTrace();
            lbl_title.setText("Could not load from xml");
        }
    }

    public JComponent getRootPanel(){
        return this.rootPanel;
    }


    abstract class ButtonActionListener implements ActionListener{

        private String key;
        private java.util.List<JComponent> componentList;

        public ButtonActionListener(String key){
            this.key = key;
            componentList = new ArrayList<>();
        }

        public void addEntry(JComponent component){
            this.componentList.add(component);
        }

        protected java.util.List<JComponent> getComponentList(){
            return this.componentList;
        }

        protected String getKey(){
            return this.key;
        }

        protected String getID(){
            return this.key.split("_")[1];
        }

        protected String getIdentifier(){
            return this.key.split("_")[0];
        }
    }

    class SaveButtonActionListener extends ButtonActionListener{

        public SaveButtonActionListener(String key) {
            super(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for(JComponent component: getComponentList()){
                try {
                    if (component instanceof JComboBox) {
                        propertiesManager.setParameter(getKey(), component.getName(), ((JComboBox) component).getSelectedItem().toString());
                    }
                    if (component instanceof JTextField) {
                        propertiesManager.setParameter(getKey(), component.getName(), ((JTextField) component).getText().toString());
                    }
                    if (component instanceof JSpinner) {
                        propertiesManager.setParameter(getKey(), component.getName(), ((JSpinner) component).getValue().toString());
                    }
                    if (component instanceof JCheckBox) {
                        String value = "TRUE";
                        if (!((JCheckBox) component).isSelected()) {
                            value = "FALSE";
                        }
                        propertiesManager.setParameter(getKey(), component.getName(), value);
                    }

                } catch (Exception ed) {
                    JOptionPane.showMessageDialog(component, ed.getMessage());
                    ed.printStackTrace();
                }
                JOptionPane.showMessageDialog(btn_add, "Saved!");
            }
        }
    }

    class AddButtonActionListener extends ButtonActionListener{

        public AddButtonActionListener(String key) {
            super(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                indicatorBox.reloadIndicator(getKey());
                JOptionPane.showMessageDialog(btn_add,String.format("Added as %s [%s]",getIdentifier(),getID()));
            } catch (XPathException xpe){
                JOptionPane.showMessageDialog(btn_add,"Could not add indicator: "+xpe.toString());
            }

        }
    }

    class DuplicateButtonActionListener extends ButtonActionListener{

        public DuplicateButtonActionListener(String key) {
            super(key);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                propertiesManager.duplicate(getKey());
                JOptionPane.showMessageDialog(btn_save, "Duplicated! Please close and open again to see/add the duplicated Indicator");
            } catch (XPathException xpe){
                JOptionPane.showMessageDialog(btn_save, "Could not duplicate indicator: "+xpe.toString());
            } catch (TransformerException e1) {
                JOptionPane.showMessageDialog(btn_save, e1.getMessage());
            }
        }
    }
}
