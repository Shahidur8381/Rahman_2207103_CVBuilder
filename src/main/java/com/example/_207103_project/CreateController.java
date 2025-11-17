package com.example._207103_project;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class CreateController {

    @FXML private TextField name;
    @FXML private TextField email;
    @FXML private TextField phone;
    @FXML private TextField address;
    @FXML private TextArea education;
    @FXML private TextArea skill;
    @FXML private TextArea project;
    @FXML private TextArea work;
    @FXML private ImageView photoView;
    private String photoPath;

    @FXML
    private void onChoosePhoto(ActionEvent event) throws Exception {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files","*.png","*.jpg","*.jpeg","*.gif"));
        File file = fc.showOpenDialog(((Node)event.getSource()).getScene().getWindow());
        if(file!=null){
            photoPath = file.toURI().toString();
            photoView.setImage(new Image(photoPath,120,120,true,true));
        }
    }

    @FXML
    private void backToWelcome(ActionEvent event) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/WelcomePage.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    @FXML
    private void onGenerate(ActionEvent event) throws Exception {
        CVModel model = new CVModel();
        model.setFullName(text(name.getText()));
        model.setEmail(text(email.getText()));
        model.setPhone(text(phone.getText()));
        model.setAddress(text(address.getText()));
        model.setPhotoPath(photoPath==null?"":photoPath);
        model.getEducation().clear();
        for(String s: splitLines(education.getText())) if(!s.isBlank()) model.getEducation().add(s.trim());
        model.getSkills().clear();
        for(String s: splitCommaOrLines(skill.getText())) if(!s.isBlank()) model.getSkills().add(s.trim());
        model.getProjects().clear();
        for(String s: splitLines(project.getText())) if(!s.isBlank()) model.getProjects().add(s.trim());
        model.getExperience().clear();
        for(String s: splitLines(work.getText())) if(!s.isBlank()) model.getExperience().add(s.trim());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/preview.fxml"));
        Parent root = loader.load();
        PreviewController pc = loader.getController();
        pc.setModel(model);
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    private String text(String s){ return s==null?"":s.trim(); }
    private String[] splitLines(String s){ return s==null||s.isBlank()?new String[0]:s.split("\\r?\\n"); }
    private String[] splitCommaOrLines(String s){ return s==null||s.isBlank()?new String[0]:s.split("[,\\r?\\n]+"); }
}
