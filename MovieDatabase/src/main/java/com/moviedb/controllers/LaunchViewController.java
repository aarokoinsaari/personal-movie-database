package com.moviedb.controllers;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import com.moviedb.database.DatabaseInitializer;

import fi.jyu.mit.fxgui.Dialogs;


/**
 * Controller class that is responsible for handling the UI logic related to the launch window.
 * It includes checking and validating the given name of the database and creating or
 * simply opening an existing one to the Main View.
 */
public class LaunchViewController {

    private static final Logger logger = Logger.getLogger(LaunchViewController.class.getName());

    @FXML
    private Button cancelButton;
    @FXML
    private TextField databaseNameField;


    /**
     * Handles the OK button click in the launch window.
     * Checks if the specified database exists, and if it does, opens the main view for that,
     * otherwise prompts user to create a new database by the given name and opens
     * the main view for that if the user clicks Yes.
     */
    @FXML
    void handleOkButton() {
        // Validate the database name
        String dbName = databaseNameField.getText().trim();
        if (dbName.isEmpty()) {
            Dialogs.showMessageDialog("Database name can not be empty");
            databaseNameField.clear();
            return;
        }

        // Format the database name and save the new path
        String formattedDBName = formatDatabaseName(dbName);
        String dbPath = "src/main/java/com/moviedb/database/" + formattedDBName + ".db";

        // Check if the database exists, and if not prompt the user to create a new one with that name
        if (databaseExists(dbPath)) {
            openMainView(formattedDBName);
        } else {
            boolean answer = Dialogs.showQuestionDialog("Database does not exist", "Do you want to create a new database?",
                    "Yes", "No");
            if (answer) {
                createNewDatabase(formattedDBName);
                openMainView(formattedDBName);
            }
        }
    }


    /**
     * Formats the database name by removing all spaces and converting to lower case.
     *
     * @param dbName The original database name.
     * @return The formatted database name.
     */
    private String formatDatabaseName(String dbName) {
        return dbName.replaceAll("\\s+", "").toLowerCase();
    }


    /**
     * Checks if a given database exists.
     *
     * @param dbPath Path of the database to check.
     * @return true if the database already exists in the given path, otherwise false.
     */
    private boolean databaseExists(String dbPath) {
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }


    /**
     * Opens the main window of the program after getting the database name from the user.
     *
     * @param dbName Name of the database.
     */
    private void openMainView(String dbName) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainView.fxml"));
            Parent root = loader.load();

            MainViewController controller = loader.getController();
            controller.setDatabaseName(dbName);
            controller.initializeAndSetupDatabase();

            // Create new Scene and set it as current window
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            controller.setPrimaryStage(stage);  // Set the main window for controller
            stage.setScene(scene);
            stage.setTitle("Main View - " + dbName);
            stage.show();

            // Close the current window
            Stage currentStage = (Stage) databaseNameField.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to open the MainView. Message: " + e.getMessage(), e);
            Dialogs.showMessageDialog("Failed to open the main window.");  // Inform the user
        }
    }


    /**
     * Creates a new database based on a given name.
     *
     * @param dbName Name of the new database.
     */
    private void createNewDatabase(String dbName) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:src/main/java/com/moviedb/database/" + dbName + ".db");
            DatabaseInitializer.initialize(connection);
            connection.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create a new database. SQL state: " + e.getSQLState()
                    + " Error code: " + e.getErrorCode() + " Message: " + e.getMessage(), e);
            Dialogs.showMessageDialog("Failed to create a new database.");  // Inform the user
        }
    }


    /**
     * Handles the 'Cancel' button click in the launch window by closing the program.
     */
    @FXML
    void handleCancelButton() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
