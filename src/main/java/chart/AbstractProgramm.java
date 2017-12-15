package chart;

import example.Loader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ta4j.core.TimeSeries;

import java.net.URL;

public abstract class AbstractProgramm extends Application {

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

    abstract public ChartIndicatorBox createIndicatorBox(TimeSeries series);


}
