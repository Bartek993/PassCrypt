package org.example.passwordmanager;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CreatePasswordController implements Initializable {

    @FXML
    private TextField websiteField;
    @FXML
    private TextField userField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField notesField;
    @FXML
    private TextField urlField;
    @FXML
    private AnchorPane insidePane;
    @FXML
    private Label minimizeButton;
    @FXML
    private Label exitButton;

    public static String website = "";
    public static String username = "";
    public static String password = "";
    public static String website_url = "";
    public static String notes = "";


    public void clearFields()
    {
        website = "";
        username = "";
        password = "";
        website_url = "";
        notes = "";
    }

    public void createPassword() throws IOException {
        if (websiteField.getText().isEmpty() || userField.getText().isEmpty() || passwordField.getText().isEmpty() || urlField.getText().isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error: Incomplete Fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all the required fields");
            alert.showAndWait();
        }
        else
        {
            List<String> passwordsUsed = new ArrayList<>();
            for (JsonReadWrite.Entry entry : JsonReadWrite.getAllEntries()) {
                passwordsUsed.add(entry.getEntry_password());
            }
            if (passwordsUsed.contains(passwordField.getText()))
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error: Duplicate Password");
                alert.setHeaderText(null);
                alert.setContentText("Password already used, please use a new unique password");
                alert.showAndWait();
            } else if (passwordField.getText().length() <= 12) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error: Invalid Password");
                alert.setHeaderText(null);
                alert.setContentText("Please ensure your password is at least 12 characters long");
                alert.showAndWait();
            } else
            {
                JsonReadWrite.createNewEntry(websiteField.getText(),userField.getText(), passwordField.getText(), urlField.getText(), notesField.getText());
                goBack();
            }
        }
    }

    public void goBack() throws IOException {
        clearFields();
        SceneManager.switchScene("passwordManagerDashboard.fxml");
    }

    public void generatePassword() throws IOException {
        website = websiteField.getText();
        username = userField.getText();
        password = passwordField.getText();
        website_url = urlField.getText();
        notes = notesField.getText();
        SceneManager.switchScene("generatePassword.fxml");
    }
    public void showTip()
    {
        String[] passwordTips = {
                "Do not reuse passwords across different accounts.",
                "Use at least 12 characters to create a strong password.",
                "Combine uppercase letters, lowercase letters, numbers, and symbols to make the password more complex to brute force.",
                "Do not include personal information like your name or birthdate.",
                "Avoid using real words or common phrases—attackers can guess them easily.",
                "Use random characters using the password generator or a passphrase made of unrelated words.",
                "Enable two-factor authentication (2FA) when available for added security.",
                "Avoid patterns like '123456', 'password', or repeated characters.",
                "Never write your passwords down in plain text or share them with others.",
                "Use the password generator to create secure passwords effortlessly.",
                "Stronger passwords reduce the risk of being hacked or compromised.",
                "The longer the password, the harder it is to crack—aim for 16+ characters when possible.",
                "Avoid keyboard patterns like 'qwerty' or predictable substitutions like 'pa$$w0rd'.",
                "Each account should have its own unique password to prevent credential stuffing.",
                "Consider using a passphrase that's easy to remember but hard to guess.",
                "Weak passwords are one of the most common causes of security breaches.",
        };

        Random random = new Random();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Creation Tip");
        alert.setHeaderText("Password Creation Tip");
        alert.setContentText(passwordTips[random.nextInt(passwordTips.length)]);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        SceneManager.checkIfWindowClosed(exitButton);
        SceneManager.checkIfWindowMinimized(minimizeButton);

        new SceneManager.windowDragger().makeDraggable(insidePane);

        websiteField.setText(website);
        userField.setText(username);
        passwordField.setText(password);
        urlField.setText(website_url);
        notesField.setText(notes);
    }
}
