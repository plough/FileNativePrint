package com.plough.nativeprint.main.fileprint;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * Created by plough on 2018/5/9.
 */
class ImgPrintable implements Printable {
    private ImageIcon printImage;

    ImgPrintable(String filePath) {
        this.printImage = new ImageIcon(filePath);
    }

    public int print(Graphics g, PageFormat pf, int pageIndex) {
        Graphics2D g2d = (Graphics2D) g;
        g.translate((int) (pf.getImageableX()), (int) (pf.getImageableY()));

        double pageWidth = pf.getImageableWidth();
        double pageHeight = pf.getImageableHeight();
        double imageWidth = printImage.getIconWidth();
        double imageHeight = printImage.getIconHeight();

        if (imageWidth > pageWidth || imageHeight > pageHeight) {
            double scaleX = pageWidth / imageWidth;
            double scaleY = pageHeight / imageHeight;
            double scaleFactor = Math.min(scaleX, scaleY);
            g2d.scale(scaleFactor, scaleFactor);
        }

        int x = (int)(pageWidth - imageWidth) / 2;
        int y = (int)(pageHeight - imageHeight) / 2;

        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }

        // 图片总是在绘制区域居中
        g.drawImage(printImage.getImage(), x, y, null);
        return Printable.PAGE_EXISTS;
    }
}