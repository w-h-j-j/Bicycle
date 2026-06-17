package com.example.bicycle.serial_utils;


/**
 * 缓存上一次未解析完成的半包
 */
public class SerialCacheManager {

    private byte[] mCache = new byte[0];
    // 缓存最大限制，防止脏数据无限堆积OOM
    private static final int MAX_CACHE = 4096;

    /**
     * 拼接历史缓存 + 本次新读到的数据
     */
    public byte[] mergeCache(byte[] newData) {
        if (mCache.length == 0) return newData;
        if (newData == null || newData.length == 0) return mCache;
        byte[] combine = ByteUtil.concat(mCache, newData);
        // 超长清空缓存，避免内存溢出
        if (combine.length > MAX_CACHE) {
            mCache = new byte[0];
            return new byte[0];
        }
        return combine;
    }

    /**
     * 更新缓存，保存剩余半包
     */
    public void setCache(byte[] remain) {
        mCache = remain;
    }

    /**
     * 清空缓存（串口关闭/切换设备调用）
     */
    public void clear() {
        mCache = new byte[0];
    }

}
