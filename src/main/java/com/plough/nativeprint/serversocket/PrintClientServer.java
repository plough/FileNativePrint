package com.plough.nativeprint.serversocket;


import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.plough.nativeprint.main.PrintConstants;
import com.plough.nativeprint.main.ServerConfig;
import com.plough.nativeprint.main.fileprint.CustomPrintUtils;
import com.plough.nativeprint.utils.CommonCodeUtils;
import com.plough.nativeprint.utils.CommonUtils;
import com.plough.nativeprint.utils.NetworkUtils;
import com.plough.nativeprint.utils.SimpleLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本地打印客户端运行的websocket server,
 * 用于跟网页通信, 触发打印结束事件等等.
 *
 * @return
 */
public class PrintClientServer {
    private static final String KEYSTORE_PATH = CommonUtils.pathJoin(PrintConstants.RELATIVE_CONFIG_HOME, "https/localhost.p12");
    private static final String KEYSTORE_PASSWORD = "123456";

    private static class ServerHolder {
        private static PrintClientServer server = new PrintClientServer();
    }

    private SocketIOServer socketIOServer;
    private ExecutorService executorService;

    private PrintClientServer() {
    }

    public void restart() {
        if (socketIOServer == null) {
            return;
        }
        executorService.shutdownNow();
        SimpleLogger.getInstance().log("socketIOServer 重启");
        initSocketIOServer();
    }

    public void start() {
        initSocketIOServer();
    }

    private void initSocketIOServer() {
        executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                SocketIOServer socketIOServer = createNewSocketIOServer();

                socketIOServer.start();
                SimpleLogger.getInstance().log(socketIOServer + " started!");

                initListeners(socketIOServer);

                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (InterruptedException e) {
                    SimpleLogger.getInstance().log("socketIOServer 线程中断");
                }

                socketIOServer.stop();
                SimpleLogger.getInstance().log(socketIOServer + " stopped!");
            }
        });
    }

    private SocketIOServer createNewSocketIOServer() {
        this.socketIOServer = new SocketIOServer(getConfiguration());
        return socketIOServer;
    }

    private void initListeners(SocketIOServer socketIOServer) {
        // 检查本地打印软件是否启动成功
        socketIOServer.addEventListener("aliveChecking", ChatObject.class, new DataListener<ChatObject>() {
            @Override
            public void onData(SocketIOClient socketIOClient, ChatObject chatObject, AckRequest ackRequest) throws Exception {
                SimpleLogger.getInstance().log("aliveChecking");
                socketIOClient.sendEvent("aliveChecking", new ChatObject(""));
            }
        });

        // 获取本地打印软件中的相关配置
        socketIOServer.addEventListener("getConfigData", ChatObject.class, new DataListener<ChatObject>() {
            @Override
            public void onData(SocketIOClient socketIOClient, ChatObject chatObject, AckRequest ackRequest) throws Exception {
                onGetConfigData(socketIOClient, chatObject);
            }
        });

        // 打印
        socketIOServer.addEventListener("startPrint", ChatObject.class, new DataListener<ChatObject>() {
            @Override
            public void onData(SocketIOClient socketIOClient, ChatObject chatObject, AckRequest ackRequest) throws Exception {
                onStartPrint(socketIOClient, chatObject);
            }
        });
    }

    private Configuration getConfiguration() {
        final Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(ServerConfig.getInstance().getPrintPort());

        setReuseAddress(config);
        checkHttps(config);

        return config;
    }

    private void setReuseAddress(Configuration config) {
        // 关闭服务器时，TCP 连接进入 TIME_WAIT 状态。短时间内再次启动，会出现端口被占用的情况。
        // 因此，需要开启地址重用。
        SocketConfig socketConfig = config.getSocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
    }

    private void checkHttps(Configuration config) {
        NetworkUtils.setTrustAllCerts();

        if (!ServerConfig.getInstance().isHttpsMode()) {
            return;
        }

        File keystoreFile = new File(KEYSTORE_PATH);
        if (!keystoreFile.exists()) {
            SimpleLogger.getInstance().log("https 设置失败：找不到证书文件");
            throw new RuntimeException("https 证书文件不存在，请检查路径：" + keystoreFile.getAbsolutePath());
        }

        try {
            config.setKeyStorePassword(KEYSTORE_PASSWORD);
            config.setKeyStore(new FileInputStream(keystoreFile));
            SimpleLogger.getInstance().log("https 设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            SimpleLogger.getInstance().log("https 设置失败：" + e.getMessage());
        }
    }

    private void onStartPrint(SocketIOClient socketIOClient, ChatObject chatObject) throws JSONException {
        SimpleLogger.getInstance().log("startPrint: " + chatObject.getMessage());
        JSONObject data = new JSONObject(chatObject.getMessage());
        try {
            CustomPrintUtils.printWithJsonArg(data);
            socketIOClient.sendEvent("afterPrint");
        } catch (Exception e) {
            SimpleLogger.getInstance().log(e.getMessage());
            e.printStackTrace();
            // do nothing
        }
    }

    private void onGetConfigData(SocketIOClient socketIOClient, ChatObject chatObject) throws JSONException {
        JSONObject receiveData = new JSONObject(chatObject.getMessage());
        cancelQuietPrintIfSpecified(receiveData);

        JSONObject responseData = createResponseData();
        dealWithCustomFilePrint(receiveData, responseData);  // 放到后面，需要覆盖纸张大小列表

        String message = CommonCodeUtils.cjkEncode(responseData.toString());
        socketIOClient.sendEvent("getConfigData", new ChatObject(message));

        SimpleLogger.getInstance().log("after getConfigData");
    }

    private void cancelQuietPrintIfSpecified(JSONObject receiveData) {
        if (receiveData.has("quietPrint") && !receiveData.optBoolean("quietPrint")) {
            ServerConfig.getInstance().setQuietPrint(false);
        }
    }

    private JSONObject createResponseData() throws JSONException {
        JSONObject data = new JSONObject();

        JSONArray jaPrinters = getServerPrinterJsonArray();
        data.put("printers", jaPrinters);

        data.put("config", ServerConfig.getInstance().createJSONConfig());
        return data;
    }

    private void dealWithCustomFilePrint(JSONObject receiveData, JSONObject data) {
        try {
            CustomPrintUtils.readConfigToData(data, receiveData.getString("customFileUrl"));
        } catch (Exception e) {
            // do nothing
        }
    }

    private JSONArray getServerPrinterJsonArray() throws JSONException {
        String[] serverPrinterList = CommonUtils.getSystemPrinterNameArray();
        JSONArray jaPrinters = new JSONArray();
        for (String printerName : serverPrinterList) {
            JSONObject jo = new JSONObject();
            jo.put("text", printerName);
            jo.put("value", printerName);
            jaPrinters.put(jo);
        }
        return jaPrinters;
    }

    /**
     * 真正的打印开始之前，触发 beforePrint 事件，通知客户端开始打印了
     * */
    public void onBeforePrint(UUID clientId) {
        SocketIOClient client = socketIOServer.getClient(clientId);
        if (client != null) {
            client.sendEvent("beforePrint");
        }
    }

    public static PrintClientServer getInstance() {
        return ServerHolder.server;
    }

    public void stop() {
        socketIOServer.stop();
    }

    public static void main(String[] args){
        try {
            PrintClientServer.getInstance().start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
}
