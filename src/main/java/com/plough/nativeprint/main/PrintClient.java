package com.plough.nativeprint.main;
import com.plough.nativeprint.serversocket.PrintClientServer;

import javax.swing.JOptionPane;

/**
 * Created by Administrator on 2015/8/18 0018.
 */

public class PrintClient {

    /**
     * 执行本地打印
     *
     * @param args 参数
     */
    public static void main(String[] args){
//        if (SingletonControl.isStarted()){
//            SingletonControl.clientSend(args);
//            System.exit(0);
//        }

        // 与服务器交互的本地打印实例
//        NativePrint print = NativePrint.getInstance();
        // 托盘控制
        PrintTray tray = PrintTray.getInstance();
        // 单例监听
//        SingletonControl.createListeningServer(print, tray, args);

        try {
            PrintClientServer.getInstance().start();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            System.exit(1);
        }
    }


}