package com.example.bicycle;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.example.bicycle.serial_utils.ByteUtil;
import com.example.bicycle.serial_utils.FrameParser;
import com.example.bicycle.serial_utils.SerialPortHelper;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.example.bicycle.utils.TTSManager;

import java.util.Random;

public class App extends Application {

    private static final String TAG = "App";
    private static Context context;
    private Thread thread;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        simulationData();

        // 讯飞 MSC SDK 初始化
        // 注意：appid 必须和下载的 SDK 保持一致，否则会出现 10407 错误
        StringBuffer param = new StringBuffer();
        param.append("appid=be98ffa3");
        param.append(",");
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(context, param.toString());
        Log.d(TAG, "讯飞 MSC SDK 初始化完成");

        // 初始化 TTS 语音合成引擎（全局只需一次）
        TTSManager.getInstance().init(context);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (!thread.isInterrupted()) thread.interrupt();
        SerialPortHelper.getInstance().closeSerial();
    }

    public static Context getContext(){
        return context;
    }

    Random random = new Random();
    private void simulationData(){
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

                    for (int i = 0; i < length - 1; i++) {
                        bytes[4 + i] = (byte) (random.nextInt(255) & 0xFF);
                    }

                    int sum = 0;

                    for (int i = 2; i < bytes.length - 1; i++) {
                        sum = sum + bytes[i];
                    }
                    bytes[bytes.length - 1] = (byte) (sum & 0xFF);
                    SerialPortHelper.getInstance().onSerialRawRead(bytes);
                    System.out.println("数据：" + ByteUtil.bytesToHex(bytes));
                    try {
                        Thread.sleep(200);
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
}
