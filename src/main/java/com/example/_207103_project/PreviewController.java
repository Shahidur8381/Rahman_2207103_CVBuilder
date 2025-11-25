package com.example._207103_project;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
public class PreviewController {
    private CVModel model;
    private boolean isEditMode = false;
    @FXML private Label nameLabel;
    @FXML private Label contactLabel;
    @FXML private VBox educationBox;
    @FXML private FlowPane skillsBox;
    @FXML private VBox experienceBox;
    @FXML private VBox projectsBox;
    @FXML private ImageView previewPhoto;
    public void setModel(CVModel model){
        this.model = model;
        loadData();
    }
    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
    }
    private void loadData(){
        if(model == null) return;
        nameLabel.setText(emptyIfNull(model.getFullName()));
        contactLabel.setText(String.join(" | ",
                emptyIfNull(model.getEmail()),
                emptyIfNull(model.getPhone()),
                emptyIfNull(model.getAddress())).replaceAll("\\s*\\|\\s*$",""));
        educationBox.getChildren().clear();
        for(String e : model.getEducation()){
            if(e == null || e.isBlank()) continue;
            Label l = new Label("• " + e.trim());
            l.setStyle("-fx-text-fill:#0b3a4a; -fx-font-size:13px;");
            educationBox.getChildren().add(l);
        }
        skillsBox.getChildren().clear();
        for(String s : model.getSkills()){
            if(s == null || s.isBlank()) continue;
            Label chip = new Label(s.trim());
            chip.setStyle("-fx-text-fill:#0b3a4a; -fx-font-size:12px; -fx-border-color:#cbd5e1; -fx-border-radius:4; -fx-background-radius:4; -fx-padding:4 8 4 8; -fx-background-color: transparent;");
            chip.setMinHeight(Label.USE_PREF_SIZE);
            skillsBox.getChildren().add(chip);
        }
        experienceBox.getChildren().clear();
        for(String x : model.getExperience()){
            if(x == null || x.isBlank()) continue;
            Label l = new Label("• " + x.trim());
            l.setStyle("-fx-text-fill:#0b3a4a; -fx-font-size:13px;");
            experienceBox.getChildren().add(l);
        }
        projectsBox.getChildren().clear();
        for(String p : model.getProjects()){
            if(p == null || p.isBlank()) continue;
            Label l = new Label("• " + p.trim());
            l.setStyle("-fx-text-fill:#0b3a4a; -fx-font-size:13px;");
            projectsBox.getChildren().add(l);
        }
        if(model.getPhotoPath() != null && !model.getPhotoPath().isBlank()){
            try{
                previewPhoto.setImage(new Image(model.getPhotoPath(),120,120,true,true));
                previewPhoto.setVisible(true);
            }catch(Exception ex){
                previewPhoto.setImage(null);
                previewPhoto.setVisible(false);
            }
        }else{
            previewPhoto.setImage(null);
            previewPhoto.setVisible(false);
        }
    }
    @FXML
    private void saveCV(ActionEvent event) {
        if (model == null) return;
        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            if (isEditMode) {
                dbManager.updateCV(model);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("CV updated successfully!");
                alert.showAndWait();
            } else {
                int cvId = dbManager.saveCV(model);
                if (cvId > 0) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("CV saved successfully to database!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to save CV to database.");
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred: " + e.getMessage());
            alert.showAndWait();
        }
    }
    @FXML
    private void backToCreate(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/create.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
    private String emptyIfNull(String s){ return s == null ? "" : s; }
}

