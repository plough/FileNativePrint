package com.plough.nativeprint.main;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plough.nativeprint.main.fileprint.PrintConfig;
import com.plough.nativeprint.serversocket.PrintClientServer;
import com.plough.nativeprint.utils.CommonUtils;
import com.plough.nativeprint.utils.EnvUtils;
import com.plough.nativeprint.utils.SimpleLogger;
import com.plough.nativeprint.utils.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * 打印机配置属性
 * Created by plough on 2019/2/25.
 */
public class ServerConfig {

    // 配置文件放到用户主目录下，升级软件后可以复用
    private static final String CONFIG_FILE_PATH = CommonUtils.pathJoin(EnvUtils.getEnvHome(), "print.config");

    private static final class ConfigHolder {
        private static ServerConfig serverConfig = readFromFile();
    }

    private String printerName = StringUtils.EMPTY;
    private int copy;
    private int orientation;
    private String paperSizeText;
    private float marginTop;
    private float marginLeft;
    private float marginBottom;
    private float marginRight;
    private boolean quietPrint = false;
    private boolean logEnabled = false;
    private String index;  // 页码范围
    private boolean fitPaperSize = false;
    private int scalePercent = 100;

    private boolean httpsMode = false;
    private int printPort = 9092;

    private ServerConfig() {
    }

    public static ServerConfig getInstance() {
        return ConfigHolder.serverConfig;
    }

    /**
     * 恢复为默认配置，取消静默
     */
    static void clearConfig() {
        ConfigHolder.serverConfig = new ServerConfig();
        getInstance().saveFile();
    }

