module com.example.memorycardflip {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires com.google.gson;
    requires javafx.media;

    opens com.example.memorycardflip to javafx.graphics;
    opens com.example.memorycardflip.controller to javafx.fxml;
    opens com.example.memorycardflip.ui to javafx.fxml;

    exports com.example.memorycardflip;
    opens com.example.memorycardflip.service to javafx.fxml;
    opens com.example.memorycardflip.model to com.google.gson;
}