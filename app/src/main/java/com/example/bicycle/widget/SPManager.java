package com.example.bicycle.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SPManager {

    private static final String key_range = "key_range";
    private static Gson gson = new Gson();

    public static void setRangeList(Context context, List<String> list) {
        if (list == null) return;
        SharedPreferences sharedPref = context.getSharedPreferences("YourPreferenceName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String jsonString = gson.toJson(list);
        Log.i("存储的Json字符串", jsonString);
        editor.putString(key_range, jsonString);
        editor.apply(); // 或者 editor.commit();，两者都可以，apply()是异步的，而commit()是同步的。
    }

    public static List<String> getRangeList(Context context){
        List<String> list = new ArrayList<>();
        SharedPreferences sharedPref = context.getSharedPreferences("YourPreferenceName", Context.MODE_PRIVATE);
        String jsonString = sharedPref.getString(key_range, ""); // 获取默认值为null以处理键不存在的情况
        if (jsonString.isEmpty()) {
            return list; // 或者返回一个空列表，取决于你的需求
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>(){}.getType(); // 使用TypeToken来指定类型参数为List<String>
        list = gson.fromJson(jsonString, type);
        Log.i("获取的列表", Arrays.toString(list.toArray()));
        return list;
    }

    public static boolean isFirst(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("YourPreferenceName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        boolean isFirst = sharedPref.getBoolean("first", true);
        if (isFirst){
            editor.putBoolean("first", false);
            editor.apply();
            return true;
        }
        return false;
    }

}
