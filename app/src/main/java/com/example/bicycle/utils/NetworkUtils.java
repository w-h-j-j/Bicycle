package com.example.bicycle.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

/**
 * 网络状态工具类
 */
public class NetworkUtils {

    /**
     * 判断当前是否有网络连接
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        if (caps == null) return false;

        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    /**
     * 判断当前是否为 WiFi 连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        if (caps == null) return false;

        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * 注册网络变化监听回调
     */
    public static void registerNetworkCallback(Context context, @NonNull final OnNetworkChangeListener listener) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                listener.onConnected();
            }

            @Override
            public void onLost(@NonNull Network network) {
                listener.onDisconnected();
            }
        });
    }

    public interface OnNetworkChangeListener {
        void onConnected();
        void onDisconnected();
    }
}
