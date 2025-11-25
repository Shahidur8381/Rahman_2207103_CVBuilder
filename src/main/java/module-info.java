module com.example._207103_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example._207103_project to javafx.fxml;
    exports com.example._207103_project;
}