package com.hjst.gather.utils;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.List;

public class DataReceiveManager {
    private static final String TAG = "DataReceiveManager";
    private static final DataReceiveManager INSTANCE = new DataReceiveManager();
    // 统一锁对象，所有操作共用一把锁
    private final Object lockObj = new Object();
    private final List<IDataListener> listenerList = new ArrayList<>();
    private byte[] bytesLast;

    private DataReceiveManager() {}
    public static DataReceiveManager getInstance() {
        return INSTANCE;
    }

    // 注册监听
    public void registerListener(IDataListener listener) {
        synchronized (lockObj) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
                // 注册成功，立刻推送缓存的最新状态
                if(bytesLast != null){
                    // 拷贝一份，防止外部修改原始对象
                    listener.onCarDataReceive(bytesLast);
                }
            }
        }
    }

    // 取消注册
    public void unRegisterListener(IDataListener listener) {
        synchronized (lockObj) {
            listenerList.remove(listener);
        }
    }

    // 底层硬件Service调用，分发数据
    public void dispatchCarData(byte[] bytes) {
        if(bytes == null){
            return;
        }
        synchronized (lockObj) {
            // 复制一份监听器列表进行遍历，避免原集合被修改触发ConcurrentModificationException
            bytesLast = bytes;
            List<IDataListener> tempListeners = new ArrayList<>(listenerList);
            for (IDataListener listener : tempListeners) {
                try {
                    listener.onCarDataReceive(bytes);
                } catch (Exception e) {
                    // 单个页面异常不影响其他页面，XLog打印异常
                    XLog.e(TAG + "   onCarDataReceive callback error");
                }
            }
        }
    }

    // 接口定义
    public interface IDataListener {
        void onCarDataReceive(byte[] bean);
    }
}
