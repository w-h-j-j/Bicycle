package com.hjst.gather;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.elvishew.xlog.XLog;
import com.hjst.gather.http_utils.HttpUtils;
import com.hjst.gather.utils.BatteryUtils;
import com.hjst.gather.utils.UtilTools;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自动采集上报服务：每 10 秒调用一次 HttpUtils.postDeviceInfo
 */
public class AutoService extends Service {

    private static final String TAG = "AutoService";
    private static final long REPORT_PERIOD_SECONDS = 10;

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "auto_service_report";

    private ScheduledExecutorService scheduler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 必须先于定时任务进入前台，避免 Android 8+ 后台限制被杀
        startAsForegroundService();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // 启动后立即上报一次，之后每 10 秒循环
        scheduler.scheduleAtFixedRate(this::reportDeviceInfo, 0, REPORT_PERIOD_SECONDS, TimeUnit.SECONDS);
        XLog.d(TAG + ", 已启动，每 " + REPORT_PERIOD_SECONDS + " 秒上报一次设备信息");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        // 被系统回收后尝试重建服务
        return START_STICKY;
    }

    /**
     * 升级为前台服务：常驻低优先级通知，声明 dataSync 类型（Android 14+ 必需）
     */
    private void startAsForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "设备信息上报", NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Intent tapIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, tapIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("采集中：每 " + REPORT_PERIOD_SECONDS + " 秒上报设备信息")
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    /**
     * 采集设备参数并上报：deviceId + 电量(%) + 信号(dBm)
     */
    private void reportDeviceInfo() {
        String deviceId = UtilTools.getDeviceId();
        String battery = String.valueOf(BatteryUtils.getBattery(this));
        // getSignal 需要 Android 9+，低版本固定上报 -99 表示未采集
        String signal = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                ? String.valueOf(BatteryUtils.getSignal(this))
                : "-99";

        HttpUtils.getInstance().postDeviceInfo(deviceId, battery, signal, new HttpUtils.ResultCallback<Object>() {
            @Override
            public void onSuccess(@Nullable Object data) {
                XLog.d(TAG + ", 上报成功: deviceId=" + deviceId + ", battery=" + battery + "%, signal=" + signal + "dBm");
            }

            @Override
            public void onFailure(@NonNull String message) {
                XLog.e(TAG + ", 上报失败: " + message);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        XLog.d(TAG + ", 已停止");
    }
}
