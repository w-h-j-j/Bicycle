package com.example.bicycle;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initLog();
    }

    private void initLog() {
        // Debug 模式开启日志，Release 自动关闭
        XLog.init(BuildConfig.DEBUG, "Bicycle");
    }
}
