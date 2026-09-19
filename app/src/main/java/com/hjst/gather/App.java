package com.hjst.gather;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;
import com.hjst.gather.serial_utils.ByteUtil;
import com.hjst.gather.serial_utils.FrameParser;
import com.hjst.gather.serial_utils.SerialPortHelper;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.hjst.gather.utils.TTSManager;

import java.util.Random;

public class App extends Application {

    private static final String TAG = "App";
    private static Context context;
    private Thread thread;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initXLog();
        simulationData();
        initTTS();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (!thread.isInterrupted()) thread.interrupt();
        SerialPortHelper.getInstance().closeSerial();
    }

    /**
     * 初始化 xlog 日志库
     */
    private void initXLog() {
        // 获取内部存储的 Download 文件夹路径
        String logPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/bicycle_logs/";
        XLog.init(LogLevel.ALL, new LogConfiguration.Builder()
                        .tag("使用XLog")
                        //.b() // 打印栈信息
                        .st(1) // 栈深度，显示类名行号
                        .build(),
                new AndroidPrinter(), // logcat输出
                new FilePrinter.Builder(logPath) // 自定义车机路径
                        .fileNameGenerator(new DateFileNameGenerator())
                        .flattener(new ClassicFlattener()) // 时间戳改为可读时间
                        .backupStrategy(new FileSizeBackupStrategy(1024 * 1024 * 5)) // 单文件 5MB 满了切下一个
                        .cleanStrategy(new FileLastModifiedCleanStrategy(7L * 24 * 60 * 60 * 1000)) // 保留 7 天
                        .build()
        );
        XLog.d("initXLog ok!");
    }

    /**
     * 讯飞 MSC SDK 初始化
     * */
    private void initTTS(){
        // 注意：appid 必须和下载的 SDK 保持一致，否则会出现 10407 错误
        StringBuffer param = new StringBuffer();
        param.append("appid=be98ffa3");
        param.append(",");
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(context, param.toString());
        // 初始化 TTS 语音合成引擎（全局只需一次）
        TTSManager.getInstance().init(context);
    }

    /**
     * 打开串口，并且模拟数据
     * */
    private void simulationData(){
        Random random = new Random();
        SerialPortHelper.getInstance().openSerial();
        thread = new Thread(() -> {
            while (!thread.isInterrupted()){
                try {
                    int length = random.nextInt(8) + 1;
                    byte len_high = (byte) ((length >> 8) & 0xFF);
                    byte len_low  = (byte) (length & 0xFF);

                    byte[] bytes = new byte[length + 5];

                    bytes[0] = FrameParser.HEAD1;
                    bytes[1] = FrameParser.HEAD2;
                    bytes[2] = len_high;
                    bytes[3] = len_low;

                    for (int i = 0; i < length; i++) {
                        bytes[4 + i] = (byte) (random.nextInt(255) & 0xFF);
                    }

                    int sum = 0;

                    for (int i = 2; i < bytes.length - 1; i++) {
                        sum = sum + bytes[i];
                    }
                    bytes[bytes.length - 1] = (byte) (sum & 0xFF);
                    XLog.d("创造的数据：" + ByteUtil.bytesToHex(bytes));
                    SerialPortHelper.getInstance().onSerialRawRead(bytes);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } catch (RuntimeException e) {
                    break;
                }
            }
        });
        thread.start();
    }

    public static Context getContext(){
        return context;
    }
}
