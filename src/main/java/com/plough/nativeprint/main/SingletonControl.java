//package com.plough.nativeprint.main;
//
//import com.plough.nativeprint.utils.ArrayUtils;
//import com.plough.nativeprint.utils.SimpleLogger;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.PrintWriter;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * 保证本地打印是一个单例
// *
// * Created by Administrator on 2016/2/18 0018.
// */
//public class SingletonControl {
//
//    private static final int MESSAGEPORT = 51764;
//
//    public static void createListeningServer(final NativePrint nativePrint, final PrintTray tray, final String[] args) {
//        Thread serverSocketThread = new Thread() {
//            public void run() {
//                ServerSocket serverSocket = null;
//                serverSocket = initServerSocket(serverSocket, args);
//                if (serverSocket == null){
//                    return;
//                }
//
//                while (true) {
//                    Socket socket = null;
//                    try {
//                        socket = serverSocket.accept(); // 接收客户连接
//                        ExecutorService service = Executors.newFixedThreadPool(100);
//                        final Socket finalSocket = socket;
//                        service.execute(new Runnable() {
//                            @Override
//                            public void run() {
//                                try {
//                                    List<String> argsFromClient = new ArrayList<String>();
//                                    BufferedReader reader = new BufferedReader(new InputStreamReader(finalSocket.getInputStream()));
//                                    String line = null;
//                                    while ((line = reader.readLine()) != null) {
//                                        argsFromClient.add(line);
//                                    }
//                                    reader.close();
//
//                                    if (argsFromClient.isEmpty()){
//                                        return;
//                                    }
//                                    print(argsFromClient, tray, nativePrint);
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                } finally {
//                                    if (finalSocket != null){
//                                        try {
//                                            finalSocket.close();
//                                        } catch (Exception e){
//
//                                        }
//                                    }
//                                }
//
//                            }
//                        });
//                    } catch (Exception e){
//
//                    }
//                }
//            }
//        };
//        serverSocketThread.start();
//    }
//
//    private static void print(List<String> args, PrintTray tray, NativePrint nativePrint) {
//        nativePrint.printWithArgs(args.toArray(new String[args.size()]));
//    }
//
//    private static void readArgsFromClient(ServerSocket serverSocket, List<String> args){
//        Socket socket = null;
//        BufferedReader reader = null;
//        try {
//            socket = serverSocket.accept(); // 接收客户连接
//            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                args.add(line);
//            }
//        } catch (Exception e){
//
//        } finally {
//            if (socket != null){
//                try {
//                    reader.close();
//                    socket.close();
//                } catch (Exception e){
//
//                }
//            }
//        }
//
//    }
//
//    private static ServerSocket initServerSocket(ServerSocket serverSocket, String[] args) {
//        try {
//            serverSocket = new ServerSocket(MESSAGEPORT);
//        } catch (Exception e1) {
//            SingletonControl.clientSend(args);
//        }
//
//        return serverSocket;
//    }
//
//    public static void clientSend(String[] lines) {
//        if (ArrayUtils.isEmpty(lines)) {
//            return;
//        }
//        Socket socket = null;
//        PrintWriter writer = null;
//        try {
//            socket = new Socket("localhost", MESSAGEPORT);
//
//            writer = new PrintWriter(socket.getOutputStream());
//            for (int i = 0; i < lines.length; i++) {
//                writer.println(lines[i]);
//            }
//
//            writer.flush();
//        } catch (Exception e) {
//            SimpleLogger.getInstance().log(e.getMessage());
//        } finally {
//            try {
//                writer.close();
//                socket.close();
//            } catch (IOException e) {
//                SimpleLogger.getInstance().log(e.getMessage());
//            }
//        }
//    }
//
//    public static boolean isStarted() {
//        try {
//            new Socket("localhost", MESSAGEPORT);
//            return true;
//        } catch (Exception exp) {
//
//        }
//        return false;
//    }
//}
