package org.example.passwordmanager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class SceneManager {
    private static Stage stage;

    public static class windowDragger {
        private double x, y;

        public void makeDraggable(AnchorPane pane){
            pane.setOnMousePressed(mouseEvent -> {
                x = mouseEvent.getSceneX();
                y = mouseEvent.getSceneY();
            });

            pane.setOnMouseDragged(mouseEvent -> {
                Stage stage = (Stage) pane.getScene().getWindow();
                stage.setX(mouseEvent.getScreenX() - x);
                stage.setY(mouseEvent.getScreenY() - y);
            });
        }
    }

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void switchScene(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        scene.setFill(Color.TRANSPARENT);


        stage.setScene(scene);
        stage.show();
    }
    public static void checkIfWindowClosed(Label exitButton)
    {
        exitButton.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to exit?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.OK)
            {
                Stage stage = (Stage) exitButton.getScene().getWindow();
                stage.close();
            }
        });
    }

    public static void checkIfWindowMinimized(Label minimizeButton) {
        minimizeButton.setOnMouseClicked(e -> {
            Stage stage = (Stage) minimizeButton.getScene().getWindow();
            stage.setIconified(true);
        });
    }
}

