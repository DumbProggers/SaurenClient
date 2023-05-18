package com.sauren.sauren;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Objects;

import com.sauren.sauren.UIelements.Message;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

public class Network {
    private Channel Channel;
    private FileUploadFile fileUploadFile;
    public static String infoServ;

    public static String message;

    public Network(String ip,int port){
        new Thread(()->{//запуск если не сделать в потоке, то граф интерфейс не откроется
            EventLoopGroup workerGroup = new NioEventLoopGroup();//обработка сетевых событый
            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)//
                        //.option(ChannelOption.SO_BACKLOG,Integer.MAX_VALUE)
                        .handler(new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(io.netty.channel.Channel channel) throws Exception {
                                Channel = channel;//Запоминаем сокет канал, создаем ссылку на сокет канал и записываем в сокет канала.
                                channel.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(FileUploadFile.class.getClassLoader())),//добавляем декодер
                                        new SimpleChannelInboundHandler <Object>(){
                                            @Override
                                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                                cause.printStackTrace();
                                                super.exceptionCaught(ctx, cause);
                                            }

                                            @Override
                                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                                                if(o instanceof String){
                                                    String mes = (String) o;

                                                    mes = mes.substring(1);
                                                    int index = mes.indexOf("$");
                                                    String command = mes.substring(0,index);
                                                    String arg = mes.substring(index+1);

                                                    if(command.equals("MSG"))
                                                        message = arg;
                                                }
                                            }
                                        })
                                ;
                            }//При подключении к серваку открывается канал с которым мы можем работать.
                        });

                ChannelFuture future = b.connect(ip,port).sync();
                future.channel().closeFuture().sync();//выход с канала если снаружи дать команду остановки.
            } catch (Exception e) {
                //информация о состоянии подключения к серверу, сохраняется во внешнюю переменную что бы отследить состояние в другом файле(файле графического интерфейса).
                infoServ = "error";
                //throw new RuntimeException(e);
            }
        }).start();
    }



    public void sendMessage(String str){
        Channel.writeAndFlush(str);
    }
    public void sendDelay(int delay){
        Channel.writeAndFlush(delay);
    }
    public void sendFile() throws IOException, InterruptedException {
        String currentDir = System.getProperty("user.dir");//папка проэкта
        String format = "png";
        String fullScreenName = "screen"+"."+format;
        String pathToScreen = currentDir +"\\"+fullScreenName;

        int wight=1920;
        int hight=1080;
        double k = 0.6;//40%

        SaveScreen(currentDir, format, fullScreenName);//сохраняем скриншот

        resizeFile(pathToScreen,pathToScreen, (int) (wight*k), (int) (hight*k));//изменяем его качество

        fileUploadFile = getFile(pathToScreen);//получаем файл
        sendFile(Channel);//отправляем

    }

    public static void resizeFile(String imagePathToRead,
                                  String imagePathToWrite, int resizeWidth, int resizeHeight)
            throws IOException {

        File fileToRead = new File(imagePathToRead);
        BufferedImage bufferedImageInput = ImageIO.read(fileToRead);

        BufferedImage bufferedImageOutput = new BufferedImage(resizeWidth,
                resizeHeight, bufferedImageInput.getType());

        Graphics2D g2d = bufferedImageOutput.createGraphics();
        g2d.drawImage(bufferedImageInput, 0, 0, resizeWidth, resizeHeight, null);
        g2d.dispose();

        String formatName = imagePathToWrite.substring(imagePathToWrite
                .lastIndexOf(".") + 1);

        ImageIO.write(bufferedImageOutput, formatName, new File(imagePathToWrite));
    }

    public static FileUploadFile getFile(String pathToFile){
        File file = new File(pathToFile);
        String fileName  = file.getName();

        FileUploadFile uploadFile = new FileUploadFile();
        uploadFile.setFile(file);
        uploadFile.setFileName(fileName);
        uploadFile.setStarPos(0);
        return uploadFile;
    }
    public void sendFile(Channel ctx) throws IOException {
        RandomAccessFile randomAccessFile;
        int lastLength = 0;
        randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
        fileUploadFile.setStarPos(0);
        fileUploadFile.setEndPos((int) randomAccessFile.length());
        randomAccessFile.seek(fileUploadFile.getStarPos());
        lastLength = fileUploadFile.getEndPos();
        byte[] bytes = new byte[lastLength];
        int byteRead;
        if ((byteRead = randomAccessFile.read(bytes)) != -1) {
            fileUploadFile.setEndPos(byteRead);
            fileUploadFile.setBytes(bytes);
            ctx.writeAndFlush(fileUploadFile);
            randomAccessFile.close();
            //System.out.println(randomAccessFile.length());
            System.out.println("file send");
        }
    }
    public static BufferedImage grabScreen() {
        try {
            return new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        } catch (SecurityException | AWTException ignored) {
        }
        return null;
    }
    public static void SaveScreen(String pathToSave, String formatName, String screenNameWithFormat) {
        try {
            ImageIO.write(Objects.requireNonNull(grabScreen()), formatName, new File(pathToSave, screenNameWithFormat));
        } catch (IOException e) {
            System.out.println("IO exception" + e);
        }
    }
}
