package org.example.passwordmanager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class EditEntryController implements Initializable {
    @FXML
    private TextField websiteField;
    @FXML
    private TextField userField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField urlField;
    @FXML
    private TextField notesField;
    @FXML
    private Label minimizeButton;
    @FXML
    private Label exitButton;
    @FXML
    private AnchorPane insidePane;

    public static boolean generator_accessed;

    private static JsonReadWrite.Entry entry;

    public static String website = "";
    public static String username = "";
    public static String password = "";
    public static String website_url = "";
    public static String notes = "";

    public static void setEntry(JsonReadWrite.Entry passwordEntry) {
        entry = passwordEntry;
    }

    public void clearFields() {
        websiteField.clear();
        userField.clear();
        passwordField.clear();
        urlField.clear();
        notesField.clear();
        entry = null;
    }

    public void confirmChange() {
        ArrayList<JsonReadWrite.Entry> updatedEntries = JsonReadWrite.getAllEntries();
        if (websiteField.getText().isEmpty() || userField.getText().isEmpty() ||
                passwordField.getText().isEmpty() || urlField.getText().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error: Incomplete Fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all the required fields");
            alert.showAndWait();
        } else {
            List<String> passwordsUsed = new ArrayList<>();
            for (JsonReadWrite.Entry e : updatedEntries) {
                if (!(entry.getEntry_website().equals(e.getEntry_website()) &&
                        entry.getEntry_username().equals(e.getEntry_username()) &&
                        entry.getEntry_password().equals(e.getEntry_password()) &&
                        entry.getEntry_url().equals(e.getEntry_url()) &&
                        entry.getEntry_notes().equals(e.getEntry_notes()) &&
                        entry.getIV().equals(e.getIV()))) {
                    passwordsUsed.add(e.getEntry_password());
                }
            }
            if (passwordsUsed.contains(passwordField.getText())) {
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
            } else {
                for (JsonReadWrite.Entry e : updatedEntries) {
                    if ((entry.getEntry_website().equals(e.getEntry_website()) &&
                            entry.getEntry_username().equals(e.getEntry_username()) &&
                            entry.getEntry_password().equals(e.getEntry_password()) &&
                            entry.getEntry_url().equals(e.getEntry_url()) &&
                            entry.getEntry_notes().equals(e.getEntry_notes()) &&
                            entry.getIV().equals(e.getIV()))) {
                        e.setEntry_website(websiteField.getText());
                        e.setEntry_username(userField.getText());
                        e.setEntry_password(passwordField.getText());
                        e.setEntry_url(urlField.getText());
                        e.setEntry_notes(notesField.getText());
                        JsonReadWrite.overwriteFile(updatedEntries);
                        clearFields();
                        goBack();
                        break;
                    }
                }
            }
        }
    }

    public void goBack() {
        try {
            clearFields();
            SceneManager.switchScene("passwordManagerDashboard.fxml");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void generatePassword() throws IOException {
        website = websiteField.getText();
        username = userField.getText();
        password = passwordField.getText();
        website_url = urlField.getText();
        notes = notesField.getText();
        GeneratePasswordController.setPreviousSection("editEntry");
        SceneManager.switchScene("generatePassword.fxml");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SceneManager.checkIfWindowClosed(exitButton);
        SceneManager.checkIfWindowMinimized(minimizeButton);

        new SceneManager.windowDragger().makeDraggable(insidePane);

        if (generator_accessed) {
            generator_accessed = false;
            websiteField.setText(website);
            userField.setText(username);
            passwordField.setText(password);
            urlField.setText(website_url);
            notesField.setText(notes);
        } else
        {
            websiteField.setText(entry.getEntry_website());
            userField.setText(entry.getEntry_username());
            passwordField.setText(entry.getEntry_password());
            urlField.setText(entry.getEntry_url());
            notesField.setText(entry.getEntry_notes());
        }
    }
}
