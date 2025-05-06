package org.example.passwordmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.stage.StageStyle;

import java.io.File;

public class StartApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        SceneManager.setStage(stage);


        String fxmlToLoad = checkIfManagerExists() ? "enterMasterPassword.fxml" : "createMasterPassword.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader(StartApplication.class.getResource(fxmlToLoad));
        Scene scene = new Scene(fxmlLoader.load());

        stage.initStyle(StageStyle.TRANSPARENT);
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("PassCrypt");

        stage.setScene(scene);
        stage.show();
    }
    private boolean checkIfManagerExists() {
        File file = new File("src/main/resources/org/example/passwordmanager/passwordmanager.json");
        return file.exists();
    }

    public static void main(String[] args) {
        launch();
    }
}