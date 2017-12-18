package chart;

import example.Loader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ta4j.core.Indicator;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TradingRecord;

import java.net.URL;

public abstract class AbstractProgramm extends Application {

    /**
     * Entry point for the JavaFX Application start
     * @param primaryStage the primary stage (handed-down from JavaFX thread)
     * @throws Exception exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        URL file = cl.getResource("fb_daily.csv");
        TimeSeries series = Loader.getDailyTimeSeries(file, "fb");

        FXMLLoader fxmlLoader = new FXMLLoader(cl.getResource("fxml/root.fxml"));

        Parent root = fxmlLoader.load();
        RootController rootController = fxmlLoader.<RootController>getController();
        rootController.setIndicatorBox(createIndicatorBox(series));

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle(series.getName());
        primaryStage.show();
    }

    /**
     * This method can be overwritten to get custom {@link ChartIndicatorBox} with custom {@link Indicator indicators},
     * {@link Strategy strategies} and {@link TradingRecord}
     * @param series The corresponding {@link TimeSeries} for the chart
     * @return a {@link ChartIndicatorBox} for the Chart that is used in the {@link #start(Stage) start(Stage) function}
     * of this class
     */
    abstract public ChartIndicatorBox createIndicatorBox(TimeSeries series);


}
