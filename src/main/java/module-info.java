module com.example.memorycardflip {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.memorycardflip to javafx.fxml;
    exports com.example.memorycardflip;
}