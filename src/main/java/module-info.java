module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires netty.all;
    requires java.desktop;
    requires jna;
    requires platform;


    opens com.sauren.sauren to javafx.fxml;
    exports com.sauren.sauren;
}