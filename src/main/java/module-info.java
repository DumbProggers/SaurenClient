module com.example.client {
    requires javafx.controls;
    requires javafx.fxml;
    //requires netty.all;
    requires java.desktop;
    requires io.netty.transport;
    requires io.netty.codec;
    requires com.sun.jna;
    requires com.sun.jna.platform;


    opens com.sauren.sauren to javafx.fxml;
    exports com.sauren.sauren;
    exports com.sauren.sauren.UIelements;
    opens com.sauren.sauren.UIelements to javafx.fxml;


}