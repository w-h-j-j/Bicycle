package com.hjst.gather.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.elvishew.xlog.XLog;
import com.hjst.gather.databinding.FragmentOkhttpTestBinding;
import com.hjst.gather.ui.HttpUtils;
import com.hjst.gather.utils.UtilTools;

import java.util.Map;

/**
 * OkHttp 网络请求测试页面
 */
public class OkHttpTestFragment extends Fragment {

    private FragmentOkhttpTestBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOkhttpTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HttpUtils.getInstance().getDeviceInfo(UtilTools.getDeviceId(), new HttpUtils.ResultCallback<Map<String, Object>>() {
            @Override
            public void onSuccess(@Nullable Map<String, Object> data) {
                XLog.d("上传状态：" + data);
            }

            @Override
            public void onFailure(@NonNull String message) {
                XLog.e("设备信息查询失败：" + message);
            }
        });

    }

}
