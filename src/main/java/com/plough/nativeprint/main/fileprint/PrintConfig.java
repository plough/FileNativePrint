package com.plough.nativeprint.main.fileprint;


import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.plough.nativeprint.main.ServerConfig;
import com.plough.nativeprint.utils.UnitUtils;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by plough on 2018/5/9.
 */
public class PrintConfig {
    private double width;  // 单位: px
    private double height;  // 单位: px
    private String paperSizeText;
    private float marginTop;  // 单位: mm
    private float marginLeft;  // 单位: mm
    private float marginBottom;  // 单位: mm
    private float marginRight;  // 单位: mm
    private int orientation;
    private String index;
    private int copy;
    private String printerName;
    private int reportTotalPage;
    private boolean quietPrint;

    private PrintConfig(JSONObject data) {
        init(data);
    }

    private void init(JSONObject data) {
        String paperSize = data.optString("paperSize", "A4");
        initPaperSize(paperSize);

        this.marginTop = (float) data.optDouble("marginTop", 0);
        this.marginLeft = (float) data.optDouble("marginLeft", 0);
        this.marginBottom = (float) data.optDouble("marginBottom", 0);
        this.marginRight = (float) data.optDouble("marginRight", 0);
        this.orientation = data.optInt("orientation", 0);
        this.index = data.optString("index");
        this.copy = data.optInt("copy", 1);
        this.printerName = data.optString("printerName");
        this.reportTotalPage = data.optInt("reportTotalPage", 1);
        this.quietPrint = data.optBoolean("quietPrint", false);
    }

    private void initPaperSize(String paperSize) {
        this.paperSizeText = paperSize.trim();

        // 如果是自定义尺寸，需要毫米转像素
        // 格式为：[100.5, 210]
        Pattern r = Pattern.compile("\\[\\s*(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)\\s*\\]");
        Matcher m = r.matcher(paperSizeText);
        if (m.matches()) {
            this.width = UnitUtils.MMtoPix(Float.parseFloat(m.group(1)));
            this.height = UnitUtils.MMtoPix(Float.parseFloat(m.group(3)));
            return;
        }

        Rectangle rect = PageSize.getRectangle(paperSizeText);
        this.width = rect.getWidth();
        this.height = rect.getHeight();
    }

    public static PrintConfig load(JSONObject data) {
        return new PrintConfig(data);
    }

    public static PrintConfig loadFromServer() {
        PrintConfig pc = new PrintConfig(new JSONObject());
        ServerConfig sc = ServerConfig.getInstance();

        Rectangle rect = PageSize.getRectangle(sc.getPaperSizeText());
        pc.paperSizeText = sc.getPaperSizeText();
        pc.width = rect.getWidth();
        pc.height = rect.getHeight();
        pc.marginTop = sc.getMarginTop();
        pc.marginLeft = sc.getMarginLeft();
        pc.marginBottom = sc.getMarginBottom();
        pc.marginRight = sc.getMarginRight();
        pc.orientation = sc.getOrientation();
        pc.index = sc.getIndex();
        pc.copy = sc.getCopy();
        pc.printerName = sc.getPrinterName();
        pc.quietPrint = sc.isQuietPrint();

        return pc;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getMarginTop() {
        return marginTop;
    }

    public int getMarginTopPix() {
        return (int) UnitUtils.MMtoPix(marginTop);
    }

    public double getMarginLeft() {
        return marginLeft;
    }

    public int getMarginLeftPix() {
        return (int) UnitUtils.MMtoPix(marginLeft);
    }

    public double getMarginBottom() {
        return marginBottom;
    }

    public int getMarginBottomPix() {
        return (int) UnitUtils.MMtoPix(marginBottom);
    }

    public double getMarginRight() {
        return marginRight;
    }

    public int getMarginRightPix() {
        return (int) UnitUtils.MMtoPix(marginRight);
    }

    public int getOrientation() {
        return orientation;
    }

    public String getIndex() {
        return index;
    }

    public int getCopy() {
        return copy;
    }

    public String getPrinterName() {
        return printerName;
    }

    public int getReportTotalPage() {
        return reportTotalPage;
    }

    public void setReportTotalPage(int reportTotalPage) {
        this.reportTotalPage = reportTotalPage;
    }

    public String getPaperSizeText() {
        return paperSizeText;
    }

    public boolean isQuietPrint() {
        return quietPrint;
    }
}