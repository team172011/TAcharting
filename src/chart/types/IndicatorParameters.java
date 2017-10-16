package chart.types;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;


public class IndicatorParameters {

    public enum TaBoolean{
        TRUE,
        FALSE;

        static JComponent getComponent(String value){
            TaBoolean choosen = valueOf(value);
            return new JCheckBox("Yes/No",choosen.toBoolean());
        }

        static String getStringDescription(){
            return "TaBoolean";
        }

        public boolean toBoolean(){
            switch (this){
                case TRUE:
                    return true;
                default:
                    return false;
            }
        }
    }

    public enum INTEGER {

        ;

        static JComponent getComponent(String value){
            int choosen = Integer.parseInt(value);
            return new JSpinner(new SpinnerNumberModel(choosen,0,100,1));
        }

        static String getStringDescription(){
            return "INTEGER";
        }
    }


    public enum TaColor{ //TODO: add rgb parameter

        BLUE(Color.BLUE),
        YELLOW(Color.YELLOW),
        RED(Color.RED),
        MAGENTA(Color.MAGENTA),
        GREEN(Color.GREEN);

        private Paint paint;

        TaColor(Paint paint){
            this.paint = paint;
        }

        static JComponent getComponent(String value){
            TaColor choosen = TaColor.valueOf(value);
            JComboBox<TaColor> cBox = new JComboBox<TaColor>(new DefaultComboBoxModel<TaColor>(TaColor.values()));
            cBox.setSelectedItem(choosen);
            return cBox;
        }

        static String getStringDescription(){
            return "TaColor";
        }

        public Paint getPaint(){
            return paint;
        }

    }

    public enum TaCategory {
        DEFAULT(0),
        BOLLINGER(1),
        CANDLES(2),
        HELPERS(3),
        ICHIMOKU(4),
        KELTNER(5),
        STATISTICS(6),
        VOLUME(7);

        private int id;

        TaCategory(int id) {

            this.id = id;
        }

        public JMenu getMenueElement(){
            switch (this){
                case HELPERS:
                    return new JMenu("Helpers");
                case BOLLINGER:
                    return new JMenu("Bollinger");
                case ICHIMOKU:
                    return new JMenu("Ichimoku");
                case CANDLES:
                    return new JMenu("Candels");
                case KELTNER:
                    return new JMenu("Keltner");
                case STATISTICS:
                    return new JMenu("Statistics");
                case VOLUME:
                    return new JMenu("Volume");
                default:
                    return new JMenu("Default");
            }

        }

        public int getId() {
            return id;
        }

        static JComponent getComponent(String value){
            JComboBox<TaCategory> cBox = new JComboBox<TaCategory>(new DefaultComboBoxModel<TaCategory>(TaCategory.values()));
            cBox.setSelectedItem(TaCategory.valueOf(value));
            return cBox;
        }

        static String getStringDescription(){
            return "TaCategory";
        }

    }

    public enum TaChartType {
        OVERLAY,
        SUBCHART;

        static JComponent getComponent(String value){
            JComboBox<TaChartType> cBox = new JComboBox<TaChartType>(new DefaultComboBoxModel<TaChartType>(TaChartType.values()));
            cBox.setSelectedItem(TaChartType.valueOf(value));
            return cBox;
        }

        static String getStringDescription(){
            return "TaChartType";
        }

        public boolean toBoolean(){
            switch (this){
                case OVERLAY:
                    return false;
                default:
                    return true;
            }
        }
    }

    public enum TaShape {
        SMALL_REC(new Rectangle(2, 2)),
        NONE(new Rectangle(1, 1)),
        CIRCLE(new Ellipse2D.Double());

        private Shape shape;

        TaShape(Shape shape) {
            this.shape = shape;
        }

        public Shape getShape() {
            return shape;
        }

        static JComponent getComponent(String value){
            JComboBox<TaShape> cBox = new JComboBox<TaShape>(new DefaultComboBoxModel<TaShape>(TaShape.values()));
            cBox.setSelectedItem(TaShape.valueOf(value));
            return cBox;
        }

        static String getStringDescription(){
            return "TaShape";
        }

    }

    public enum TaStroke {
        SMALL_LINE(new BasicStroke(1f)),
        DOT_DOT_LINE(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 4.0f, 3.0f, 3.0f}, 0.0f)),
        LINE_LINE(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 5.0f, new float[]{10.0f, 5.0f}, 0.0f)),
        DOTS(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6.0f, 4.0f, 3.0f, 3.0f}, 0.0f)),
        BIG_DOTS(new BasicStroke(0.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.50f, new float[]{1.0f, 1.0f}, 0.0f)),
        NONE(new BasicStroke());

        private Stroke stroke;

        TaStroke(Stroke stroke) {
            this.stroke = stroke;
        }

        public Stroke getStroke() {
            return stroke;
        }


        static JComponent getComponent(String value){
            JComboBox<TaStroke> cBox = new JComboBox<TaStroke>(new DefaultComboBoxModel<TaStroke>(TaStroke.values()));
            cBox.setSelectedItem(TaStroke.valueOf(value));
            return cBox;
        }

        static String getStringDescription(){
            return "TaStroke";
        }
    }

    /**
     * returns a JComponent to view and/or change the value of parameter via gui
     * @param parameterType the type of the parameter from xml
     * @return JComponent to view and/or change the value of parameter via gui
     */
    public static JComponent getComponent(String parameterType, String paramValue){
        JComponent component = new JTextField(paramValue);

        if(parameterType.equals(TaColor.getStringDescription())){
            component = TaColor.getComponent(paramValue);
        }

        if(parameterType.equals(INTEGER.getStringDescription())){
            component = INTEGER.getComponent(paramValue);
        }

        if(parameterType.equals(TaShape.getStringDescription())){
            component = TaShape.getComponent(paramValue);
        }

        if(parameterType.equals(TaStroke.getStringDescription())){
            component = TaStroke.getComponent(paramValue);
        }

        if(parameterType.equals(TaCategory.getStringDescription())){
            component = TaCategory.getComponent(paramValue);
        }

        if(parameterType.equals(TaChartType.getStringDescription())){
            component = TaChartType.getComponent(paramValue);
        }
        if(parameterType.equals(TaBoolean.getStringDescription())){
            component = TaBoolean.getComponent(paramValue);
        }

        return component;
    }
}
