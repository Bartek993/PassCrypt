package org.example.passwordmanager;

import com.nulabinc.zxcvbn.Strength;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.nulabinc.zxcvbn.Zxcvbn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PasswordStrengthController implements Initializable {

    @FXML
    private TextField passwordField;
    @FXML
    private Label strengthLabel;
    @FXML
    private Text textWarning;
    @FXML
    private Text textImprovements;
    @FXML
    private AnchorPane insidePane;
    @FXML
    private Label minimizeButton;
    @FXML
    private Label exitButton;

    private final Zxcvbn zxcvbn = new Zxcvbn();

    public void goBack() throws IOException {
        SceneManager.switchScene("passwordManagerDashboard.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        SceneManager.checkIfWindowClosed(exitButton);
        SceneManager.checkIfWindowMinimized(minimizeButton);

        new SceneManager.windowDragger().makeDraggable(insidePane);

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            Strength strength = zxcvbn.measure(newValue);

            if (!passwordField.getText().isEmpty())
            {
                strengthLabel.setVisible(true);
                strengthLabel.setText("Password Strength: " + strength.getScore() + "/4");

                if (!strength.getFeedback().getWarning().isEmpty())
                {
                    textWarning.setVisible(true);
                    String warning = strength.getFeedback().getWarning();
                    textWarning.setText("Warning: " + warning);
                }
                else {
                    textWarning.setVisible(false);
                }
                if (!strength.getFeedback().getSuggestions().isEmpty()) {
                    textImprovements.setVisible(true);
                    String feedback = strength.getFeedback().getSuggestions().toString();
                    textImprovements.setText("Improvement: " + feedback);
                } else {
                    textImprovements.setVisible(false);
                }
            }
            else
            {
                textWarning.setVisible(false);
                textImprovements.setVisible(false);
                strengthLabel.setVisible(false);
            }
        });

    }
}
