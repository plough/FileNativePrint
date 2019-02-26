package com.plough.nativeprint.utils;


import com.plough.nativeprint.main.ServerConfig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * 输出log的工具类
 * Created by plough on 2018/7/12.
 */
public class SimpleLogger {
    private static final String LOG_PATH = "debug.log";
    private static SimpleLogger singleton;

    public static SimpleLogger getInstance() {
        if (singleton == null) {
            singleton = new SimpleLogger();
        }
        return singleton;
    }

    private SimpleLogger() {
    }

    public void log(String text) {
        if (!ServerConfig.getInstance().isLogEnabled()) {
            return;
        }
        try {
            // FileWriter 不支持设置 UTF-8
            OutputStreamWriter fw = new OutputStreamWriter(
                    new FileOutputStream(LOG_PATH, true),
                    Charset.forName("UTF-8").newEncoder()
            );
            String content = new Date() + ":  " + text + "\n";
            System.out.println(content);
            fw.append(content);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
