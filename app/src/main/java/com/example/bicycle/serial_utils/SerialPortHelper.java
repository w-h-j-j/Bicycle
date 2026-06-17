package com.example.bicycle.serial_utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.bicycle.App;

public class SerialPortHelper {

    private static SerialPortHelper instance;
    private SerialParserWorker mParserWorker;       // 解析工作器
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());    // 主线程Handler，用于UI刷新

    private SerialPortHelper(){}

    public static SerialPortHelper getInstance(){
        if (instance == null){
            synchronized (SerialPortHelper.class){
                if (instance == null){
                    instance = new SerialPortHelper();
                }
            }
        }
        return instance;
    }



    /**
     * 打开串口时初始化并启动解析线程
     */
    public void openSerial() {
        // 串口硬件打开逻辑省略...
        //UsbSerialHelper.getInstance(App.getContext()).openSerial();

        // 初始化解析器
        mParserWorker = new SerialParserWorker();
        mParserWorker.setCallback(new SerialParserWorker.FrameResultCallback() {
            @Override
            public void onReceiveCompleteFrame(final byte[] payload) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleSerialPayload(payload);
                    }
                });
            }
        });
        mParserWorker.start();
    }

    /**
     * 串口底层IO读取回调（IO线程，禁止耗时操作）
     */
    public void onSerialRawRead(byte[] rawBuffer) {
        // 仅推送数据入队列，毫秒级完成，不阻塞串口
        if (mParserWorker != null) {
            mParserWorker.pushRawData(rawBuffer);
        }
    }

    /**
     * 主线程处理解析完成的业务数据帧
     */
    private void handleSerialPayload(byte[] payload) {
        String hexStr = ByteUtil.bytesToHex(payload);
        Log.d("SerialBusiness", "收到业务数据：" + hexStr);
        // 自定义业务逻辑：指令解析、数据计算、控件刷新
    }

    /**
     * 关闭串口，释放所有资源
     */
    public void closeSerial() {
        if (mParserWorker != null) {
            mParserWorker.stop();
            mParserWorker = null;
        }
        mMainHandler.removeCallbacksAndMessages(null);
        // 关闭串口硬件逻辑省略...
        UsbSerialHelper.getInstance(App.getContext()).closeSerial();
    }
}
