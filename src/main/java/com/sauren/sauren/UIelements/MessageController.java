package com.sauren.sauren.UIelements;

import com.sauren.sauren.Network;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MessageController implements Initializable {
    @FXML
    private Button button;
    @FXML
    private Label message;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        message.setText(Network.message);
    }
    public void setActionButton(){
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
