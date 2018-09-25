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
package org.sjwimmer.tacharting.program;

import java.io.File;
import java.io.IOException;

import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.implementation.util.InitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Preloader Application for initial tasks
 */
public class Preloader extends javafx.application.Preloader{

    private final Logger log = LoggerFactory.getLogger(Preloader.class);

    private Stage stage;
    private ProgressBar bar;
    private Label lblProgress;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        stage.setScene(buildScene());
        stage.show();
        // create api properties file if not exists
        File file = new File(Parameter.API_PROPERTIES_FILE);
        try{
            file.getParentFile().mkdirs(); //properties folder
            if(file.createNewFile()){// api.properties file
                log.debug("Created new api properties file");
            } else {
                log.debug("Found api properties file ");
            }
        } catch (IOException ioe){
            log.error("Could not create api properties file {}",Parameter.API_PROPERTIES_FILE);
        }
        if(new File(Parameter.PROGRAM_FOLDER).mkdirs()){
            log.debug("Created new Program folder in user home");
        }

        // export the indicators properties file if not exists
        File indicatorParametersFile = new File(Parameter.USER_INDICATOR_PROPERTIES_FILE);
        if(indicatorParametersFile.exists()){
            log.info("Found user indicator file");
        } else {
            try{
                InitUtils.exportResource(Parameter.INDICATOR_PROPERTIES_FILE, Parameter.USER_INDICATOR_PROPERTIES_FILE);
                log.info("No user indicator file found. Default file exported");
            } catch (Exception e){
                e.printStackTrace();
                log.error("Could not export default properties file");
            }
        }

        // create the database directory if not exists
        File dataBaseFile = new File(Parameter.DATABASE_PATH);
        if(dataBaseFile.mkdirs()){
            log.debug("Created new directory for database: {} ", dataBaseFile.getPath());
        } else {
            log.debug("Found database directory");
        }
    }

    private Scene buildScene(){
        lblProgress = new Label("Lade: ");
        bar = new ProgressBar(-1d);
        VBox vBox = new VBox(5,lblProgress,bar);
        return new Scene(vBox,300,150);
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        bar.setProgress(pn.getProgress());
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }

}
