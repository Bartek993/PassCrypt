package org.example.passwordmanager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.ResourceBundle;


public class MasterPasswordLoginController implements Initializable {

    @FXML
    private AnchorPane leftPane;
    @FXML
    private AnchorPane rightPane;
    @FXML
    private PasswordField loginTextFieldHidden;
    @FXML
    private TextField loginTextFieldShown;
    @FXML
    private CheckBox showPasswordCheckbox;
    @FXML
    private Label exitButton;
    @FXML
    private Label minimizeButton;

    private int loginAttempts = 3;

    public void toggleVisiblePassword(ActionEvent event)
    {
        if (showPasswordCheckbox.isSelected())
        {
            loginTextFieldShown.setText(loginTextFieldHidden.getText());
            loginTextFieldShown.setVisible(true);
            loginTextFieldHidden.setVisible(false);
            return;
        }
        loginTextFieldHidden.setText(loginTextFieldShown.getText());
        loginTextFieldHidden.setVisible(true);
        loginTextFieldShown.setVisible(false);
    }

    public void signIn(ActionEvent event) throws Exception {
        if ((showPasswordCheckbox.isSelected()? loginTextFieldShown.getText(): loginTextFieldHidden.getText()).isEmpty())
        {
            loginAttempts--;
            if (loginAttempts > 0)
            {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Error: Master Password Required");
                alert.setHeaderText(null);
                alert.setContentText("The Password Field is empty, you have " + loginAttempts + " attempts remaining, please try again.");
                alert.showAndWait();
            }
        }
        else
        {
            String masterPassword = showPasswordCheckbox.isSelected()?
                    loginTextFieldShown.getText(): loginTextFieldHidden.getText();
            loginTextFieldHidden.clear();
            loginTextFieldShown.clear();

            if (confirmMasterPassword(masterPassword)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Master Password Confirmed");
                alert.setHeaderText(null);
                alert.setContentText("Master Password Confirmed. Entering the Password Manager");
                JsonReadWrite.AppSession.setMasterPassword(masterPassword);
                SceneManager.switchScene("passwordManagerDashboard.fxml");

            }
            else {
                loginAttempts--;
                if (loginAttempts > 0)
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Invalid Master Password");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid Master Password, you have "+ loginAttempts+" attempts remaining, please try again.");
                    alert.showAndWait();
                }
            }
        }
        if (loginAttempts == 0)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Master Password");
            alert.setHeaderText(null);
            alert.setContentText("Invalid Master Password, you have ran out of attempts, closing the application.");
            alert.showAndWait();
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
        }
    }
    public static Boolean confirmMasterPassword(String masterPassword) throws Exception {
        boolean confirmation = false;
        Map<String, String> metadata = JsonReadWrite.getMasterKeyHashAndSalt();
        String salt_string = metadata.get("salt");
        byte[] salt = Base64.getDecoder().decode(salt_string);
        SecretKeySpec secretKeySpec = Encryption.getAESKeyFromPassword(masterPassword, salt);
        String hashedKey = Encryption.hashKey(Base64.getEncoder().encodeToString(secretKeySpec.getEncoded()));
        if (hashedKey.equals(metadata.get("master_key")))
        {
            confirmation = true;
        }

        return confirmation;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.toggleVisiblePassword(null);

        new SceneManager.windowDragger().makeDraggable(leftPane);
        new SceneManager.windowDragger().makeDraggable(rightPane);

        SceneManager.checkIfWindowClosed(exitButton);
        SceneManager.checkIfWindowMinimized(minimizeButton);

    }
}
