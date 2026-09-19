package com.example.bicycle.http_utils;


import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/device/report")
    Call<Map<String, Object>> report(@Body Map<String, Object> body);

    @GET("/api/device/history")
    Call<Map<String, Object>> history(@Query("deviceId") String deviceId);
}