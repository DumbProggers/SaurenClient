package com.sauren.sauren;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javax.imageio.ImageIO;

public class Network {
    private Channel Channel;
    public static String inf = "";
    private static final String HOST = "192.168.0.105";
    //private static final String HOST = "18.177.53.48";
    private static final int port = 8190;


    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private FileUploadFile fileUploadFile;
    public static String infoServ;

    public Network(String ip){
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
                                                    String s = (String) o;
                                                    System.out.println(s);
                                                    if (Objects.equals(s, "cloze")){
                                                        Channel.close();
                                                        Channel.closeFuture();
                                                        System.exit(1);
                                                    }
                                                    inf+=s;
                                                    System.out.print(s);
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

                System.out.println("ERROR");
                throw new RuntimeException(e);
            }
        }).start();

    }

    public void sendMessage(String str){
        Channel.writeAndFlush(str);
    }
    public void sendDelay(int delay){
        Channel.writeAndFlush(delay);
    }

    public static String currentDir = System.getProperty("user.dir");//папка проэкта
    public static String pathToSaveScreen = currentDir;
    public static String format = "png";
    public static String screenName = "screen";
    public static String fullScreenName = screenName+"."+format;
    public static String pathToScreen = pathToSaveScreen+"\\"+fullScreenName;

    public void send() throws IOException, InterruptedException {
        int wight=1920;
        int hight=1080;
        double k = 0.6;//40%

        SaveScreen(pathToSaveScreen, format, fullScreenName);
        //Thread.sleep(500);
        resizeFile(pathToScreen,pathToScreen, (int) (wight*k), (int) (hight*k));
        //Thread.sleep(500);
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //получаем файл и отправляем его
        fileUploadFile = getFile(pathToScreen);
        sendFile(Channel);

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
        randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
        fileUploadFile.setStarPos(0);
        fileUploadFile.setEndPos((int) randomAccessFile.length());
        randomAccessFile.seek(fileUploadFile.getStarPos());
        lastLength = (int) randomAccessFile.length();
        lastLength = fileUploadFile.getEndPos();
        byte[] bytes = new byte[lastLength];
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
