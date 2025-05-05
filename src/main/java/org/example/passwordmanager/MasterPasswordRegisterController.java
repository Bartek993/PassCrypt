package org.example.passwordmanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class MasterPasswordRegisterController implements Initializable {


    @FXML
    private AnchorPane insidePane;
    @FXML
    private Button importDatabaseButton;
    @FXML
    private Button submitMasterPasswordButton;
    @FXML
    private PasswordField masterPasswordField;
    @FXML
    private TextField masterPasswordFieldShown;
    @FXML
    private CheckBox pass_toggle;
    @FXML
    private Label minimizeButton;
    @FXML
    private Label exitButton;


    public void toggleVisiblePassword(ActionEvent event)
    {
        if (pass_toggle.isSelected())
        {
            masterPasswordFieldShown.setText(masterPasswordField.getText());
            masterPasswordFieldShown.setVisible(true);
            masterPasswordField.setVisible(false);
            return;
        }
        masterPasswordField.setText(masterPasswordFieldShown.getText());
        masterPasswordField.setVisible(true);
        masterPasswordFieldShown.setVisible(false);
    }

    public void submitMasterPassword(ActionEvent actionEvent) throws Exception {

        if ((pass_toggle.isSelected()? masterPasswordFieldShown.getText(): masterPasswordField.getText()).isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Error: Master Password Required");
            alert.setHeaderText(null);
            alert.setContentText("The Password Field is empty, please try again.");
            alert.showAndWait();
            return;
        }
        String masterPassword = pass_toggle.isSelected()?
                masterPasswordFieldShown.getText(): masterPasswordField.getText();
        masterPasswordField.clear();
        masterPasswordFieldShown.clear();
        if (validatePassword(masterPassword))
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Master Password Created");
            alert.setHeaderText(null);
            alert.setContentText("Master Password has been created successfully.");
            alert.showAndWait();
            JsonReadWrite.createJSONFile(masterPassword);
            SceneManager.switchScene("enterMasterPassword.fxml");
        }
        else
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error: Invalid Master Password");
            alert.setHeaderText(null);
            alert.setContentText("Invalid Master Password, please try again.");
            alert.showAndWait();
        }
    }

    public boolean validatePassword(String masterPassword) {
        if (masterPassword.length() < 12) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char ch : masterPassword.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isDigit(ch)) hasDigit = true;
            else if (!Character.isLetterOrDigit(ch)) hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    @FXML
    public void openFileBrowser() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Password Manager Database", "*.json")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if(selectedFile != null)
        {
            if (JsonReadWrite.validateFile(selectedFile))
            {

                File targetDir = new File("src/main/resources/org/example/passwordmanager");

                File targetFile = new File(targetDir, "passwordmanager.json");
                Files.move(selectedFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Database imported");
                alert.setHeaderText(null);
                alert.setContentText("Database file has been imported successfully.");
                alert.showAndWait();
                SceneManager.switchScene("enterMasterPassword.fxml");
            }
            else
            {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error: Invalid File");
                alert.setHeaderText(null);
                alert.setContentText("This file is invalid for use");
                alert.showAndWait();
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.toggleVisiblePassword(null);

        new SceneManager.windowDragger().makeDraggable(insidePane);

        SceneManager.checkIfWindowClosed(exitButton);
        SceneManager.checkIfWindowMinimized(minimizeButton);
    }
}
