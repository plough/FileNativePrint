package com.plough.nativeprint.utils;


import com.plough.nativeprint.main.PrintConstants;

import java.io.File;

/**
 * Created by plough on 2018/11/26.
 */
public class EnvUtils {
    public static String getEnvHome() {
        return getEnvHomeByAppName(PrintConstants.APP_NAME);
    }

    private static String getEnvHomeByAppName(String appName) {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            userHome = System.getProperty("userHome");
        }

        File envHome = new File(userHome + File.separator + "." + appName);
        if (!envHome.exists()) {
            mkdirs(envHome);
        }

        return envHome.getAbsolutePath();
    }

    private static boolean mkdirs(File file) {
        return file != null && (file.exists() || file.mkdirs());
    }
}
