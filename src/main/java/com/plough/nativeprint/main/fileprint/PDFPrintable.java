package com.plough.nativeprint.main.fileprint;

/**
 * Created by plough on 2018/5/11.
 */
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterIOException;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.rendering.PDFRenderer;

public final class PDFPrintable implements Printable {
    private final PDDocument document;
    private final PDFRenderer renderer;
    private final boolean showPageBorder;
    private final Scaling scaling;
    private final float dpi;
    private final boolean center;

    public PDFPrintable(PDDocument document) {
        this(document, Scaling.SHRINK_TO_FIT);
    }

    public PDFPrintable(PDDocument document, Scaling scaling) {
        this(document, scaling, false, 0.0F);
    }

    public PDFPrintable(PDDocument document, Scaling scaling, boolean showPageBorder) {
        this(document, scaling, showPageBorder, 0.0F);
    }

    public PDFPrintable(PDDocument document, Scaling scaling, boolean showPageBorder, float dpi) {
        this(document, scaling, showPageBorder, dpi, true);
    }

    public PDFPrintable(PDDocument document, Scaling scaling, boolean showPageBorder, float dpi, boolean center) {
        this.document = document;
        this.renderer = new PDFRenderer(document);
        this.scaling = scaling;
        this.showPageBorder = showPageBorder;
        this.dpi = dpi;
        this.center = center;
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        pageIndex %= this.document.getNumberOfPages();
        if(pageIndex >= 0 && pageIndex < this.document.getNumberOfPages()) {
            try {
                Graphics2D e = (Graphics2D)graphics;
                PDPage page = this.document.getPage(pageIndex);
                PDRectangle cropBox = getRotatedCropBox(page);
                double imageableWidth = pageFormat.getImageableWidth();
                double imageableHeight = pageFormat.getImageableHeight();
                double scale = 1.0D;
                if(this.scaling != Scaling.ACTUAL_SIZE) {
                    double printerGraphics = imageableWidth / (double)cropBox.getWidth();
                    double transform = imageableHeight / (double)cropBox.getHeight();
                    scale = Math.min(printerGraphics, transform);
                    if(scale > 1.0D && this.scaling == Scaling.SHRINK_TO_FIT) {
                        scale = 1.0D;
                    }

                    if(scale < 1.0D && this.scaling == Scaling.STRETCH_TO_FIT) {
                        scale = 1.0D;
                    }
                }

                e.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                if(this.center) {
                    e.translate((imageableWidth - (double)cropBox.getWidth() * scale) / 2.0D, (imageableHeight - (double)cropBox.getHeight() * scale) / 2.0D);
                }

                Graphics2D printerGraphics1 = null;
                BufferedImage image = null;
                if(this.dpi > 0.0F) {
                    float transform1 = this.dpi / 72.0F;
                    image = new BufferedImage((int)(imageableWidth * (double)transform1 / scale), (int)(imageableHeight * (double)transform1 / scale), 2);
                    printerGraphics1 = e;
                    e = image.createGraphics();
                    printerGraphics1.scale(scale / (double)transform1, scale / (double)transform1);
                    scale = (double)transform1;
                }

                AffineTransform transform2 = (AffineTransform)e.getTransform().clone();
                e.setBackground(Color.WHITE);
                this.renderer.renderPageToGraphics(pageIndex, e, (float)scale);
                if(this.showPageBorder) {
                    e.setTransform(transform2);
                    e.setClip(0, 0, (int)imageableWidth, (int)imageableHeight);
                    e.scale(scale, scale);
                    e.setColor(Color.GRAY);
                    e.setStroke(new BasicStroke(0.5F));
                    graphics.drawRect(0, 0, (int)cropBox.getWidth(), (int)cropBox.getHeight());
                }

                if(printerGraphics1 != null) {
                    printerGraphics1.setBackground(Color.WHITE);
                    printerGraphics1.clearRect(0, 0, image.getWidth(), image.getHeight());
                    printerGraphics1.drawImage(image, 0, 0, (ImageObserver)null);
                    e.dispose();
                }

                return 0;
            } catch (IOException var17) {
                throw new PrinterIOException(var17);
            }
        } else {
            return 1;
        }
    }

    static PDRectangle getRotatedCropBox(PDPage page) {
        PDRectangle cropBox = page.getCropBox();
        int rotationAngle = page.getRotation();
        return rotationAngle != 90 && rotationAngle != 270?cropBox:new PDRectangle(cropBox.getLowerLeftY(), cropBox.getLowerLeftX(), cropBox.getHeight(), cropBox.getWidth());
    }

    static PDRectangle getRotatedMediaBox(PDPage page) {
        PDRectangle mediaBox = page.getMediaBox();
        int rotationAngle = page.getRotation();
        return rotationAngle != 90 && rotationAngle != 270?mediaBox:new PDRectangle(mediaBox.getLowerLeftY(), mediaBox.getLowerLeftX(), mediaBox.getHeight(), mediaBox.getWidth());
    }
}