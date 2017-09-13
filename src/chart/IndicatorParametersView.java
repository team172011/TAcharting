package chart;

import javax.swing.*;
import java.util.Properties;

public class IndicatorParametersView extends JFrame {

    private TaChartIndicatorBox box;
    private Properties properties;

    public IndicatorParametersView(TaChartIndicatorBox box){
        super("Parameter Settings");
        this.box = box;
    }
}
