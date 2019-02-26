//package com.plough.nativeprint.main;
//import com.plough.nativeprint.serversocket.PrintClientServer;
//import com.plough.nativeprint.utils.SimpleLogger;
//import com.plough.nativeprint.utils.StringUtils;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import javax.swing.JOptionPane;
//import javax.swing.UIManager;
//import java.awt.Graphics;
//import java.awt.print.PageFormat;
//import java.awt.print.PrinterException;
//import java.io.IOException;
//import java.lang.reflect.Method;
//import java.net.URL;
//import java.net.URLDecoder;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.regex.Pattern;
//
///**
// * 本地打印跟服务器交互的家伙
// *
// * Created by Administrator on 2016/2/18 0018.
// */
//public class NativePrint {
//
//    private static final String PREFIX = "fineprintv4:";
//    private static final String ODD = "odd";  // 奇数页
//    private static final String EVEN = "even";  // 偶数页
//    private static final int DOUBLE_LEAP = 2;  // 双面打印，迭代的间隔
//    private static final double HUNDRED = 100.0;
//    private static NativePrint singleton;
//
//    private NativePrint() {
//    }
//
//    public static NativePrint getInstance() {
//        if (singleton == null) {
//            singleton = new NativePrint();
//        }
//        return singleton;
//    }
//
//    public void printWithArgs(String[] args) {
//        if (args.length == 0){
//            return;
//        }
//
//        if (args.length > 0) {
//            JSONObject jo = null;
//            if (args.length == 1) {
//                jo = getJsonObjectForModernBrowser(args[0], jo);
//            } else {
//                jo = getJsonObjectForIE(args, jo);
//            }
//
//            if (jo == null || StringUtils.isEmpty(jo.toString())) {
//                return;
//            }
//
//            printWithJsonArg(jo);
//        }
//    }
//
//    public void printWithJsonArg(JSONObject jo) {
//        printWithJsonArg(jo, null);
//    }
//
//    public void printWithJsonArg(JSONObject jo, UUID clientId) {
//        PrintTray.getInstance().markPrintingTray();
//        if (StringUtils.isEmpty(jo.optString("isShowDialog"))) {  // 新本地打印
//            SimpleLogger.getInstance().log("新本地打印");
//            init(jo, clientId);
//        }
//        PrintTray.getInstance().printOver();
//    }
//
//
//    private JSONObject getJsonObjectForModernBrowser(String arg, JSONObject jo) {
//        SimpleLogger.getInstance().log("arg: " + arg);
//        String encodeJsonContent = arg.substring(PREFIX.length());
//        try {
//            String decodeJsonContent = URLDecoder.decode(encodeJsonContent, EncodeConstants.ENCODING_UTF_8);
//            jo = new JSONObject(decodeJsonContent);
//        } catch (Exception e) {
//            SimpleLogger.getInstance().log(e.getMessage() + encodeJsonContent);
//        }
//        return jo;
//    }
//
//    //ie比较奇葩, 外面套了两层encode, 还是被自动解码了, 还把分号去掉了, 只能重新构建json
//    private JSONObject getJsonObjectForIE(String[] args, JSONObject jo) {
//        int len = args.length;
//        StringBuffer fullOriContent = new StringBuffer();
//
//        try {
//            for (int i = 0; i < len; i++) {
//                fullOriContent.append(args[i]);
//            }
//            jo = new JSONObject();
//
//            String jsonContent = fullOriContent.substring(PREFIX.length() + 1, fullOriContent.lastIndexOf("}"));
//            String[] paraArray = jsonContent.split(",");
//
//            for (String temp : paraArray) {
//                int dotIndex = temp.indexOf(":");
//                if (dotIndex == -1) {
//                    continue;
//                }
//
//                String key = temp.substring(0, dotIndex);
//                String value = temp.substring(dotIndex + 1, temp.length());
//                jo.put(key, value);
//            }
//        } catch (Exception e){
//            FineLoggerFactory.getLogger().error(e.getMessage() + fullOriContent);
//            PrintTray.getInstance().printOver();
//        }
//        return jo;
//    }
//
//    private void init(JSONObject jo, UUID clientId) {
//        try{
//            String url = jo.optString("url");
//            if (StringUtils.isEmpty(url)) {
//                SimpleLogger.getInstance().log("url is empty，return");
//                return;
//            }
//
//            SimpleLogger.getInstance().log("url: " + url);
//            URL servletURL = new URL(url);
//            printAsApplet(jo, servletURL, clientId);
//        } catch(Exception e) {
//            JOptionPane.showMessageDialog(null, " e=" + e.getMessage());
//        }
//    }
//
//    public static void main(String[] args) throws IOException, PrinterException, JSONException {
//        final JSONObject jo = new JSONObject();
//        jo.put("sessionID", "98644");
//        jo.put("printerName", "testprinter");
//        jo.put("copy", "1");
//        jo.put("index", "");
//        jo.put("orientation", "0");
//        jo.put("paperSize", "171.5,400");
//        jo.put("marginTop", "6.85");
//        jo.put("marginLeft", "6.85");
//        jo.put("marginBottom", "6.85");
//        jo.put("marginRight", "6.85");
//        jo.put("quietPrint", "true");
//        jo.put("url", "http://localhost:8075/WebReport/ReportServer?sessionID=98644&op=fr_applet&cmd=print");
//
//        ExecutorService service = Executors.newFixedThreadPool(5);
//        final NativePrint print = new NativePrint();
//        for (int i = 0; i < 1; i++) {
//            final int finalI = i;
//            service.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Thread.sleep(finalI * 100L);
//                        print.printAsApplet(jo, new URL(jo.optString("url")));
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//    }
//
//    private void printAsApplet(JSONObject jo, URL servletURL) throws Exception {
//        printAsApplet(jo, servletURL, null);
//    }
//
//    private void printAsApplet(JSONObject jo, URL servletURL, UUID clientId) throws Exception {
//        SimpleLogger.getInstance().log("in printAsApplet");
//
//        //neil: isShowDialog值为true则显示选择打印机窗口，false则默认直接打印
//        boolean isShowDialog = false;  // 不显示
//        boolean isSingleSheet = jo.optBoolean("isSingleSheet");
//        String index = jo.optString("index");
//
//        java.io.InputStream input = servletURL.openStream();
//        PageXmlProvider xmlUtils = StableFactory.getMarkedInstanceObjectFromClass(PageXmlProvider.XML_TAG, PageXmlProvider.class);
//        BaseSinglePagePrintable[] allPages = xmlUtils.deXmlizable4SinglePage(input);
//
//        SimpleLogger.getInstance().log("allPages.length: " + allPages.length);
//
//        BaseSingleReportCache pageCache = StableFactory.getMarkedInstanceObjectFromClass(BaseSingleReportCache.XML_TAG, BaseSingleReportCache.class);
//        //根据index来初始化需要打印的页面
//        BaseSinglePagePrintable[] pages = validIndex(index, allPages, servletURL, pageCache);
//
//        int copy;
//        String printerName;
//        synchronized (ServerConfig.getInstance()) {
//            // 更新服务器配置
//            ServerConfig.update(jo);
//
//            // 更新页面设置
//            updatePageSetting(pages);
//
//            copy = ServerConfig.getInstance().getCopy();
//            printerName = ServerConfig.getInstance().getPrinterName();
//        }
//
//        //设置打印份数
//        pages = setUpCopies(copy, pages);
//        //设置页面缓存
//        cachePaperSetting(isSingleSheet, pages);
//
//        if (clientId != null) {
//            PrintClientServer.getInstance().onBeforePrint(clientId);
//        }
//        SimpleLogger.getInstance().log("before real print");
//        //开始真正的打印过程
//        print(isShowDialog, printerName, pages);
//
//        SimpleLogger.getInstance().log("print completed");
//        //清下applet打印用到的reportpage缓存
//        pageCache.clearReportPageCache();
//    }
//
//    private void updatePageSetting(BaseSinglePagePrintable[] pages) {
//        ServerConfig config = ServerConfig.getInstance();
//        // 更新页面设置
//        for (BaseSinglePagePrintable page : pages) {
//
//            PaperSettingProvider paperSettingProvider = page.getPaperSetting();
//            paperSettingProvider.setOrientation(config.getOrientation());
//
//            String paperSizeText = config.getPaperSizeText();
//            PaperSize paperSize = new PaperSize();  // 给一个默认值
//            if (paperSizeText.contains(",")) {  // 直接指定宽、高
//                String[] paperSizeArr = paperSizeText.split(",\\s*");
//                UNIT width = new MM(Float.parseFloat(paperSizeArr[0]));
//                UNIT height = new MM(Float.parseFloat(paperSizeArr[1]));
//                paperSize = new PaperSize(width, height);
//            } else {  // 指定名称
//                for (int i = 0; i < ReportConstants.PaperSizeNameSizeArray.length; i++) {
//                    if (ReportConstants.PaperSizeNameSizeArray[i][0].toString().equals(paperSizeText)) {
//                        paperSize = (PaperSize) ReportConstants.PaperSizeNameSizeArray[i][1];
//                        break;
//                    }
//                }
//            }
//            paperSettingProvider.setPaperSize(paperSize);
//
//            UNIT top = new MM(config.getMarginTop());
//            UNIT left = new MM(config.getMarginLeft());
//            UNIT bottom = new MM(config.getMarginBottom());
//            UNIT right = new MM(config.getMarginRight());
//            Margin newMargin = new Margin(top, left, bottom, right);
//            paperSettingProvider.setMargin(newMargin);
//
//            page.setScaleFactor(config.getScalePercent() / HUNDRED);
//        }
//    }
//
//    private BaseSinglePagePrintable[] setUpCopies(int copies, BaseSinglePagePrintable[] pages){
//        if(copies <= 1){
//            return pages;
//        }
//        List<BaseSinglePagePrintable> new_list = new ArrayList<BaseSinglePagePrintable>();
//        List<BaseSinglePagePrintable> cur_list = Arrays.asList(pages);
//        for (int i = 0; i < copies; i++) {
//            new_list.addAll(cur_list);
//        }
//
//        return new_list.toArray(new BaseSinglePagePrintable[new_list.size()]);
//    }
//
//
//    private BaseSinglePagePrintable[] validIndex(String index, BaseSinglePagePrintable[] allPages, URL servletURL,
//                                                 BaseSingleReportCache pageCache){
//        if (StringUtils.isEmpty(index)) {
//            // 全部打印
//            return initIndexedPages(allPages.length, 0, allPages, servletURL, pageCache);
//        }
//
//        //指定页码打印，如：1-2 ，3-5
//        if (index.indexOf("-") != -1) {
//            Pattern pattern = Pattern.compile("-");
//            String[] strs = pattern.split(index);
//            int startIndex = Integer.parseInt(strs[0]) - 1;
//            int endIndex = Integer.parseInt(strs[1]) - 1;
//
//            if(startIndex > allPages.length - 1 || endIndex > allPages.length - 1){
//                JOptionPane.showMessageDialog(null, "页面索引不正确.");
//                return allPages;
//            }
//
//            if(startIndex >= 0 && endIndex >= startIndex){
//                int pageLength = endIndex - startIndex + 1;
//                return initIndexedPages(pageLength, startIndex, allPages, servletURL, pageCache);
//            }
//        }
//
//        if (isInteger(index)) {
//            //单页打印，如：2,5,7
//            int printIndex = Integer.parseInt(index) -1;
//            if(printIndex > allPages.length - 1 ){
//                JOptionPane.showMessageDialog(null, "页面索引不正确.");
//                return allPages;
//            }
//
//            return initSingleIndexedPages(printIndex, allPages, servletURL, pageCache);
//        }
//
//        // 打印奇数页
//        if (ODD.equals(index)){
//            return initIndexedPages(allPages.length, 0, allPages, servletURL, pageCache, DOUBLE_LEAP);
//        } else if (EVEN.equals(index)) {  // 打印偶数页
//            return initIndexedPages(allPages.length, 1, allPages, servletURL, pageCache, DOUBLE_LEAP);
//        }
//
//        return allPages;
//    }
//
//    private void cachePaperSetting(boolean isSingleSheet, BaseSinglePagePrintable[] pages){
//        if (pages.length > 0 && isSingleSheet) {
//            for(int i = 0; i< pages.length; i++) {
//                if (i == 0) {
//                    continue;
//                }
//                pages[i].setReadreportsettings(false);
//            }
//        }
//    }
//
//    private BaseSinglePagePrintable[] initSingleIndexedPages(int index, BaseSinglePagePrintable[] allPages, URL servletURL, BaseSingleReportCache pageCache) {
//        return new BaseSinglePagePrintable[] {
//                getSinglePagePrintable(index, allPages, servletURL, pageCache)
//        };
//    }
//
//    private BaseSinglePagePrintable[] initIndexedPages(int len, int startIndex, BaseSinglePagePrintable[] allPages, URL servletURL, BaseSingleReportCache pageCache){
//        return initIndexedPages(len, startIndex, allPages, servletURL, pageCache, 1);
//    }
//
//    private BaseSinglePagePrintable[] initIndexedPages(int len, int startIndex, BaseSinglePagePrintable[] allPages, URL servletURL, BaseSingleReportCache pageCache, int leap){
//        SimpleLogger.getInstance().log("initIndexedPages len = " + len);
//        List<BaseSinglePagePrintable> pageList = new ArrayList<>();
//
//        for (int j = 0 ;j < len ; j+=leap) {
//            BaseSinglePagePrintable page = getSinglePagePrintable(j + startIndex, allPages, servletURL, pageCache);
//            pageList.add(page);
//        }
//
//        // 总数为奇数的模板，打印偶数页最后应多出一张空白页
//        if (startIndex == 1 && len > 1 && len % 2 != 0) {
//            pageList.add(new SinglePagePrintable(allPages[1].getPaperSetting()) {
//                @Override
//                public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
//                    return 0;
//                }
//            });
//        }
//
//        return pageList.toArray(new BaseSinglePagePrintable[pageList.size()]);
//    }
//
//    private BaseSinglePagePrintable getSinglePagePrintable(int index, BaseSinglePagePrintable[] allPages, URL servletURL, BaseSingleReportCache pageCache) {
//        PaperSettingProvider ps = allPages[index].getPaperSetting();
//        HashMap<String, Class> pageClass = new HashMap<>();
//        pageClass.put(Constants.ARG_1, PaperSettingProvider.class);
//        pageClass.put(Constants.ARG_3, BaseSingleReportCache.class);
//        Object[] paraValue = new Object[]{servletURL, ps, index, pageCache};
//        return StableFactory.getMarkedInstanceObjectFromClass(BaseSinglePagePrintable.XML_TAG,
//                paraValue, pageClass, BaseSinglePagePrintable.class);
//    }
//
//    private void startModule(String moduleName){
//        try {
//            Class c = Class.forName(moduleName);
//            Method m = c.getMethod("init", null);
//            m.invoke(c.newInstance(), new Object[0]);
//        } catch (Exception e1) {
//            //do nth 可能有一种情况就是反射那边某个类出现了问题, 但是不影响
//        }
//    }
//
//    //判断从网页传过来的index是否是正整数
//    private boolean isInteger(String value) {
//        if(StringUtils.isEmpty(value)){
//            return false;
//        }
//
//        Pattern p = Pattern.compile("^[1-9]\\d*$");
//        return p.matcher(value).matches();
//    }
//
//    /**
//     * 开始打印
//     *
//     * @param isShowDialog 是否显示设置界面
//     *
//     */
//    private void print(boolean isShowDialog, BaseSinglePagePrintable[] pages) {
//        print(isShowDialog, null, pages);
//    }
//
//    /**
//     * 开始打印
//     *
//     * @param isShowDialog 是否显示设置界面
//     * @param printerName 打印机名称
//     *
//     */
//    private void print(boolean isShowDialog, String printerName, BaseSinglePagePrintable[] pages) {
//        SimpleLogger.getInstance().log("pages != null ? " + (pages != null));
//        if(pages != null) {
//            try {
//                SimpleLogger.getInstance().log("PrintUtils.print: isShowDialog: " + isShowDialog + " printerName: " + printerName);
//                PrintUtils.print(new SinglePageSet(pages), isShowDialog, printerName);
//            } catch (Exception e) {
//                SimpleLogger.getInstance().log("Exception: " + e.getMessage());
//                if (StringUtils.isEmpty(e.getMessage())) {
//                    return;
//                }
//                FineLoggerFactory.getLogger().error(e.getMessage(), e);
//                JOptionPane.showMessageDialog(null, e.getMessage());
//            }
//        }
//    }
//}
//
//
