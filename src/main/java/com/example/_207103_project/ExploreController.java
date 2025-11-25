package com.example._207103_project;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;
import java.util.Optional;
public class ExploreController {
    @FXML private VBox cvListContainer;
    @FXML
    public void initialize() {
        loadSavedCVs();
    }
    private void loadSavedCVs() {
        cvListContainer.getChildren().clear();
        DatabaseManager dbManager = DatabaseManager.getInstance();
        List<CVModel> cvList = dbManager.getAllCVs();
        if (cvList.isEmpty()) {
            Label emptyLabel = new Label("No saved CVs found. Create one first!");
            emptyLabel.setStyle("-fx-font-size:16px; -fx-text-fill:#64748b; -fx-padding:20;");
            cvListContainer.getChildren().add(emptyLabel);
        } else {
            for (CVModel cv : cvList) {
                cvListContainer.getChildren().add(createCVCard(cv));
            }
        }
    }
    private HBox createCVCard(CVModel cv) {
        HBox card = new HBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8,0,0,2); " +
                      "-fx-padding: 16;");
        card.setPrefHeight(Region.USE_COMPUTED_SIZE);
        VBox infoBox = new VBox(6);
        Label nameLabel = new Label(cv.getFullName());
        nameLabel.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#0b3a4a;");
        Label emailLabel = new Label(cv.getEmail() != null ? cv.getEmail() : "");
        emailLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        Label phoneLabel = new Label(cv.getPhone() != null ? cv.getPhone() : "");
        phoneLabel.setStyle("-fx-font-size:13px; -fx-text-fill:#64748b;");
        infoBox.getChildren().addAll(nameLabel, emailLabel, phoneLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        VBox buttonBox = new VBox(8);
        buttonBox.setStyle("-fx-alignment: center-right;");
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white; " +
                        "-fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 16 6 16;");
        viewBtn.setOnAction(_ -> viewCV(cv.getId()));
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color:#10b981; -fx-text-fill:white; " +
                        "-fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 16 6 16;");
        editBtn.setOnAction(_ -> editCV(cv.getId()));
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; " +
                          "-fx-font-weight:bold; -fx-background-radius:6; -fx-padding:6 16 6 16;");
        deleteBtn.setOnAction(_ -> deleteCV(cv.getId()));
        buttonBox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
        card.getChildren().addAll(infoBox, buttonBox);
        VBox.setMargin(card, new Insets(0, 0, 12, 0));
        return card;
    }
    private void viewCV(int cvId) {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            CVModel cv = dbManager.getCVById(cvId);
            if (cv != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/preview.fxml"));
                Parent root = loader.load();
                PreviewController pc = loader.getController();
                pc.setModel(cv);
                Stage stage = (Stage) cvListContainer.getScene().getWindow();
                stage.getScene().setRoot(root);
            }
        } catch (Exception e) {
            showError("Failed to load CV: " + e.getMessage());
        }
    }
    private void editCV(int cvId) {
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            CVModel cv = dbManager.getCVById(cvId);
            if (cv != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/create.fxml"));
                Parent root = loader.load();
                CreateController controller = loader.getController();
                controller.loadCVForEdit(cv);
                Stage stage = (Stage) cvListContainer.getScene().getWindow();
                stage.getScene().setRoot(root);
            }
        } catch (Exception e) {
            showError("Failed to load CV for editing: " + e.getMessage());
        }
    }
    private void deleteCV(int cvId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete this CV?");
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                DatabaseManager dbManager = DatabaseManager.getInstance();
                dbManager.deleteCV(cvId);
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("CV deleted successfully!");
                successAlert.showAndWait();
                loadSavedCVs();
            } catch (Exception e) {
                showError("Failed to delete CV: " + e.getMessage());
            }
        }
    }
    @FXML
    private void backToWelcome(ActionEvent event) {
        try {
            var resource = getClass().getResource("/WelcomePage.fxml");
            if (resource == null) {
                showError("WelcomePage.fxml not found");
                return;
            }
            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            showError("Failed to return to welcome page: " + e.getMessage());
        }
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

