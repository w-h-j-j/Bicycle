package com.example.bicycle.serial_utils;


import android.util.Log;

import java.util.List;

public class FrameParser {

    private static FrameParser instance;

    private static final String TAG = "FrameParser";
    // 协议固定帧头
    public static final byte HEAD1 = (byte) 0xAB;
    public static final byte HEAD2 = (byte) 0xBA;
    // 单帧最小完整长度：2帧头 + 2长度 + 1数据 + 1校验 = 6字节   //需要和MCU定义不能发送空的数据包
    private static final int MIN_FRAME_LEN = 6;

    private FrameParser(){}

    public static FrameParser getInstance(){
        if (instance == null){
            synchronized (FrameParser.class){
                if (instance == null){
                    instance = new FrameParser();
                }
            }
        }
        return instance;
    }

    /**
     * 串口完整字节流解包核心方法
     * 协议格式：[0xAB][0xBA][LEN_H][LEN_L][DATA_N][CHECKSUM]
     * 1. 帧头固定双字节：0xAB 紧跟着 0xBA，连续出现才判定为包起始
     * 2. LEN_H+LEN_L：2字节大端无符号整数，代表后面DATA数据域的字节数量
     * 3. 校验范围：LEN_H、LEN_L、全部DATA，累加和取反加1
     * 4. 兼容粘包（多包挤在一起）、分包（半包不完整），循环持续拆所有完整帧
     * 5. 游标 handlePos 动态前进，匹配到完整包直接跳过整包，无效字节只前进1位
     *
     * @param source 缓存半包+本次新数据合并后的完整原始字节流
     * @param outFrameList 输出容器：存放所有解析校验通过的纯业务DATA数据域
     * @return 解析结束后，剩余不足一整包的半包字节数组，交给上层缓存，下次数据拼接后继续解析
     */
    public byte[] parse(byte[] source, List<byte[]> outFrameList) {
        // 安全校验：输入字节流为空 / 输出集合为空，直接返回空数组，不做任何解析
        if (source == null || source.length == 0 || outFrameList == null) {
            return new byte[0];
        }

        // 原始字节流总长度
        int totalLen = source.length;
        // 解析游标：记录当前遍历到哪个下标，所有操作基于此下标偏移
        int handlePos = 0;

        try {
            /**
             * while循环条件解释：
             * MIN_FRAME_LEN=6 是一帧数据最小占用字节：2帧头+2长度+1数据+1校验
             * totalLen - MIN_FRAME_LEN：游标最大允许停留位置，再往后剩余字节凑不齐最小完整包，无需继续遍历
             * 只要游标还没到临界位置，就持续循环匹配帧头、拆分数据包
             */
            while (handlePos <= totalLen - MIN_FRAME_LEN) {
                // 获取游标当前指向的字节，判断是否是第一个帧头0xAB
                byte currByte = source[handlePos];

                // 当前字节不是0xAB，不满足帧头第一位，游标+1跳过当前无效字节，进入下一轮循环
                if (currByte != HEAD1) {
                    handlePos++;
                    continue;
                }

                // 走到这里：当前字节是0xAB，需要校验下一字节是否为0xBA；
                // 先做边界判断，防止数组下标越界（理论while条件已经限制，做双重兜底）
                if (handlePos + 1 >= totalLen) {
                    break;
                }

                // 下一字节不等于0xBA，说明只是单个字节巧合等于AB，不是合法帧头，游标+1跳过
                if (source[handlePos + 1] != HEAD2) {
                    handlePos++;
                    continue;
                }

                // ====================== 代码走到此处：匹配到合法帧头 AB BA ======================
                // 帧头后第1字节：长度高字节 LEN_H
                int lenHigh = ByteUtil.byte2Uint(source[handlePos + 2]);
                // 帧头后第2字节：长度低字节 LEN_L
                int lenLow = ByteUtil.byte2Uint(source[handlePos + 3]);
                // 拼接2字节大端长度，得到业务数据域真实字节长度
                // 左移8位把高字节放到高8位，低字节转无符号后按位或合并
                int dataRealLength = (lenHigh << 8) | lenLow;

                // 计算当前这一整包完整占用字节总数：2帧头 + 2长度 + N数据域 + 1校验和
                int fullFrameByteCount = 2 + 2 + dataRealLength + 1;

                /**
                 * 剩余字节判断：从当前游标开始，往后的字节总数不足以放下完整一帧
                 * 代表当前匹配到帧头，但数据是半包，不完整，直接终止while循环
                 * 游标不再前进，当前帧头及后面所有字节作为半包返回缓存，等下一次接收新数据拼接
                 */
                if (handlePos + fullFrameByteCount > totalLen) {
                    break;
                }

                // ====================== 字节数量足够，开始计算校验和校验合法性 ======================
                // 校验计算区间：从长度字节开始，到数据域最后一字节（不含帧头、不含末尾校验字节）
                int checkStartIndex = handlePos + 2;
                int checkAreaLength = 2 + dataRealLength;
                // 截取参与校验的字节片段
                byte[] checkSourceData = ByteUtil.subBytes(source, checkStartIndex, checkAreaLength);
                // 根据协议算法计算标准校验码
                int calcCheckCode = ByteUtil.calcCheckSum(checkSourceData) & 0xff;
                // 读取帧末尾存储的设备下发的原始校验字节
                int recvCheckIndex = handlePos + fullFrameByteCount - 1;
                int recvCheckCode = source[recvCheckIndex] & 0xff;

                // 本地计算校验码 和 设备传来的校验码不一致：数据包受干扰、数据错误
                if (calcCheckCode != recvCheckCode) {
                    Log.e(TAG, "校验和不匹配，丢弃当前片段，原始流：" + ByteUtil.bytesToHex(source));
                    // 游标仅+1，向后滑动1字节，重新匹配帧头（防止帧头嵌在错误数据中间卡死解析）
                    handlePos++;
                    continue;
                }

                // ====================== 校验完全通过，提取纯业务数据域 ======================
                // 数据域起始下标：跳过2帧头、2长度字节
                int dataStartIndex = handlePos + 4;
                // 截取纯业务数据（剔除帧头、长度、校验，上层只需要业务载荷）
                byte[] businessPayload = ByteUtil.subBytes(source, dataStartIndex, dataRealLength);
                // 将合法完整帧存入输出集合，循环结束后上层统一处理
                outFrameList.add(businessPayload);

                /**
                 * 游标一次性跳过当前完整数据包所有字节
                 * 不需要逐字节+1，直接跳到当前包末尾的下一字节，减少循环次数，提升解析性能
                 */
                handlePos += fullFrameByteCount;
            }

            // while循环结束：两种情况
            // 1. 剩余字节不足最小帧长度，无法再拆分完整包
            // 2. 中途遇到半包，主动break跳出循环
        } catch (Exception e) {
            // 兜底异常捕获：防止数组越界、字节运算异常导致串口读取线程崩溃
            Log.e(TAG, "帧解析过程出现异常", e);
        }

        // ====================== 处理剩余半包 ======================
        // 总字节长度 - 当前游标 = 未处理、不足一整包的半包字节数量
        int remainByteCount = totalLen - handlePos;
        // 无剩余半包，返回空数组
        if (remainByteCount <= 0) {
            return new byte[0];
        }
        // 截取游标之后所有剩余字节，作为半包返回给缓存管理器保存
        return ByteUtil.subBytes(source, handlePos, remainByteCount);
    }

}
