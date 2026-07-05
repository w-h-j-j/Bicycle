package com.example.bicycle;

import android.app.Application;
import android.content.Context;

public class App extends Application {

    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        initLog();
        context = getApplicationContext();
    }

    private void initLog() {
        // Debug 模式开启日志，Release 自动关闭
        XLog.init(BuildConfig.DEBUG, "Bicycle");
    }

    public static Context getContext(){
        return getContext();
    }
}
