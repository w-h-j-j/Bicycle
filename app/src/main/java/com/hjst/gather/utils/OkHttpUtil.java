package com.hjst.gather.utils;


import android.os.Handler;
import android.os.Looper;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil {

    private static final String TAG = "OkHttpUtil";

    public static final String BIN_ID = "6aaa0209ac6210605ad2d965";
    private static final String X_MASTER_KEY = "$2a$10$XR/ZtEn5Tu5Mb1Jh9u43jeU7PI19LLVRgKnPZKpmWr.sbDDH1AViu";
    private static final String BASE_URL = "https://api.jsonbin.io/v3/b/";


    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * 网络请求回调接口（回调在主线程）
     */
    public interface OnRequestCallback {
        void onSuccess(String result);
        void onFailure(String errorMsg);
    }


    /**
     * PUT：上传/编辑数据，覆盖云端Bin里全部json
     * @param json 你要保存的完整json字符串
     * @param callback 结果回调（主线程），可为null
     */
    public static void saveJsonToCloud(String json, OnRequestCallback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + BIN_ID)
                .put(body)
                .header("Content-Type", "application/json")
                .header("X-Master-Key", X_MASTER_KEY)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                XLog.e(TAG + "   保存失败: " + e.getMessage());
                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure("保存失败: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    XLog.d(TAG + "   ✅云端保存成功：" + result);
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(result));
                    }
                } else {
                    XLog.e(TAG + "   ❌保存失败 code:" + response.code());
                    if (callback != null) {
                        mainHandler.post(() -> callback.onFailure("保存失败 code:" + response.code()));
                    }
                }
                response.close();
            }
        });
    }

    /**
     * GET：读取云端保存的数据
     * @param callback 结果回调（主线程），可为null
     */
    public static void getJsonFromCloud(OnRequestCallback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + BIN_ID + "/latest")
                .get()
                .header("X-Master-Key", X_MASTER_KEY)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                XLog.e(TAG + "   读取失败: " + e.getMessage());
                if (callback != null) {
                    mainHandler.post(() -> callback.onFailure("读取失败: " + e.getMessage()));
                }
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    XLog.d(TAG + "   读取云端数据：" + result);
                    if (callback != null) {
                        mainHandler.post(() -> callback.onSuccess(result));
                    }
                } else {
                    XLog.e(TAG + "   读取失败 code:" + response.code());
                    if (callback != null) {
                        mainHandler.post(() -> callback.onFailure("读取失败 code:" + response.code()));
                    }
                }
                response.close();
            }
        });
    }
}