package org.sjwimmer.tacharting.chart;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.sjwimmer.tacharting.chart.parameters.Parameter;
import org.sjwimmer.tacharting.chart.utils.InitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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
