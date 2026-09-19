package com.hjst.gather.utils;

import android.annotation.SuppressLint;

import com.hjst.gather.App;

public class UtilTools {

    @SuppressLint("HardwareIds")
    public static String getDeviceId(){
        return android.provider.Settings.Secure.getString(
                App.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

}
