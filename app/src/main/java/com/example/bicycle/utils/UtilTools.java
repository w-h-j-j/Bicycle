package com.example.bicycle.utils;

import android.annotation.SuppressLint;

import com.example.bicycle.App;

public class UtilTools {

    @SuppressLint("HardwareIds")
    public static String getDeviceId(){
        return android.provider.Settings.Secure.getString(
                App.getContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

}
