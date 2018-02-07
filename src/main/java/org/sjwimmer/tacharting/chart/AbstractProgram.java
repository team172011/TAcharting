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
 WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.sjwimmer.tacharting.chart;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.sjwimmer.tacharting.chart.controller.ChartController;
import org.sjwimmer.tacharting.chart.model.BaseIndicatorBox;
import org.ta4j.core.Indicator;
import org.ta4j.core.Strategy;
import org.ta4j.core.TradingRecord;

import java.net.URL;

public abstract class AbstractProgram extends Application {

    /**
     * Entry point for the JavaFX Application
     * @param primaryStage the primary stage (handed-down from JavaFX thread)
     * @throws Exception exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icons/logo.png")));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/charting-root.fxml"));

        Parent root = fxmlLoader.load();
        ChartController controller = fxmlLoader.<ChartController>getController();
        final BaseIndicatorBox indicatorBox = createIndicatorBox();
        controller.setIndicatorBox(indicatorBox);
        Scene rootScene = new Scene(root);
        URL stylesheetPath = getClass().getClassLoader().getResource(("fxml/charting-root.css"));
        if(stylesheetPath != null){
            rootScene.getStylesheets().add(stylesheetPath.toExternalForm());
        }
        primaryStage.setScene(rootScene);
        primaryStage.setTitle("Ta4j-Charting");
        primaryStage.show();
    }

    /**
     * This method can be overwritten to get custom {@link BaseIndicatorBox} with custom {@link Indicator indicators},
     * {@link Strategy strategies} and {@link TradingRecord}
     * @return a {@link BaseIndicatorBox} for the Chart that is used in the {@link #start(Stage) start(Stage) function}
     * colorOf this class
     */
    abstract public BaseIndicatorBox createIndicatorBox();


}
