package com.example._207103_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

public class PreviewController {

    private CVModel model;

    @FXML private Label nameLabel;
    @FXML private Label contactLabel;
    @FXML private VBox educationBox;
    @FXML private FlowPane skillsBox;
    @FXML private VBox experienceBox;
    @FXML private VBox projectsBox;

    public void setModel(CVModel model) {
        this.model = model;
        loadData();
    }

    private void loadData() {
        nameLabel.setText(model.getFullName());

        contactLabel.setText(
                model.getEmail() + " | " +
                        model.getPhone() + " | " +
                        model.getAddress()
        );

        educationBox.getChildren().clear();
        model.getEducation().forEach(e -> educationBox.getChildren().add(new Label("• " + e)));

        skillsBox.getChildren().clear();
        model.getSkills().forEach(s -> {
            Label tag = new Label(s);
            tag.setStyle("-fx-border-color: gray; -fx-padding: 3 6;");
            skillsBox.getChildren().add(tag);
        });

        experienceBox.getChildren().clear();
        model.getExperience().forEach(x -> experienceBox.getChildren().add(new Label("• " + x)));

        projectsBox.getChildren().clear();
        model.getProjects().forEach(p -> projectsBox.getChildren().add(new Label("• " + p)));
    }

    @FXML
    private void backToCreate(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/create.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
}
