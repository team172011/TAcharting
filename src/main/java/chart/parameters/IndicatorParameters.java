package chart.parameters;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.TextField;

import java.awt.*;
import java.awt.geom.Ellipse2D;


public class IndicatorParameters {

    /**
     * (In progress..)
     * Enum of all available indicators with names
     *
     */
    public enum Indicators{
        BOLLINGER("Bollinger Bands (and Width)"),
        SMA("Simple Moving Average"),
        EMA("Exponential Moving Average");

        private String name;

        Indicators(String name){
            this.name = name;
        }
    }

    public enum TaBoolean{
        TRUE,
        FALSE;

        static Control getComponent(String value){
            TaBoolean choosen = valueOf(value);
            CheckBox checkBox = new CheckBox("Yes/No");
            checkBox.setSelected(choosen.toBoolean());
            return checkBox;
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

        static Control getComponent(String value){
            int choosen = Integer.parseInt(value);
            return new Spinner(0,1000,choosen);
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

        private java.awt.Paint paint;

        TaColor(Paint paint){
            this.paint = paint;
        }

        static Control getComponent(String value){
            TaColor choosen = TaColor.valueOf(value);
            ComboBox<TaColor> cBox = new ComboBox<TaColor>(FXCollections.observableArrayList(TaColor.values()));
            cBox.getSelectionModel().select(choosen);
            ColorPicker colorPicker = new ColorPicker();
            return colorPicker;
        }

        static String getStringDescription(){
            return "TaColor";
        }

        public Paint getPaint(){
            return paint;
        }

    }

    /**
     * The available categories in the menu
     */
    public enum TaCategory {
        CUSTOM(8),
        STRATEGY(9),
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

        public int getId() {
            return id;
        }

        static Control getComponent(String value){
            ComboBox<TaCategory> cBox = new ComboBox<TaCategory>(FXCollections.observableArrayList(TaCategory.values()));
            cBox.getSelectionModel().select(TaCategory.valueOf(value));
            return cBox;
        }

        static String getStringDescription(){
            return "TaCategory";
        }

    }

    public enum TaChartType {
        OVERLAY,
        SUBCHART;

        static Control getComponent(String value){
            ComboBox<TaChartType> cBox = new ComboBox<TaChartType>(FXCollections.observableArrayList(TaChartType.values()));
            cBox.getSelectionModel().select(TaChartType.valueOf(value));
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

        static Control getComponent(String value){
            ComboBox<TaShape> cBox = new ComboBox<TaShape>(FXCollections.observableArrayList(values()));
            cBox.getSelectionModel().select(TaShape.valueOf(value));
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


        static Control getComponent(String value){
            ComboBox<TaStroke> cBox = new ComboBox<TaStroke>(FXCollections.observableArrayList(values()));
            cBox.getSelectionModel().select(TaStroke.valueOf(value));
            return cBox;
        }

        static String getStringDescription(){
            return "TaStroke";
        }
    }

    /**
     * returns a Labeled to view and/or change the value of parameter via gui
     * @param parameterType the type of the parameter from xml
     * @return Labeled to view and/or change the value of parameter via gui
     */
    public static Control getComponent(String parameterType, String paramValue){
        Control control = new TextField(paramValue);

        if(parameterType.equals(TaColor.getStringDescription())){
            control = TaColor.getComponent(paramValue);
        }

        if(parameterType.equals(INTEGER.getStringDescription())){
            control = INTEGER.getComponent(paramValue);
        }

        if(parameterType.equals(TaShape.getStringDescription())){
            control = TaShape.getComponent(paramValue);
        }

        if(parameterType.equals(TaStroke.getStringDescription())){
            control = TaStroke.getComponent(paramValue);
        }

        if(parameterType.equals(TaCategory.getStringDescription())){
            control = TaCategory.getComponent(paramValue);
        }

        if(parameterType.equals(TaChartType.getStringDescription())){
            control = TaChartType.getComponent(paramValue);
        }
        if(parameterType.equals(TaBoolean.getStringDescription())){
            control = TaBoolean.getComponent(paramValue);
        }

        return control;
    }
}
