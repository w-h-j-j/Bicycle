package com.example.bicycle.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

/**
 * 讯飞 MSC 语音合成单例管理器
 * <p>
 * 提供常用 TTS 接口，全局只需初始化一次，任何地方均可调用。
 * <p>
 * 用法：
 * <pre>
 *   // 初始化（Application 或首次使用前调用一次）
 *   TTSManager.getInstance().init(context);
 *
 *   // 播报
 *   TTSManager.getInstance().speak("你好");
 *
 *   // 停止
 *   TTSManager.getInstance().stop();
 *
 *   // 销毁（不再使用时调用）
 *   TTSManager.getInstance().destroy();
 * </pre>
 */
public class TTSManager {

    private static final String TAG = "TTSManager";

    private static volatile TTSManager instance;

    private SpeechSynthesizer mTts;
    private boolean isInitialized = false;
    private boolean isSpeaking = false;
    private boolean isPaused = false;

    // ==================== 可配置参数 ====================

    /** 引擎类型：云端 / 本地 / 增强版 */
    private String engineType = SpeechConstant.TYPE_CLOUD;
    /** 发音人 */
    private String voiceName = "xiaoyan";
    /** 语速 0~100 */
    private int speed = 50;
    /** 音调 0~100 */
    private int pitch = 50;
    /** 音量 0~100 */
    private int volume = 50;

    /** 外部回调 */
    private TTSCallback callback;

    // ==================== 单例 ====================

    private TTSManager() {
    }

    public static TTSManager getInstance() {
        if (instance == null) {
            synchronized (TTSManager.class) {
                if (instance == null) {
                    instance = new TTSManager();
                }
            }
        }
        return instance;
    }

    // ==================== 回调接口 ====================

    /**
     * TTS 状态回调
     */
    public interface TTSCallback {
        /** 初始化完成，success 为 true 表示成功 */
        void onInitResult(boolean success, int errorCode);

        /** 开始播放 */
        default void onSpeakBegin() {}

        /** 播放完成 */
        default void onSpeakCompleted() {}

        /** 播放出错 */
        default void onSpeakError(int errorCode, String description) {}

        /** 暂停播放 */
        default void onSpeakPaused() {}

        /** 继续播放 */
        default void onSpeakResumed() {}

        /** 播放进度更新 (0~100) */
        default void onSpeakProgress(int percent) {}

        /** 缓冲进度更新 (0~100) */
        default void onBufferProgress(int percent) {}
    }

    // ==================== 生命周期 ====================

    /**
     * 初始化语音合成引擎（全局调用一次，建议在 Application 中调用）
     */
    public void init(Context context) {
        if (isInitialized && mTts != null) {
            Log.d(TAG, "TTS 已初始化，跳过重复调用");
            return;
        }
        mTts = SpeechSynthesizer.createSynthesizer(context, mInitListener);
    }

    /**
     * 销毁合成引擎，释放资源
     */
    public void destroy() {
        if (mTts != null) {
            mTts.stopSpeaking();
            mTts.destroy();
            mTts = null;
        }
        isInitialized = false;
        isSpeaking = false;
        isPaused = false;
        callback = null;
    }

    // ==================== 播报控制 ====================

