package com.plough.nativeprint.main;

import com.plough.nativeprint.serversocket.PrintClientServer;
import com.plough.nativeprint.utils.CommonUtils;
import com.plough.nativeprint.utils.EnvUtils;
import com.plough.nativeprint.utils.IOUtils;
import com.plough.nativeprint.utils.SimpleLogger;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 本地打印的托盘提示
 * 作用:
 * 1, 默认常驻后台, 右键退出.
 * 2, 打印过程中的动画展示, 区别打印起始结束
 *
 * Created by Administrator on 2016/2/18 0018.
 */
public class PrintTray {
    private static final String CERT_HOME = CommonUtils.pathJoin(PrintConstants.RELATIVE_CONFIG_HOME, "https/");

    // 托盘正在打印的图标动画间隔
    private static final int DISPLAY_SLEEP_TIME = 333;
    // 当前打印的页码
    private int num;
    private static final Image PRINT_LOGO = IOUtils.readImage("/com/plough/nativeprint/resource/print.png");
    private static final String[] PRINT_IMAGES = new String[]{
            "/com/plough/nativeprint/resource/print1.png",
            "/com/plough/nativeprint/resource/print2.png",
            "/com/plough/nativeprint/resource/print3.png",
    };

    private static PrintTray tray = new PrintTray();

    public static PrintTray getInstance() {
        return tray;
    }

    private TrayIcon trayIcon;
    //是否正在打印
    private boolean printing = false;

    private PrintTray() {
        showTray();
    }

    private void showTray(){
        //判断当前平台是否支持托盘功能
        if (SystemTray.isSupported()){
            //创建托盘实例
            SystemTray tray = SystemTray.getSystemTray();
            //鼠标悬停提示
            String text = "本地程序打印";

            PopupMenu popMenu = new PopupMenu();

            popMenu.add(getHabitMenu());
            popMenu.add(getHttpsMenu());

            MenuItem itmExit = new MenuItem("退出");
            itmExit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    PrintClientServer.getInstance().stop();
                    System.exit(0);
                }
            });
            popMenu.add(itmExit);

            trayIcon = new TrayIcon(PRINT_LOGO,text,popMenu);
            //将托盘图标加到托盘上
            try {
                tray.add(trayIcon);
            } catch (Exception e1) {
                SimpleLogger.getInstance().log(e1.getMessage());
            }
        }
    }

    private Menu getHttpsMenu() {
        Menu httpsMenu = new Menu("https");
        MenuItem itmOpenCer = new MenuItem("打开证书目录");
        itmOpenCer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(CERT_HOME));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        httpsMenu.add(itmOpenCer);
        MenuItem itmOpenDoc = new MenuItem("使用说明");
        itmOpenDoc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
//                String url = SiteCenter.getInstance().acquireUrlByKind("print.https.help", "");
//                BrowseUtils.browse(url);
            }
        });
        httpsMenu.add(itmOpenDoc);
        return httpsMenu;
    }

    private Menu getHabitMenu() {
        Menu habitMenu = new Menu("打印习惯");

        MenuItem itmClear = new MenuItem("清除");
        itmClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ServerConfig.clearConfig();
            }
        });
        habitMenu.add(itmClear);

        MenuItem itmReload = new MenuItem("重新读取");
        itmReload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ServerConfig.reloadConfig();
            }
        });
        habitMenu.add(itmReload);

        MenuItem itmEditConfig = new MenuItem("修改配置文件");
        itmEditConfig.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(EnvUtils.getEnvHome()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    SimpleLogger.getInstance().log(ex.getMessage());
                }
            }
        });
        habitMenu.add(itmEditConfig);

        return habitMenu;
    }

    public void setNum(int num) {
        this.num = num;
    }

    /**
     * 正在打印, 显示打印动画
     */
    public void markPrintingTray(){
        printing = true;
        Thread changLogoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (printing) {
                    loopDisplayImage();
                }
            }
        });
        changLogoThread.start();
    }

    public static void main(String[] args) throws InterruptedException {
        PrintTray tray = new PrintTray();
        tray.markPrintingTray();
        for (int i = 0; i < 1000; i++) {
            Thread.sleep(1000L);
            tray.num = i;
        }
        tray.printOver();
    }

    private void loopDisplayImage() {
        for (String temp : PRINT_IMAGES){
            if (!printing){
                break;
            }
            BufferedImage image = IOUtils.readImage(temp);
            Graphics2D g2d = image.createGraphics();
            g2d.setPaint(new Color(0, 51, 51));
            g2d.setFont(new Font("宋体", Font.PLAIN, 9));
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            String numStr = num + "";
            int x = Math.max(0, 10 - 3 * numStr.length());
            g2d.drawString(numStr, x, 10);

            trayIcon.setImage(image);
            try {
                Thread.sleep(DISPLAY_SLEEP_TIME);
            } catch (InterruptedException e) {
                SimpleLogger.getInstance().log(e.getMessage());
            }
        }
    }

    /**
     * 打印结束, 图标重置
     */
    public void printOver(){
        printing = false;
        tray.num = 1;
        trayIcon.setImage(PRINT_LOGO);
    }
}
