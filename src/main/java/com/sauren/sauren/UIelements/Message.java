package com.sauren.sauren.UIelements;

import com.sauren.sauren.HelloApplication;
import com.sauren.sauren.HelloController;
import com.sauren.sauren.Network;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.text.Text;

import java.io.IOException;

public class Message extends Application{
    /* public static void newWindow(String title){
           Label message = new Label(title);

           Stage window = new Stage();
           window.initModality(Modality.WINDOW_MODAL);
           Pane pane = new Pane();

           pane.getChildren().add(message);

           //Button btn = new Button("OK");

           //btn.setOnAction(event -> window.close());
           //pane.getChildren().addAll(btn);

           Scene scene = new Scene(pane,200,100);

           window.setScene(scene);
           window.setTitle(title);
           window.show();
   }
    * */

        @Override
        public void start(Stage stage) throws IOException {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Message.fxml"));
                Scene scene = new Scene(fxmlLoader.load(), 425,218 );
                stage.setTitle("Work");
                stage.setScene(scene);
                stage.show();
        }
        @Override
        public void stop() throws Exception {
                super.stop();
                System.exit(0);
        }

        public static void main(String[] args) {
                launch();
        }
}
