module com.example.memorycardflip {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.memorycardflip to javafx.graphics;
    opens com.example.memorycardflip.controller to javafx.fxml;
    opens com.example.memorycardflip.ui to javafx.fxml;

    exports com.example.memorycardflip;
}