package com.hjst.gather.http_utils;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.elvishew.xlog.XLog;
import com.hjst.gather.model.InfoBean;
import com.hjst.gather.model.Result;

import java.util.HashMap;
import java.util.List;
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

    /**
     * 上报设备信息
     */
    public void postDeviceInfo(String deviceId, String battery, String signal, ResultCallback<Object> callback){
        Map<String, Object> body = new HashMap<>();
        body.put("deviceId", deviceId);
        body.put("battery", battery);
        body.put("signal", signal);
        apiService.report(body).enqueue(new Callback<Result<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Result<Object>> call, @NonNull Response<Result<Object>> response) {
                XLog.d(TAG + ", postDeviceInfo url = " + call.request().url() + ", http = " + response.code());
                Result<Object> result = response.body();
                mainHandler.post(() -> {
                    if (callback == null) return;
                    if (!response.isSuccessful() || result == null) {
                        callback.onFailure("HTTP " + response.code());
                    } else if (result.isSuccess()) {
                        callback.onSuccess(result.getData());
                    } else {
                        callback.onFailure("code=" + result.getCode() + ", msg=" + result.getMsg());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Result<Object>> call, @NonNull Throwable t) {
                XLog.e(TAG + ", postDeviceInfo onFailure: " + t.getMessage());
                mainHandler.post(() -> {
                    if (callback != null) callback.onFailure(String.valueOf(t.getMessage()));
                });
            }
        });
    }

    /**
     * 查询设备历史记录，成功时直接拿到 List<InfoBean>
     */
    public void getDeviceInfo(String deviceId, ResultCallback<List<InfoBean>> callback){
        apiService.history(deviceId).enqueue(new Callback<Result<List<InfoBean>>>() {
            @Override
            public void onResponse(@NonNull Call<Result<List<InfoBean>>> call, @NonNull Response<Result<List<InfoBean>>> response) {
                XLog.d(TAG + ", getDeviceInfo url = " + call.request().url() + ", http = " + response.code());
                Result<List<InfoBean>> result = response.body();
                mainHandler.post(() -> {
                    if (callback == null) return;
                    if (!response.isSuccessful() || result == null) {
                        callback.onFailure("HTTP " + response.code());
                    } else if (result.isSuccess()) {
                        callback.onSuccess(result.getData());
                    } else {
                        callback.onFailure("code=" + result.getCode() + ", msg=" + result.getMsg());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Result<List<InfoBean>>> call, @NonNull Throwable t) {
                XLog.e(TAG + ", getDeviceInfo onFailure: " + t.getMessage());
                mainHandler.post(() -> {
                    if (callback != null) callback.onFailure(String.valueOf(t.getMessage()));
                });
            }
        });
    }
}
