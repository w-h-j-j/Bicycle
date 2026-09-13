package com.example.bicycle;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.example.bicycle.utils.TTSManager;

public class App extends Application {

    private static final String TAG = "App";
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

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

    public static Context getContext(){
        return context;
    }
}
