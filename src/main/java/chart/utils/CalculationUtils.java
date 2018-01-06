package chart.utils;

public class CalculationUtils {


    public static String roundToString(Double value, int d){
        return String.format("%."+d+"f",value);
    }
}
