package com.plough.nativeprint.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by plough on 2019/2/25.
 */
public class IOUtils {
    public static BufferedImage readImage(String filePath) {
        BufferedImage image;
        try {
            image = ImageIO.read(new FileInputStream(new File(transformPath(filePath))));
        } catch (IOException e) {
            image = new BufferedImage(0, 0, BufferedImage.TYPE_INT_RGB);
        }
        return image;
    }

    private static String transformPath(String resourcePath) {
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        return IOUtils.class.getClassLoader().getResource(resourcePath).getFile();
    }
}
