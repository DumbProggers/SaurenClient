package com.sauren.sauren;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    //переменные берутся из fxml файла обращение к ним идет по их id

    @FXML
    public TextField ipText;
    @FXML
    //например Button fx:id="connectServ" обращение к свойствам кнопки будет происходить по этой перменной, благодаря ему мы имеед доступ ко всем свойствам кнопки.
    public  Button connectServ;
    private Network network;

    @FXML
    TextField msgField;

    @FXML
    TextArea mainArea;
    @FXML
    Button connect;
    @FXML
    public  Label timeInWork;
    @FXML
    public Label infoserv;

    int count = 0;
    long now;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(new File("config.txt").exists()) {
            ipText.setText(readFile("config.txt"));
        }
        else{
            saveToDataBase("");
        }

    }
    public void Connect(){
        saveToDataBase(ipText.getText());

        //connect.setText("Начать работу");
        network = new Network(ipText.getText());
        connectServ.setDisable(true);
        try {
            Thread.sleep(500);
            if(Objects.equals(Network.infoServ, "error")){
                infoserv.setText("Сервер не отвечает. Попробуйте перезапустить \nклиент и удостоверьтесь, что сервер запущен.");
                connect.setText("Закрыть программу.");
            }
          }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
    }

    public void sendMsgAction(ActionEvent actionEvent) throws IOException {

        if(Objects.equals(Network.infoServ, "error")){
            //если из класса Network переменная infoserv имеет значение error, подключение создано не будет и мы завершим процессы
            infoserv.setText("Сервер не отвечает. Попробуйте перезапустить \nклиент и удостоверьтесь, что сервер запущен.");
            connect.setText("Закрыть программу.");
            System.exit(0);

        }
        else
        {
            now = new Date().getTime();
            //таймер проведенного времени в работе
            Timeline timeline = new Timeline (
                    new KeyFrame(
                            Duration.millis(1000), //1000 мс = 1 сек
                            ae -> {
                                try {
                                    timeInWork.setText(longToDate(Math.abs(now - new Date().getTime())));
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    )
            );
            timeline.setCycleCount((int) Long.MAX_VALUE); //Ограничим число повторений
            timeline.play(); //Запускаем


            count++;
            connect.setText("Закончить работу");
            if(count%2==0){
                connect.setText("Закончить работу");
                System.exit(0);
            }else{

                new Thread(()->{
                    while (true){
                        try {
                            //timeInWork.setText(String.valueOf(now - new Date().getTime()));
                            network.sendMessage(System.getProperty("user.name")+"\\"+EnumerateWindows.activeWindow()+"\\"+EnumerateWindows.activeTitleWindow());
                            network.sendDelay(1000);
                            network.send();

                            Thread.sleep(1000);
                        } catch (IOException | InterruptedException e) {
                            System.out.println("ERR213213OR");
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
            }
        }
        //network.sendMessage("Hello");
    }
    public String longToDate(long timeInProject) throws ParseException {

        long diffSecondsAll = timeInProject / 1000 % 60;
        long diffMinutesAll = timeInProject / (60 * 1000) % 60;
        long diffHoursAll = timeInProject / (60 * 60 * 1000) % 24;
        long diffDaysAll = timeInProject / (24 * 60 * 60 * 1000);

        return diffDaysAll+" days "+diffHoursAll+" hours "+diffMinutesAll+" min "+diffSecondsAll+" sec";
    }
    public void saveToDataBase(String text){

        try(FileWriter writer = new FileWriter("config.txt", false))
        {
            // запись всей строки
            writer.write(text);
            // запись по символам
            writer.flush();
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
    }
    public String readFile(String file){
        String dt = "";
        try(BufferedReader br = new BufferedReader (new FileReader(file)))
        {
            // чтение посимвольно
            int c;
            while((c=br.read())!=-1){
                //System.out.print((char)c);
                dt+=(char)c;
            }
        }
        catch(IOException ex){

            System.out.println(ex.getMessage());
        }
        return dt;
    }
}