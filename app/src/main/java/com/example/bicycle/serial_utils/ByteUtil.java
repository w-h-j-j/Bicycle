package com.example.bicycle.serial_utils;


public class ByteUtil {

    /**
     * byte转无符号int 0~255
     */
    public static int byte2Uint(byte b) {
        return b & 0xFF;
    }

    /**
     * 拼接两个byte数组
     */
    public static byte[] concat(byte[] a, byte[] b) {
        if (a == null || a.length == 0) return b;
        if (b == null || b.length == 0) return a;
        byte[] res = new byte[a.length + b.length];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        return res;
    }

    /**
     * 截取字节数组
     */
    public static byte[] subBytes(byte[] src, int offset, int len) {
        if (src == null || offset < 0 || len <= 0 || offset + len > src.length) {
            return new byte[0];
        }
        byte[] out = new byte[len];
        System.arraycopy(src, offset, out, 0, len);
        return out;
    }

    /**
     * 字节数组转16进制字符串（日志打印）
     */
    public static String bytesToHex(byte[] data) {
        if (data == null || data.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * 计算累加和校验（长度两字节 + 全部数据域）
     * 校验算法：sum & 0xFF → ~sum +1
     */
    public static byte calcCheckSum(byte[] data) {
        int sum = 0;
        for (byte b : data) {
            sum += byte2Uint(b);
        }
        byte sumByte = (byte) (sum & 0xFF);
        //return (byte) ((~sumByte) + 1);
        return sumByte;
    }

}