    /**
     * 重新读取配置文件
     */
    static void reloadConfig() {
        boolean httpsMode = getInstance().isHttpsMode();

        ConfigHolder.serverConfig = readFromFile();

        if (httpsMode != getInstance().isHttpsMode()) {
            PrintClientServer.getInstance().restart();
        }
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public int getCopy() {
        return copy;
    }

    public void setCopy(int copy) {
        this.copy = copy;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public String getPaperSizeText() {
        return paperSizeText;
    }

    public void setPaperSizeText(String paperSizeText) {
        this.paperSizeText = paperSizeText;
    }

    public void setMargin(float top, float left, float bottom, float right) {
        this.marginTop = top;
        this.marginLeft = left;
        this.marginBottom = bottom;
        this.marginRight = right;
    }


    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public float getMarginRight() {
        return marginRight;
    }

    // 传给前台的值。实际打印的时候，根据 json 对象来判断是否为使用静默配置
    public boolean isQuietPrint() {
        return quietPrint;
    }

    public void setQuietPrint(boolean quietPrint) {
        this.quietPrint = quietPrint;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public boolean isFitPaperSize() {
        return fitPaperSize;
    }

    public void setFitPaperSize(boolean fitPaperSize) {
        this.fitPaperSize = fitPaperSize;
    }

    public int getScalePercent() {
        return scalePercent;
    }

    public void setScalePercent(int scalePercent) {
        this.scalePercent = scalePercent;
    }

    public boolean isHttpsMode() {
        return httpsMode;
    }

    public void setHttpsMode(boolean httpsMode) {
        this.httpsMode = httpsMode;
    }

    public int getPrintPort() {
        return printPort;
    }

    public void setPrintPort(int printPort) {
        this.printPort = printPort;
    }

    private boolean saveFile() {
        File outFile = new File(CONFIG_FILE_PATH);
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static ServerConfig readFromFile() {
        File inFile = new File(CONFIG_FILE_PATH);
        ServerConfig serverConfig = new ServerConfig();
        if (inFile.exists()) {
            try {
                serverConfig = readFromFile(inFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return serverConfig;
    }

    /**
     * 读取配置的时候需要容错，最好手写。写入配置的时候，直接写对象 Mapper 就好了
     * @date 2018/10/26 5:18 PM
     */
    private static ServerConfig readFromFile(File inFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(inFile);
        ServerConfig serverConfig = new ServerConfig();

        Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields();
        while (it.hasNext())
        {
            Map.Entry<String, JsonNode> entry = it.next();
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();
            if ("printerName".equals(fieldName)) {
                serverConfig.printerName = fieldValue.asText();
            } else if ("copy".equals(fieldName)) {
                serverConfig.copy = fieldValue.asInt();
            } else if ("orientation".equals(fieldName)) {
                serverConfig.orientation = fieldValue.asInt();
            } else if ("paperSizeText".equals(fieldName)) {
                serverConfig.paperSizeText = fieldValue.asText();
            } else if ("marginTop".equals(fieldName)) {
                serverConfig.marginTop = (float) fieldValue.asDouble();
            } else if ("marginLeft".equals(fieldName)) {
                serverConfig.marginLeft = (float) fieldValue.asDouble();
            } else if ("marginBottom".equals(fieldName)) {
                serverConfig.marginBottom = (float) fieldValue.asDouble();
            } else if ("marginRight".equals(fieldName)) {
                serverConfig.marginRight = (float) fieldValue.asDouble();
            } else if ("quietPrint".equals(fieldName)) {
                serverConfig.quietPrint = fieldValue.asBoolean();
            } else if ("logEnabled".equals(fieldName)) {
                serverConfig.logEnabled = fieldValue.asBoolean();
            } else if ("index".equals(fieldName)) {
                serverConfig.index = fieldValue.asText();
            } else if ("fitPaperSize".equals(fieldName)) {
                serverConfig.fitPaperSize = fieldValue.asBoolean();
            } else if ("scalePercent".equals(fieldName)) {
                serverConfig.scalePercent = fieldValue.asInt();
            } else if ("httpsMode".equals(fieldName)) {
                serverConfig.httpsMode = fieldValue.asBoolean();
            } else if ("printPort".equals(fieldName)) {
                serverConfig.printPort = fieldValue.asInt();
            }
        }

        return serverConfig;
    }

    public static void update(JSONObject jo) {
        ServerConfig config = getInstance();
        if (!jo.has("printerName")) {
            return;  // 静默打印，使用原来的配置
        }
        config.setPrinterName(jo.optString("printerName"));
        config.setCopy(jo.optInt("copy"));
        config.setOrientation(jo.optInt("orientation"));
        config.setPaperSizeText(jo.optString("paperSize"));

        float top = (float)jo.optDouble("marginTop");
        float left = (float)jo.optDouble("marginLeft");
        float bottom = (float)jo.optDouble("marginBottom");
        float right = (float)jo.optDouble("marginRight");
        config.setMargin(top, left, bottom, right);

        config.setQuietPrint(jo.optBoolean("quietPrint"));
        config.setIndex(jo.optString("index"));

        config.setFitPaperSize(jo.optBoolean("fitPaperSize"));
        config.setScalePercent(jo.optInt("scalePercent", 100));

        // 保存到配置文件中
        config.saveFile();
    }

    public JSONObject createJSONConfig() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("printerName", this.getPrinterName());
            jo.put("copy", this.getCopy());
            jo.put("orientation", this.getOrientation());
            jo.put("paperSize", this.getPaperSizeText());
            jo.put("marginTop", this.getMarginTop());
            jo.put("marginLeft", this.getMarginLeft());
            jo.put("marginBottom", this.getMarginBottom());
            jo.put("marginRight", this.getMarginRight());
            jo.put("quietPrint", this.isQuietPrint());
            jo.put("index", this.getIndex());
            jo.put("fitPaperSize", this.isFitPaperSize());
            jo.put("scalePercent", this.getScalePercent());
        } catch (JSONException e) {
            SimpleLogger.getInstance().log(e.getMessage());
        }
        return jo;
    }

    public static void update(PrintConfig pc) {
        ServerConfig config = getInstance();

        config.setPrinterName(pc.getPrinterName());
        config.setCopy(pc.getCopy());
        config.setOrientation(pc.getOrientation());
        config.setPaperSizeText(pc.getPaperSizeText());

        float top = (float)pc.getMarginTop();
        float left = (float)pc.getMarginLeft();
        float bottom = (float)pc.getMarginBottom();
        float right = (float)pc.getMarginRight();
        config.setMargin(top, left, bottom, right);

        config.setQuietPrint(pc.isQuietPrint());
        config.setIndex(pc.getIndex());

        // 保存到配置文件中
        config.saveFile();
    }

    @Override
    public String toString() {
        String template = "printerName: %s;\ncopy: %d;\norientation: %d;\npaperSizeText:%s;\n" +
                "margin(t-l-b-r): (%.2f, %.2f, %.2f, %.2f);\nquietPrint: %b;\nlogEnabled: %b;\nindex: %s;\n" +
                "fitPaperSize: %b;\nscalePercent: %d;\nhttpsMode: %b;\nprintPort: %d;";
        return String.format(template, printerName, copy, orientation, paperSizeText, marginTop, marginLeft,
                marginBottom, marginRight, quietPrint, logEnabled, index, fitPaperSize, scalePercent,
                httpsMode, printPort);
    }
}
