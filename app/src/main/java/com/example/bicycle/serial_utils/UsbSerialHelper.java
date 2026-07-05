package com.example.bicycle.serial_utils;


import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Android 原生系统USB串口工具类（API24+ 自带API，无第三方so）
 * 配套你之前的 SerialParserWorker、ByteUtil 解包线程
 */
public class UsbSerialHelper {
    /*private static final String TAG = "UsbSerialHelper";
    private static UsbSerialHelper mInstance;
    private final Context mAppContext;
    private UsbManager mUsbManager;
    private UsbSerialManager mSerialManager;
    private UsbSerialDevice mSerialDevice;
    private UsbSerialPort mSerialPort;

    private InputStream mReadStream;
    private OutputStream mWriteStream;

    // 串口读取线程标记
    private volatile boolean mReadLoopRunning;
    private Thread mReadThread;
    // 独立解包线程
    private SerialParserWorker mParserWorker;

    // 串口参数固定：8N1 8数据位 1停止位 无校验
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = UsbSerialPort.STOPBITS_1;
    private static final int PARITY = UsbSerialPort.PARITY_NONE;
    private static final int FLOW_CONTROL = UsbSerialPort.FLOW_CONTROL_OFF;

    private UsbSerialHelper(Context context) {
        mAppContext = context.getApplicationContext();
        mUsbManager = (UsbManager) mAppContext.getSystemService(Context.USB_SERVICE);
        mSerialManager = mAppContext.getSystemService(UsbSerialManager.class);
        mParserWorker = SerialParserWorker.getInstance();
    }

    public static UsbSerialHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (UsbSerialHelper.class) {
                if (mInstance == null) {
                    mInstance = new UsbSerialHelper(context);
                }
            }
        }
        return mInstance;
    }

    // 1、获取所有已插入USB串口设备
    public HashMap<String, UsbDevice> getAllUsbDevice() {
        return mUsbManager.getDeviceList();
    }

    // 2、根据VID PID打开串口（常用CH340 VID=0x1A86）
    public boolean openSerial(int vid, int pid, int baudRate) {
        closeSerial();
        HashMap<String, UsbDevice> deviceMap = getAllUsbDevice();
        UsbDevice targetDevice = null;
        Iterator<UsbDevice> iterator = deviceMap.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice dev = iterator.next();
            if (dev.getVendorId() == vid && dev.getProductId() == pid) {
                targetDevice = dev;
                break;
            }
        }
        if (targetDevice == null) {
            Log.e(TAG, "未找到对应VID/PID串口设备");
            return false;
        }
        // 检查USB权限
        if (!mUsbManager.hasPermission(targetDevice)) {
            Log.e(TAG, "无USB权限，请先申请权限");
            return false;
        }
        try {
            // 系统原生打开串口设备
            mSerialDevice = mSerialManager.openDevice(targetDevice);
            // 使用第0路串口（大部分USB转串口只有port0）
            mSerialPort = mSerialDevice.getPort(0);
            // 配置波特率、8N1参数
            mSerialPort.setParameters(baudRate, DATA_BITS, STOP_BITS, PARITY, FLOW_CONTROL);
            // 获取读写流
            mReadStream = mSerialPort.getInputStream();
            mWriteStream = mSerialPort.getOutputStream();
            // 启动解析线程
            mParserWorker.start();
            // 启动串口读取循环
            startReadThread();
            Log.i(TAG, "原生串口打开成功，波特率：" + baudRate);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "打开串口IO异常", e);
            closeSerial();
            return false;
        }
    }

    // 启动串口读取子线程
    private void startReadThread() {
        if (mReadLoopRunning) {
            LogUtils.i(TAG, "读取线程已运行，无需重复启动");
            return;
        }
        mReadLoopRunning = true;
        mReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int readLen;
                while (mReadLoopRunning) {
                    try {
                        if (mReadStream == null) break;
                        // 阻塞读取串口原始字节
                        readLen = mReadStream.read(buffer);
                        if (readLen > 0) {
                            byte[] rawData = ByteUtil.subBytes(buffer, 0, readLen);
                            // 送入独立解析线程解包（AB BA双字节帧头协议）
                            mParserWorker.pushRawData(rawData);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "串口读取断开", e);
                        break;
                    }
                }
            }
        }, "Native-Serial-Read");
        mReadThread.setPriority(Thread.NORM_PRIORITY + 1);
        mReadThread.start();
    }

    // 完整关闭串口，释放所有系统资源
    public void closeSerial() {
        // 停止读取线程
        mReadLoopRunning = false;
        if (mReadThread != null) {
            mReadThread.interrupt();
            mReadThread = null;
        }
        // 停止解包线程，清空半包缓存
        if (mParserWorker != null) {
            mParserWorker.stop();
        }
        // 关闭流与系统串口端口
        try {
            if (mReadStream != null) mReadStream.close();
            if (mWriteStream != null) mWriteStream.close();
            if (mSerialPort != null) mSerialPort.close();
            if (mSerialDevice != null) mSerialDevice.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mReadStream = null;
        mWriteStream = null;
        mSerialPort = null;
        mSerialDevice = null;
        Log.i(TAG, "系统原生串口已完全关闭");
    }

    // 发送字节数组到串口
    public boolean sendData(byte[] data) {
        if (!isSerialOpen() || data == null || data.length == 0) return false;
        try {
            mWriteStream.write(data);
            mWriteStream.flush();
            Log.d(TAG, "串口发送：" + ByteUtil.bytesToHex(data));
            return true;
        } catch (IOException e) {
            Log.e(TAG, "发送数据失败", e);
            return false;
        }
    }

    // 判断串口是否正常打开
    public boolean isSerialOpen() {
        return mSerialPort != null && mReadStream != null && mWriteStream != null;
    }
*/

}
