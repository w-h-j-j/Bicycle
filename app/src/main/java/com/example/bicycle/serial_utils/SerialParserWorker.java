package com.example.bicycle.serial_utils;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 串口独立解析工作线程
 * 所有串口原始数据入队列，单一线程串行解包，不阻塞串口IO线程
 * 内置半包缓存，持续处理源源不断的串口数据流
 */
public class SerialParserWorker {
    private static final String TAG = "SerialParserWorker";
    private static SerialParserWorker instance;

    // 阻塞队列：缓存串口原始接收数据
    private final BlockingQueue<byte[]> mRawDataQueue;
    // 半包缓存管理器
    private final SerialCacheManager mCache;
    // 帧解析核心类
    private final FrameParser mFrameParser;

    // 解析工作线程
    private Thread mWorkThread;
    // 线程运行标记 volatile 保证多线程可见性
    private volatile boolean mRunning;

    // 数据回调接口，解析出完整帧对外抛出
    public interface FrameResultCallback {
        void onReceiveCompleteFrame(byte[] payload);
    }
    private FrameResultCallback mCallback;

    public SerialParserWorker() {
        mRawDataQueue = new LinkedBlockingQueue<>();
        mCache = new SerialCacheManager();
        mFrameParser = FrameParser.getInstance();
    }

    public static SerialParserWorker getInstance(){
        if (instance == null){
            synchronized (SerialParserWorker.class){
                if (instance == null){
                    instance = new SerialParserWorker();
                }
            }
        }
        return instance;
    }

    // 设置解析结果回调
    public void setCallback(FrameResultCallback callback) {
        this.mCallback = callback;
    }

    /**
     * 启动解析线程，串口打开时调用一次
     */
    public void start() {
        if (mRunning) {
            Log.i(TAG, "解析线程已运行，无需重复启动");
            return;
        }
        mRunning = true;
        mWorkThread = new Thread(runnableLoopParseTask, "Serial-Parser-Worker");
        // 设置适中线程优先级，兼顾串口实时性
        //mWorkThread.setPriority(RenderScript.Priority.NORMAL);
        mWorkThread.start();
        Log.i(TAG, "串口解析子线程启动成功");
    }

    /**
     * 循环消费队列数据，所有解包逻辑运行在独立子线程
     */
    private Runnable runnableLoopParseTask = new Runnable() {
        @Override
        public void run() {
            while (mRunning) {
                byte[] rawData;
                try {
                    // 无数据时阻塞休眠，不消耗CPU
                    rawData = mRawDataQueue.take();
                    Log.d(TAG, "mRawDataQueue.take   rawData = " + ByteUtil.bytesToHex(rawData));
                } catch (InterruptedException e) {
                    Log.w(TAG, "解析线程被中断，退出循环");
                    break;
                }

                // 1、合并上次残留半包 + 当前新接收数据
                byte[] combineBuffer = mCache.mergeCache(rawData);
                if (combineBuffer.length == 0) {
                    continue;
                }

                // 2、执行粘包/分包解析（耗时逻辑全部在子线程）
                List<byte[]> completeFrameList = new ArrayList<>();
                byte[] remainHalfData = mFrameParser.parse(combineBuffer, completeFrameList);
                // 3、保存本次解析剩余半包，下次数据到来继续拼接
                mCache.setCache(remainHalfData);
                // 4、遍历所有合法完整业务帧，回调上层
                for (byte[] payload : completeFrameList) {
                    if (mCallback != null) {
                        mCallback.onReceiveCompleteFrame(payload);
                    }
                }
            }
            Log.i(TAG, "解析循环结束，线程退出");
        }
    };

    /**
     * 外部串口接收回调调用：推送原始数据进入解析队列
     * @param rawBytes 串口底层读取到的原始字节数组
     */
    public void pushRawData(byte[] rawBytes) {
        if (!mRunning || rawBytes == null || rawBytes.length == 0) {
            return;
        }
        try {
            mRawDataQueue.put(rawBytes);
        } catch (InterruptedException e) {
            Log.e(TAG, "数据入队异常", e);
        }
    }

    /**
     * 停止解析线程，串口关闭/页面销毁必须调用
     * 清空队列、清空半包缓存，释放资源
     */
    public void stop() {
        mRunning = false;
        if (mWorkThread != null) {
            mWorkThread.interrupt();
        }
        // 清空等待队列所有残留数据
        mRawDataQueue.clear();
        // 清空半包缓存，防止下次打开串路口脏数据
        mCache.clear();
        // 清空回调，避免内存泄漏
        mCallback = null;
        Log.i(TAG, "解析线程已停止，缓存全部清空");
    }
}
