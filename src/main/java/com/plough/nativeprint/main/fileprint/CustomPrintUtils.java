package com.plough.nativeprint.main.fileprint;

/**
 * Created by plough on 2018/5/6.
 */

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.plough.nativeprint.main.ServerConfig;
import com.plough.nativeprint.utils.ComparatorUtils;
import com.plough.nativeprint.utils.SimpleLogger;
import com.plough.nativeprint.utils.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PageRanges;
import javax.swing.JOptionPane;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public final class CustomPrintUtils
{
    /**
     * 供外部调用的打印接口
     * */
    public static void printWithJsonArg(JSONObject data) {
        try {
            List<String> fileUrlList = new ArrayList<>();
            JSONArray urls = data.optJSONArray("url");
            if (urls != null) {
                for (int i = 0; i < urls.length(); i++) {
                    fileUrlList.add(urls.get(i).toString());
                }
            } else {
                fileUrlList.add(data.optString("url"));
            }

            List<String> filePathList = getFilePathList(fileUrlList);

            for (String filePath : filePathList) {
                printSingleFile(data, filePath);
            }
        } catch (JSONException e) {
            SimpleLogger.getInstance().log(e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> getFilePathList(List<String> fileUrlList) {
        List<String> filePathList = new ArrayList<>();
        for (String fileUrl : fileUrlList) {
            SimpleLogger.getInstance().log("fileUrl: " + fileUrl);
            String fileName = FileUtils.getFileName(fileUrl);
            SimpleLogger.getInstance().log("fileName: " + fileName);
            String filePath = FileUtils.makeSureExistAndReturnPath(fileUrl, fileName);
            filePathList.add(filePath);
        }
        return filePathList;
    }

    private static void printSingleFile(JSONObject data, String filePath) {
        SimpleLogger.getInstance().log("filePath: " + filePath);
        if (FileUtils.isFileExist(filePath)) {
            PrintConfig config;
            if (ServerConfig.getInstance().isQuietPrint()) {
                SimpleLogger.getInstance().log("静默打印，从本地加载配置");
                config = PrintConfig.loadFromServer();
            } else {
                config = PrintConfig.load(data);
                // 更新打印配置
                ServerConfig.update(config);
            }
            doPrint(filePath, config);
        }
    }

    /**
     * 读取PDF文件信息，设置到 data 中
     * */
    public static void readConfigToData(JSONObject data, String customFileUrl) {
        try {
            // 图片、pdf都会在打印区域居中，默认不设置边距
            data.put("marginLeft", 0);
            data.put("marginRight", 0);
            data.put("marginTop", 0);
            data.put("marginBottom", 0);

            // Java 打印的纸张大小列表，跟报表 ReportConstants.PaperSizeNameSizeArray 里定义的纸张大小列表略有差异，所以要单独处理
            // 纸张大小列表
            JSONArray jaPaperSizeNames = new JSONArray();
            for (Field field : PageSize.class.getDeclaredFields()) {
                if (!"Rectangle".equals(field.getType().getSimpleName())) {
                    continue;
                }
                String paperSize = field.getName();
                JSONObject jo = new JSONObject();
                jo.put("text", paperSize);
                jo.put("value", paperSize);
                jaPaperSizeNames.put(jo);
            }
            data.put("paperSizeNames", jaPaperSizeNames);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!customFileUrl.toUpperCase().endsWith(".PDF")) {
            return;  // 目前只有 pdf 才需要读取打印格式
        }

        String fileName = FileUtils.getFileName(customFileUrl);
        String filePath = FileUtils.makeSureExistAndReturnPath(customFileUrl, fileName);
        if (FileUtils.isFileExist(filePath)) {
            try {
                PdfReader reader = new PdfReader(filePath);
                data.put("reportTotalPage", reader.getNumberOfPages());
                Rectangle mediabox = reader.getPageSize(1);
                float width = mediabox.getWidth();
                float height = mediabox.getHeight();
                data.put("paperSize", getPaperSize(width, height));
                data.put("orientation", getOrientation(width, height));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    // 获取打印机
    private static PrintService getPrintService(PrintConfig o) {
        PrintService ps = null;
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.AUTOSENSE, null);
        if(StringUtils.isNotEmpty(o.getPrinterName())) {
            for(int i = 0; i < printServices.length; ++i) {
                if(ComparatorUtils.equals(o.getPrinterName(), printServices[i].getName())) {
                    ps = printServices[i];
                    break;
                }
            }
        }

        if (ps == null) {
            ps = PrintServiceLookup.lookupDefaultPrintService();
        }

        if (ps == null) {
            if (printServices.length > 0) {
                ps = printServices[0];
            } else {
                JOptionPane.showMessageDialog(null, "no printer found!");
            }
        }

        return ps;
    }

    private static Printable getPrintable(String filePath, PrintConfig o) throws IOException {
        if (FileUtils.isPDF(filePath)) {
            PDDocument document = PDDocument.load(new File(filePath));
            o.setReportTotalPage(document.getNumberOfPages());
            return new PDFPrintable(document);
        }
        return new ImgPrintable(filePath);
    }

    private static void doPrint(String filePath, PrintConfig o) {
        try {
            PrintService ps = getPrintService(o);
            if (ps == null) {
                SimpleLogger.getInstance().log("打印服务不可用");
                return;
            }
            // 设置打印机
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setPrintService(ps);

            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();

            // 页码范围
            if (StringUtils.isNotEmpty(o.getIndex())) {
                String[] arr = o.getIndex().split("-");
                int start = Integer.parseInt(arr[0]);
                int end = arr.length > 1 ? Integer.parseInt(arr[1]) : start;
                PageRanges pageRng = new PageRanges(start , end);  // 闭区间 [start, end]
                pras.add(pageRng);
            }

            // 要使页码范围（以及一些pras的特有配置）生效，必须传入 pras。
            // TODO：目前发现 Mac 下的 PDFWriter 打印机，设置页码范围后（即pras有配置项），纸张大小会错乱。Windows 上测试没有问题，怀疑是 PDFWriter 虚拟打印机自身的问题
            PageFormat pageFormat = printerJob.getPageFormat(pras);
            // 设置页面横纵向
            pageFormat.setOrientation(o.getOrientation() == 0 ? PageFormat.PORTRAIT : PageFormat.LANDSCAPE);

            Paper paper = new Paper();
            // 纸张大小
            paper.setSize(o.getWidth(), o.getHeight());
            // 边距
            paper.setImageableArea(o.getMarginLeftPix(), o.getMarginTopPix(), o.getWidth() - (o.getMarginLeftPix() + o.getMarginRightPix()),
                    o.getHeight() - (o.getMarginTopPix() + o.getMarginBottomPix()));
            pageFormat.setPaper(paper);

            // 获取打印内容
            Printable printable = getPrintable(filePath, o);
            Book book = new Book();
            // 打印份数（Java 打印 api 设置份数不生效，干脆直接添加到同一个文档中）
            for (int i = 0; i < o.getCopy(); i++) {
                book.append(printable, pageFormat, o.getReportTotalPage());
            }

            printerJob.setPageable(book);

            printerJob.print();

        } catch (InvalidPasswordException e) {
            SimpleLogger.getInstance().log(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            SimpleLogger.getInstance().log(e.getMessage());
            e.printStackTrace();
        } catch (PrinterException e) {
            SimpleLogger.getInstance().log(e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getOrientation(float width, float height) {
        return width > height ? 1 : 0;
    }

    private static String getPaperSize(float width, float height) {
        if (getOrientation(width, height) == 1) {
            float tmp = width;
            width = height;
            height = tmp;
        }
        // 从 A10 找到 A0
        for (int i = 10; i >= 0; i--) {
            String paperSize = "A" + i;
            Rectangle rect = PageSize.getRectangle(paperSize);
            if (rect.getWidth() >= width && rect.getHeight() >= height) {
                return paperSize;
            }
        }
        return "A4";
    }

    public static void main(String[] args) {
//        String fileUrl = "http://o8k9160j9.bkt.clouddn.com/%E5%AE%89%E5%85%A8%E5%9C%A8%E4%B9%8B%E6%B0%B4%E5%8D%B0%E2%80%94%E2%80%942018.04.16.pdf";
        String fileUrl = "/Users/plough/Downloads/gettingstarted.pdf";
        String fileName = FileUtils.getFileName(fileUrl);
        String filePath = FileUtils.makeSureExistAndReturnPath(fileUrl, fileName);
        if (FileUtils.isFileExist(filePath)) {
            JSONObject data = new JSONObject();
            try {
                data.put("marginLeft", "0");
                data.put("marginTop", "0");

                data.put("paperSize", "A2");
                data.put("orientation", 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            PrintConfig config = PrintConfig.load(data);
            doPrint(filePath, config);
        }
    }
}
