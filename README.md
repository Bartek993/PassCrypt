# How to Run

1. Download the project & open in Intellj Idea
2. Ensure the dependencies in pom.xml are loaded in.
3. Copy the path location of javafx-sdk-23.0.2\lib and save for the next step (e.g. H:\PassCrypt-main\javafx-sdk-23.0.2\lib)
4. In Intellj go to Run -> Edit Configurations -> Add New Configuration -> Application:
   1. Main Class: `org.example.passwordmanager.StartApplication`
   2. Add VM Options: `--module-path "{Insert Path Location}" --add-modules javafx.controls,javafx.fxml` (e.g --module-path "H:\PassCrypt-main\javafx-sdk-23.0.2\lib" --add-modules javafx.controls,javafx.fxml)
8. Apply Changes & Run StartApplication.java


### Creating New Database File if there is an existing one

To create a new database file if one has already been created, remove passwordmanager.json from "src\main\resources\org\example\passwordmanager" directory
