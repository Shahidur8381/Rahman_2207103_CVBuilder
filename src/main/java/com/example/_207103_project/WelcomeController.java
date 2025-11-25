package com.example._207103_project;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.stage.Stage;
public class WelcomeController {
    @FXML
    private void createClicked(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/create.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
    @FXML
    private void exploreClicked(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/explore.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
    @FXML
    private void backClicked(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/WelcomePage.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }
}

