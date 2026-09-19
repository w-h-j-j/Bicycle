package com.hjst.gather.http_utils;


import com.hjst.gather.model.InfoBean;
import com.hjst.gather.model.Result;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/api/device/report")
    Call<Result<Object>> report(@Body Map<String, Object> body);

    @GET("/api/device/history")
    Call<Result<List<InfoBean>>> history(@Query("deviceId") String deviceId);
}