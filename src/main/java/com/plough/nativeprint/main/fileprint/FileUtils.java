package com.plough.nativeprint.main.fileprint;

import com.google.common.io.Files;
import com.plough.nativeprint.utils.SimpleLogger;
import com.plough.nativeprint.utils.StringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Created by plough on 2018/5/9.
 */
public final class FileUtils {

    /**
     * 确保文件存在本地，并返回路径。不存在则返回空串
     * */
    public static String makeSureExistAndReturnPath(String fileUrl, String fileName) {
        if (isLocalFilePath(fileUrl)) {
            SimpleLogger.getInstance().log("检测到是本地文件路径");
            return fileUrl;
        }
        if (!isCacheFileExist(fileName)) {
            SimpleLogger.getInstance().log("不在缓存中，下载到本地");
            downloadFile(fileUrl, fileName);
        }
        // 确保文件存在
        if (isCacheFileExist(fileName)) {
            return getFilePath(fileName);
        }

        SimpleLogger.getInstance().log("指定文件不存在");
        return StringUtils.EMPTY;
    }

    /**
     * 判断名为 fileName 的文件是否存在于缓存目录中
     * */
    public static boolean isCacheFileExist(String fileName) {
        return isFileExist(getFilePath(fileName));
    }

    public static boolean isFileExist(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 判断是否为 pdf 格式
     * */
    public static boolean isPDF(String filePath) {
        try {
            String line = Files.readFirstLine(new File(filePath), StandardCharsets.UTF_8);
            return line.startsWith("%PDF"); // 所有pdf文件，第一行一定是 "%PDF" 开头
        } catch (IOException e) {
            SimpleLogger.getInstance().log(e.getMessage());
            return false;
        }
    }


    /**
     * 根据文件名判断是否为图片格式
     * */
    public static boolean isImg(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith("jpg") || fileName.endsWith("jpeg") || fileName.endsWith("gif") || fileName.endsWith("png");
    }

    /**
     * 从 url 解析出文件名
     * */
    public static String getFileName(String fileUrl) {
        try {
            fileUrl =  URLDecoder.decode(fileUrl,"UTF-8");
            String[] slices = fileUrl.split("/");
            return slices[slices.length - 1];
        } catch (UnsupportedEncodingException e) {
            SimpleLogger.getInstance().log(e.getMessage());
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取文件在磁盘上的路径
     * */
    public static String getFilePath(String fileName){
        File path = new File(System.getProperty("user.dir").concat("/downFile"));
        if (!path.exists() && !path.isDirectory()) {
            path.mkdir();
        }
        String filePath = path + "/" + fileName;
        return filePath;
    }

    /**
     * 判断 fileUrl 是否为本地文件的绝对路径
     * */
    public static boolean isLocalFilePath(String fileUrl) {
        return new File(fileUrl).isFile();
    }

    /**
     * 从指定 fileUrl 下载文件，并修改文件名为 fileName
     * */
    public static boolean downloadFile(String fileUrl, String fileName) {
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(getFilePath(fileName)));
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            SimpleLogger.getInstance().log(e.getMessage());
            return false;
        }
    }
}
