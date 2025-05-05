package org.example.passwordmanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class PasswordManagerController implements Initializable {

    @FXML
    private AnchorPane insidePane;
    @FXML
    private TableView<JsonReadWrite.Entry> tableView;
    @FXML
    private TableColumn<JsonReadWrite.Entry, String> websiteColumn;
    @FXML
    private TableColumn<JsonReadWrite.Entry, String> usernameColumn;
    @FXML
    private TableColumn<JsonReadWrite.Entry, String> passwordColumn;
    @FXML
    private TableColumn<JsonReadWrite.Entry, String> urlColumn;
    @FXML
    private TableColumn<JsonReadWrite.Entry, String> notesColumn;
    @FXML
    private TableColumn<JsonReadWrite.Entry, Void> actionColumn;
    @FXML
    private Label minimizeButton;
    @FXML
    private Label exitButton;
    @FXML
    private TextField filterField;

    private ArrayList<JsonReadWrite.Entry> entryList;

    private static SecretKeySpec AESKey;

    public static SecretKeySpec getAESKey() {
        return AESKey;
    }
    public void setMasterPassword(String masterPassword) throws Exception {
        AESKey = Encryption.getAESKeyFromPassword(masterPassword, Encryption.getSalt());
    }
    public void goToPasswordCreate() throws IOException {
        SceneManager.switchScene("createPasswordEntry.fxml");
    }
    public void goToPasswordGenerate() throws IOException {
        GeneratePasswordController.setPreviousSection("passwordManager");
        SceneManager.switchScene("generatePassword.fxml");
    }
    public void exportDatabase()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Password Manager Database");

        fileChooser.setInitialFileName("passwordmanager.json");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
        );

        File selectedFile = fileChooser.showSaveDialog(insidePane.getScene().getWindow());

        if (selectedFile != null) {
            File originalFile = new File("src/main/resources/org/example/passwordmanager/passwordmanager.json");
            try {
                Files.copy(originalFile.toPath(), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkPasswordStrength() throws IOException {
        SceneManager.switchScene("passwordStrengthEvaluator.fxml");
    }
    private void initializeTableView(String currentFilterText) {
        entryList = JsonReadWrite.getAllEntries();

        ObservableList<JsonReadWrite.Entry> observableList = FXCollections.observableArrayList(entryList);
        FilteredList<JsonReadWrite.Entry> filteredData = new FilteredList<>(observableList, e -> true);

        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            String lowerFilter = newValue.toLowerCase();

            filteredData.setPredicate(entry -> {
                if (lowerFilter.isEmpty()) return true;
                return entry.getEntry_website().toLowerCase().contains(lowerFilter);
            });
        });
        if (!currentFilterText.isEmpty()) {
            String lowerFilter = currentFilterText.toLowerCase();
            filteredData.setPredicate(entry -> entry.getEntry_website().toLowerCase().contains(lowerFilter));
        }

        SortedList<JsonReadWrite.Entry> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            setMasterPassword(JsonReadWrite.AppSession.getMasterPassword());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SceneManager.checkIfWindowClosed(exitButton);

        SceneManager.checkIfWindowMinimized(minimizeButton);

        new SceneManager.windowDragger().makeDraggable(insidePane);

        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("entry_website"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("entry_username"));
        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("entry_password"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("entry_url"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("entry_notes"));

        initializeTableView("");

        urlColumn.setCellFactory(column -> new TableCell<>() {
            private final Hyperlink link = new Hyperlink();
            {
                link.setOnAction(event -> {
                    String url = link.getText();
                    if (!url.startsWith("http")) {
                        url = "https://" + url;
                    }
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setGraphic(null);
                } else {
                    link.setText(item);
                    setGraphic(link);
                }
            }
        });

        actionColumn.setCellFactory(col -> new TableCell<>(){
            private final Button copyButton = new Button("Copy Password");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttonBox = new HBox(5, copyButton, editButton, deleteButton);
            {
                copyButton.setOnAction(e -> {
                    JsonReadWrite.Entry entry = getTableView().getItems().get(getIndex());
                    ClipboardContent content = new ClipboardContent();
                    content.putString(entry.getEntry_password());
                    Clipboard.getSystemClipboard().setContent(content);

                });

                editButton.setOnAction(e -> {
                    JsonReadWrite.Entry entry = getTableView().getItems().get(getIndex());
                    EditEntryController.setEntry(entry);
                    try {
                        SceneManager.switchScene("editPasswordEntry.fxml");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                deleteButton.setOnAction(e -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText(null);
                    alert.setContentText("Are you sure you want to delete this entry?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK) {

                        JsonReadWrite.Entry entry = getTableView().getItems().get(getIndex());
                        ArrayList<JsonReadWrite.Entry> updatedEntries = JsonReadWrite.getAllEntries();

                        for (JsonReadWrite.Entry updatedEntry: updatedEntries)
                        {
                            if ((entry.getEntry_website().equals(updatedEntry.getEntry_website()) &&
                                    entry.getEntry_username().equals(updatedEntry.getEntry_username()) &&
                                    entry.getEntry_password().equals(updatedEntry.getEntry_password()) &&
                                    entry.getEntry_url().equals(updatedEntry.getEntry_url()) &&
                                    entry.getEntry_notes().equals(updatedEntry.getEntry_notes()) &&
                                    entry.getIV().equals(updatedEntry.getIV())))
                            {
                                updatedEntries.remove(updatedEntry);
                                JsonReadWrite.overwriteFile(updatedEntries);
                                break;
                            }
                        }


                        initializeTableView(filterField.getText());
                    }

                });

                buttonBox.setAlignment(Pos.CENTER);
            }


            @Override
            protected void updateItem(Void item, boolean empty)
            {
                super.updateItem(item, empty);
                if (empty)
                {
                    setGraphic(null);
                }else
                {
                    setGraphic(buttonBox);
                }
            }
        });
        tableView.widthProperty().addListener((obs, oldVal, newVal) -> {
            tableView.getColumns().forEach(col -> col.setReorderable(false));
        });
    }
}
