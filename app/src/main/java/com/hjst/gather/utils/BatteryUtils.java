package com.hjst.gather.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

public class BatteryUtils {

    public static int getBattery(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = context.registerReceiver(null, filter);
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        return (int) (level * 100f / scale);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static int getSignal(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (tm != null && tm.getSignalStrength() != null
                        && tm.getSignalStrength().getCellSignalStrengths().size() > 0) {
                    return tm.getSignalStrength().getCellSignalStrengths().get(0).getDbm();
                }
            }
        } catch (Exception e) {
            return -99;
        }
        return -99;
    }
}
