package com.plough.nativeprint.utils;

/**
 * 与单位有关的工具类
 * Created by plough on 2019/2/26.
 */
public class UnitUtils {
//    mm = 25.4 * pixels / dpi;
//    pixels = (mm * dpi) / 25.4
    private static final int DEFAULT_DPI = 72;

    /**
     * 毫米转像素
     * @param mm 毫米数
     * @return double 像素值
     * @date 2019/2/26 4:09 PM
     */
    public static double MMtoPix(double mm) {
        return MMtoPix(mm, DEFAULT_DPI);
    }

    /**
     * 毫米转像素
     * @param mm 毫米数
     * @param dpi dpi
     * @return double 像素值
     * @date 2019/2/26 4:09 PM
     */
    public static double MMtoPix(double mm, int dpi) {
        return (mm * dpi) / 25.4;
    }
}
