package org.example.passwordmanager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GeneratePasswordController implements Initializable {

    @FXML
    private Label passwordLengthLabel;
    @FXML
    private Slider passwordLengthSlider;
    @FXML
    private CheckBox uppercaseCheckbox;
    @FXML
    private CheckBox lowercaseCheckbox;
    @FXML
    private CheckBox digitsCheckbox;
    @FXML
    private CheckBox specialCharactersCheckbox;
    @FXML
    private Label passwordLabel;
    @FXML
    private AnchorPane insidePane;
    @FXML
    private Button copyButton;
    @FXML
    private Button useButton;
    @FXML
    private Label minimizeButton;
    @FXML
    private Label exitButton;

    private String generatedPassword;

    private static String previousSection = "";

    private int passwordLength;

    public static void setPreviousSection(String section)
    {
        previousSection = section;
    }


    public void goBack() throws IOException {
        if (previousSection.equals("passwordManager"))
        {
            previousSection = "";
            SceneManager.switchScene("passwordManagerDashboard.fxml");
        }
        else if (previousSection.equals("editEntry"))
        {
            previousSection = "";
            EditEntryController.generator_accessed = true;
            SceneManager.switchScene("editPasswordEntry.fxml");
        }
        else
        {
            previousSection = "";
            SceneManager.switchScene("createPasswordEntry.fxml");
        }
    }

    public void generatePassword() throws IOException {
        if (uppercaseCheckbox.isSelected() || lowercaseCheckbox.isSelected() || digitsCheckbox.isSelected() || specialCharactersCheckbox.isSelected()) {
            String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String lowercase = "abcdefghijklmnopqrstuvwxyz";
            String digits = "0123456789";
            String special = "!@#$%^&*()-_=+[]{}|;:,.<>?";

            StringBuilder password = new StringBuilder();
            StringBuilder allowedChars = new StringBuilder();
            List<Character> guaranteedChars = new ArrayList<>();

            if (uppercaseCheckbox.isSelected()) {
                guaranteedChars.add(uppercase.charAt((int) (Math.random() * uppercase.length())));
                allowedChars.append(uppercase);
            }
            if (lowercaseCheckbox.isSelected()) {
                guaranteedChars.add(lowercase.charAt((int) (Math.random() * lowercase.length())));
                allowedChars.append(lowercase);
            }
            if (digitsCheckbox.isSelected()) {
                guaranteedChars.add(digits.charAt((int) (Math.random() * digits.length())));
                allowedChars.append(digits);
            }
            if (specialCharactersCheckbox.isSelected()) {
                guaranteedChars.add(special.charAt((int) (Math.random() * special.length())));
                allowedChars.append(special);
            }
            Random random = new Random();
            while (guaranteedChars.size() < passwordLength) {
                guaranteedChars.add(allowedChars.charAt(random.nextInt(allowedChars.length())));
            }
            Collections.shuffle(guaranteedChars);
            for (char c : guaranteedChars) {
                password.append(c);
            }
            generatedPassword = password.toString();
            passwordLabel.setText(generatedPassword);
            copyButton.setDisable(false);
            useButton.setDisable(false);
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select select a character set");
            alert.showAndWait();
        }
    }

    public void copyPassword()
    {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(generatedPassword);
        clipboard.setContent(content);
    }
    public void usePassword() throws IOException {

        if (previousSection.equals("editEntry"))
        {
            EditEntryController.password = generatedPassword;
            EditEntryController.generator_accessed = true;
            SceneManager.switchScene("editPasswordEntry.fxml");
        }
        else
        {
            CreatePasswordController.password = generatedPassword;
            SceneManager.switchScene("createPasswordEntry.fxml");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        SceneManager.checkIfWindowClosed(exitButton);

        SceneManager.checkIfWindowMinimized(minimizeButton);

        new SceneManager.windowDragger().makeDraggable(insidePane);

        if (previousSection.equals("passwordManager"))
        {
         useButton.setVisible(false);
        }
        copyButton.setDisable(true);
        useButton.setDisable(true);

        uppercaseCheckbox.setSelected(true);
        lowercaseCheckbox.setSelected(true);
        digitsCheckbox.setSelected(true);
        specialCharactersCheckbox.setSelected(true);

        passwordLength = (int) passwordLengthSlider.getValue();
        passwordLengthLabel.setText("Password Length: " + String.valueOf(passwordLength));

        passwordLengthSlider.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                passwordLength = (int) passwordLengthSlider.getValue();
                passwordLengthLabel.setText("Password Length: " + String.valueOf(passwordLength));
            }
        });
    }
}