    /**
     * 播报文本（使用当前参数）
     *
     * @return ErrorCode，0 表示成功
     */
    public int speak(String text) {
        if (!checkReady()) return -1;
        applyParams();
        isSpeaking = true;
        isPaused = false;
        int code = mTts.startSpeaking(text, mSynthesizerListener);
        if (code != ErrorCode.SUCCESS) {
            isSpeaking = false;
            Log.e(TAG, "startSpeaking 失败，错误码: " + code);
        }
        return code;
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mTts != null && isSpeaking && !isPaused) {
            mTts.pauseSpeaking();
            isPaused = true;
        }
    }

    /**
     * 继续播放
     */
    public void resume() {
        if (mTts != null && isSpeaking && isPaused) {
            mTts.resumeSpeaking();
            isPaused = false;
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (mTts != null) {
            mTts.stopSpeaking();
            isSpeaking = false;
            isPaused = false;
        }
    }

    /**
     * 合成并保存为音频文件
     *
     * @param text 要合成的文本
     * @param path 保存路径（.wav 或 .pcm）
     * @return ErrorCode，0 表示成功
     */
    public int synthesizeToFile(String text, String path) {
        if (!checkReady()) return -1;
        applyParams();
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, path);
        return mTts.startSpeaking(text, mSynthesizerListener);
    }

    // ==================== 参数设置 ====================

    /** 设置发音人（如 xiaoyan / xiaofeng） */
    public TTSManager setVoiceName(String voiceName) {
        this.voiceName = voiceName;
        return this;
    }

    /** 设置语速 (0~100，默认50) */
    public TTSManager setSpeed(int speed) {
        this.speed = Math.max(0, Math.min(100, speed));
        return this;
    }

    /** 设置音调 (0~100，默认50) */
    public TTSManager setPitch(int pitch) {
        this.pitch = Math.max(0, Math.min(100, pitch));
        return this;
    }

    /** 设置音量 (0~100，默认50) */
    public TTSManager setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        return this;
    }

    /** 设置引擎类型：TYPE_CLOUD / TYPE_LOCAL / TYPE_XTTS */
    public TTSManager setEngineType(String engineType) {
        this.engineType = engineType;
        return this;
    }

    /** 设置回调 */
    public TTSManager setCallback(TTSCallback callback) {
        this.callback = callback;
        return this;
    }

    // ==================== 状态查询 ====================

    /** 引擎是否已初始化 */
    public boolean isInitialized() {
        return isInitialized;
    }

    /** 是否正在播报 */
    public boolean isSpeaking() {
        return isSpeaking;
    }

    /** 是否处于暂停状态 */
    public boolean isPaused() {
        return isPaused;
    }

    // ==================== 内部实现 ====================

    private boolean checkReady() {
        if (mTts == null || !isInitialized) {
            Log.e(TAG, "TTS 未初始化，请先调用 init()");
            return false;
        }
        return true;
    }

    /** 将当前参数应用到合成器 */
    private void applyParams() {
        mTts.setParameter(SpeechConstant.PARAMS, null);
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, engineType);
        mTts.setParameter(SpeechConstant.VOICE_NAME, voiceName);
        mTts.setParameter(SpeechConstant.SPEED, String.valueOf(speed));
        mTts.setParameter(SpeechConstant.PITCH, String.valueOf(pitch));
        mTts.setParameter(SpeechConstant.VOLUME, String.valueOf(volume));
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
    }

    /** 初始化监听 */
    private final InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            if (code == ErrorCode.SUCCESS) {
                isInitialized = true;
                Log.d(TAG, "TTS 引擎初始化成功");
            } else {
                isInitialized = false;
                Log.e(TAG, "TTS 引擎初始化失败，错误码: " + code);
            }
            if (callback != null) {
                callback.onInitResult(isInitialized, code);
            }
        }
    };

    /** 合成播放监听 */
    private final SynthesizerListener mSynthesizerListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            isSpeaking = true;
            isPaused = false;
            if (callback != null) callback.onSpeakBegin();
        }

        @Override
        public void onSpeakPaused() {
            isPaused = true;
            if (callback != null) callback.onSpeakPaused();
        }

        @Override
        public void onSpeakResumed() {
            isPaused = false;
            if (callback != null) callback.onSpeakResumed();
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            if (callback != null) callback.onBufferProgress(percent);
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            if (callback != null) callback.onSpeakProgress(percent);
        }

        @Override
        public void onCompleted(SpeechError error) {
            isSpeaking = false;
            isPaused = false;
            if (callback != null) {
                if (error == null) {
                    callback.onSpeakCompleted();
                } else {
                    callback.onSpeakError(error.getErrorCode(), error.getPlainDescription(true));
                }
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 保留扩展
        }
    };
}
