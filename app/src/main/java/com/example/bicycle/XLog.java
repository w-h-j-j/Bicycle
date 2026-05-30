package com.example.bicycle;

import android.util.Log;

/**
 * 自定义日志工具类
 * 在 Logcat 中点击日志可自动跳转到打印日志的源码行
 *
 * 原理：Android Studio Logcat 会将 "ClassName.java:行号" 格式的文本
 * 识别为可点击链接，点击后自动跳转到对应的源码位置
 *
 * 用法：
 *   XLog.d("调试信息");
 *   XLog.e("错误信息");
 *   XLog.json(jsonString);
 */
public final class XLog {

    private static final String DEFAULT_TAG = "Bicycle";
    private static boolean sEnable = true;
    private static String sGlobalTag = DEFAULT_TAG;

    private XLog() {}

    /** 在 Application 中调用，全局配置 */
    public static void init(boolean enable) {
        sEnable = enable;
    }

    public static void init(boolean enable, String globalTag) {
        sEnable = enable;
        sGlobalTag = globalTag;
    }

    // ======================== 核心方法 ========================

    public static void v(String msg) {
        printLog(Log.VERBOSE, msg);
    }

    public static void v(String format, Object... args) {
        printLog(Log.VERBOSE, String.format(format, args));
    }

    public static void d(String msg) {
        printLog(Log.DEBUG, msg);
    }

    public static void d(String format, Object... args) {
        printLog(Log.DEBUG, String.format(format, args));
    }

    public static void i(String msg) {
        printLog(Log.INFO, msg);
    }

    public static void i(String format, Object... args) {
        printLog(Log.INFO, String.format(format, args));
    }

    public static void w(String msg) {
        printLog(Log.WARN, msg);
    }

    public static void w(String format, Object... args) {
        printLog(Log.WARN, String.format(format, args));
    }

    public static void e(String msg) {
        printLog(Log.ERROR, msg);
    }

    public static void e(String format, Object... args) {
        printLog(Log.ERROR, String.format(format, args));
    }

    public static void e(Throwable tr, String msg) {
        if (!sEnable) return;
        StackTraceElement element = getCallerStackTraceElement();
        String tag = buildClickableTag(element);
        Log.e(tag, msg, tr);
    }

    /** 打印 JSON（自动格式化） */
    public static void json(String jsonString) {
        if (!sEnable) return;
        String formatted = formatJson(jsonString);
        d(formatted);
    }

    // ======================== 内部实现 ========================

    /**
     * 获取调用者的堆栈信息
     * 堆栈结构：
     *   [0] Thread.getStackTrace()
     *   [1] getCallerStackTraceElement()
     *   [2] printLog() 或 e()
     *   [3] 实际调用位置 ← 我们要的
     */
    private static StackTraceElement getCallerStackTraceElement() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 下标根据调用层级确定：Thread -> getCallerStackTraceElement -> printLog -> 调用者
        int callerIndex = 5;
        if (stackTrace.length > callerIndex) {
            return stackTrace[callerIndex];
        }
        return stackTrace[stackTrace.length - 1];
    }

    /**
     * 构建可点击跳转的 Tag
     * 格式：GlobalTag.ClassName.java:行号
     * Android Studio Logcat 会识别 "文件名.java:行号" 并生成可点击链接
     */
    private static String buildClickableTag(StackTraceElement element) {
        // element.getFileName() 返回 "MainActivity.java" 这样的文件名
        // 格式化为 "Bicycle.MainActivity.java:42" -> 点击可跳转到第42行
        return sGlobalTag + "." + element.getFileName() + ":" + element.getLineNumber();
    }

    /**
     * 核心打印方法
     */
    private static void printLog(int logLevel, String msg) {
        if (!sEnable) return;

        StackTraceElement element = getCallerStackTraceElement();
        String tag = buildClickableTag(element);

        // 在消息前附加调用方法名，方便定位
        String methodName = element.getMethodName();
        String message = "()[" + methodName + "] " + msg;

        switch (logLevel) {
            case Log.VERBOSE:
                Log.v(tag, message);
                break;
            case Log.DEBUG:
                Log.d(tag, message);
                break;
            case Log.INFO:
                Log.i(tag, message);
                break;
            case Log.WARN:
                Log.w(tag, message);
                break;
            case Log.ERROR:
                Log.e(tag, message);
                break;
        }
    }

    /**
     * 简易 JSON 格式化
     */
    private static String formatJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "Empty/Null json content";
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                return new org.json.JSONObject(json).toString(2);
            }
            if (json.startsWith("[")) {
                return new org.json.JSONArray(json).toString(2);
            }
        } catch (Exception e) {
            return json;
        }
        return json;
    }
}
