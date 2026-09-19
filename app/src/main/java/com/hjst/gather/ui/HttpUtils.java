package com.hjst.gather.ui;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.elvishew.xlog.XLog;
import com.hjst.gather.http_utils.ApiService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpUtils {

    /**
     * 统一请求结果回调，回调固定在主线程执行
     */
    public interface ResultCallback<T> {
        void onSuccess(@Nullable T data);
        void onFailure(@NonNull String message);
    }

    private static volatile HttpUtils instance;

    private static final String TAG = "HttpUtils";
    private static final String BASE_URL = "http://120.24.169.197:8080/";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // Retrofit/OkHttpClient 随单例创建一次，复用连接池和线程池，避免每次请求重建
    private final ApiService apiService;

    private HttpUtils(){
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();
        apiService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService.class);
    }

    public static HttpUtils getInstance(){
        if (instance == null){
            synchronized (HttpUtils.class){
                if (instance == null){
                    instance = new HttpUtils();
                }
            }
        }
        return instance;
    }

    public void postDeviceInfo(String deviceId, String battery, String signal, ResultCallback<Map<String, Object>> callback){
        Map<String, Object> body = new HashMap<>();
        body.put("deviceId", deviceId);
        body.put("battery", battery);
        body.put("signal", signal);
        apiService.report(body).enqueue(newCallback("postDeviceInfo", callback));
    }

    public void getDeviceInfo(String deviceId, ResultCallback<Map<String, Object>> callback){
        apiService.history(deviceId).enqueue(newCallback("getDeviceInfo", callback));
    }

    /**
     * 构建通用 Retrofit 回调：统一日志、判空、错误信息透传，并切回主线程
     */
    private Callback<Map<String, Object>> newCallback(String action, ResultCallback<Map<String, Object>> callback) {
        return new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                XLog.d(TAG + ", " + action + " request url = " + call.request().url());
                Map<String, Object> result = response.body();
                XLog.d(TAG + ", " + action + " onResponse code = " + response.code() + ", body = " + result);
                mainHandler.post(() -> {
                    if (callback == null) return;
                    if (response.isSuccessful()) {
                        callback.onSuccess(result);
                    } else {
                        callback.onFailure("HTTP " + response.code());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                XLog.e(TAG + ", " + action + " onFailure: " + t.getMessage());
                mainHandler.post(() -> {
                    if (callback != null) callback.onFailure(String.valueOf(t.getMessage()));
                });
            }
        };
    }
}
