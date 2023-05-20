package com.sauren.sauren;

import com.sauren.sauren.UIelements.Message;
import com.sauren.sauren.UIelements.MessageController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
    private Network network;
    @FXML
    Button connect;
    @FXML
    public  Label timeInWork;
    @FXML
    public Label infoserv;
    private static boolean isConnected=false;
    int varToChecking = 0;
    long now;
    private static HelloController mainApp;

    private static String info;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        //Message.newWindow("Message!!!");
        if(new File("config.txt").exists()){
            //устанавливаем ip и порт из конфига
            ipText.setText(readFile("config.txt"));
        }
        Timeline timeline = new Timeline(
                new KeyFrame(
                        //задержка таймера
                        Duration.millis(1000), //1000 мс = 1 сек
                        ae -> {
                            if(Network.message!=null){
                                System.out.println(Network.message);

                                Message message = new Message();
                                try {
                                    message.start(new Stage());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                Network.message = null;
                            }
                        })
        );
        timeline.setCycleCount(Integer.MAX_VALUE); //Ограничим число повторений
        timeline.play(); //Запускаем

        //


    }
    public void sendMsgAction(ActionEvent actionEvent) throws IOException, InterruptedException
    {
        if(isConnected)
        {
            connect.setText("Закончить работу");
            System.exit(0);
        }
        //сохраняем данные с текста в конфиг файл
        saveConfigFile(ipText.getText());

        //получаем с текста информацию о будущем подключении
        String infoConnection = ipText.getText();

        //если данные не введены выдать ошибку.
        if(infoConnection.length()==0)
        {
            infoserv.setText("Данные не введены!");
            return;
        }//иначе:
            //проверяем, ввел ли пользователь порт.
            if(infoConnection.contains(":"))
            {
                //достаем из строки порт и айпи адресс.
                int index = infoConnection.indexOf(":");
                String ip = infoConnection.substring(0,index);
                int port = Integer.parseInt(infoConnection.replace(ip+":",""));
                try
                {
                    //создаем соединение в новом потоке
                    new Thread(()->{ network = new Network(ip,port);    }).start();
                    //получаем текущую дату
                        now = new Date().getTime();
                        //таймер проведенного слежки за состоянием сервера
                        Timeline timeline = new Timeline(
                                new KeyFrame(
                                        //задержка таймера
                                        Duration.millis(1000), //1000 мс = 1 сек
                                        ae -> {
                                            try {
                                                //если сервер выдал ошибку
                                                if(Objects.equals(info, "error")){
                                                    infoserv.setText("Ошибка подключения!");
                                                    timeInWork.setText("Сервер не работает! Перезапустите приложение");
                                                    connect.setText("ЗАКРЫТЬ");
                                                }
                                                else
                                                {
                                                    //если счетчик таймера меньше 5
                                                    if(varToChecking<=5)
                                                    {
                                                        //устанавливаем статус проверка и инкрементируем счетчик
                                                        varToChecking++;
                                                        timeInWork.setText("Проверка..");
                                                    }
                                                    else
                                                    {
                                                        //иначе показываем время которое пользователь провел в работе.
                                                        timeInWork.setText(longToDate(Math.abs(now - new Date().getTime())));
                                                    }
                                                }
                                            } catch (ParseException e) {
                                                throw new RuntimeException(e);
                                            }
                                        })
                        );
                        timeline.setCycleCount(Integer.MAX_VALUE); //Ограничим число повторений
                        timeline.play(); //Запускаем

                        //счетчик для переключения текста на кнопки
                        isConnected=true;
                        connect.setText("Закончить работу");

                            //отправка файлов и сообщений
                            new Thread(()->
                            {
                                while (true)
                                {
                                    try {
                                        Thread.sleep(1000);
                                        if((network!=null && !Objects.equals(Network.infoServ, "error")))//Если нетворк есть сервер не выдал ошибку.
                                        {
                                            //Отправляем данные и устанавливаем статус о сервере
                                            network.sendMessage(System.getProperty("user.name")+"\\"+EnumerateWindows.activeWindow()+"\\"+EnumerateWindows.activeTitleWindow());
                                            network.sendDelay(1000);
                                            network.sendFile();
                                            info="connect";
                                        }
                                        else
                                        {
                                            //устанавливаем статус сервера
                                            info = "error";
                                        }
                                    }
                                    catch (IOException | InterruptedException e)
                                    {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }).start();
                    }
                catch (Exception ex)
                {
                    //вывод какой либо из ошибок
                    System.out.println(">------|Error|-------");
                    ex.printStackTrace();
                }
            }
            else
            {
                infoserv.setText("Порт не указан!");
            }
    }
    public String longToDate(long timeInProject) throws ParseException {

        long diffSecondsAll = timeInProject / 1000 % 60;
        long diffMinutesAll = timeInProject / (60 * 1000) % 60;
        long diffHoursAll = timeInProject / (60 * 60 * 1000) % 24;
        long diffDaysAll = timeInProject / (24 * 60 * 60 * 1000);

        return diffDaysAll+" days "+diffHoursAll+" hours "+diffMinutesAll+" min "+diffSecondsAll+" sec";
    }
    public void saveConfigFile(String text){

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