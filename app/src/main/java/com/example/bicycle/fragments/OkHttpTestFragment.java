package com.example.bicycle.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bicycle.MainActivity;
import com.example.bicycle.R;
import com.example.bicycle.databinding.FragmentOkhttpTestBinding;
import com.example.bicycle.utils.OkHttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * OkHttp 网络请求测试页面
 */
public class OkHttpTestFragment extends Fragment {

    private FragmentOkhttpTestBinding binding;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOkhttpTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 返回按钮
        binding.btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateBack();
            }
        });

        // 日志区可滚动
        binding.tvLog.setMovementMethod(new android.text.method.ScrollingMovementMethod());

        // 清空按钮
        binding.btnClearLog.setOnClickListener(v -> {
            binding.tvLog.setText("");
        });

        // 默认填充示例 JSON
        binding.etJsonInput.setText("{\"userName\":\"小8\",\"carNo\":\"京A99999\",\"speed\":2500,\"status\":6666}");

        // 读取云端数据
        binding.btnGet.setOnClickListener(v -> {
            appendLog("发起 GET 请求，读取云端数据...");
            updateStatus("请求中", true);
            OkHttpUtil.getJsonFromCloud(new OkHttpUtil.OnRequestCallback() {
                @Override
                public void onSuccess(String result) {
                    appendLog("✅ 读取成功: " + result);
                    // 将读取到的数据填充到输入框，方便查看和编辑
                    binding.etJsonInput.setText(result);
                    updateStatus("空闲", false);
                }

                @Override
                public void onFailure(String errorMsg) {
                    appendLog("❌ " + errorMsg);
                    updateStatus("空闲", false);
                }
            });
        });

        // 上传数据
        binding.btnPut.setOnClickListener(v -> {
            String input = binding.etJsonInput.getText().toString().trim();
            if (input.isEmpty()) {
                appendLog("⚠️ 输入为空，请输入JSON数据");
                return;
            }
            // 校验 JSON 格式
            if (!isValidJson(input)) {
                appendLog("⚠️ JSON格式有误，请检查后重试");
                return;
            }
            appendLog("发起 PUT 请求，上传数据: " + input);
            updateStatus("请求中", true);
            OkHttpUtil.saveJsonToCloud(input, new OkHttpUtil.OnRequestCallback() {
                @Override
                public void onSuccess(String result) {
                    appendLog("✅ 上传成功: " + result);
                    updateStatus("空闲", false);
                }

                @Override
                public void onFailure(String errorMsg) {
                    appendLog("❌ " + errorMsg);
                    updateStatus("空闲", false);
                }
            });
        });

        appendLog("OkHttp 测试页面已就绪");
    }

    /**
     * 校验字符串是否为合法 JSON
     */
    private boolean isValidJson(String text) {
        try {
            new JSONObject(text);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * 追加一条日志（自动滚动到底部）
     */
    private void appendLog(String msg) {
        if (binding == null) return;
        String time = sdf.format(new Date());
        String line = "[" + time + "] " + msg + "\n";
        binding.tvLog.append(line);
        binding.svLog.post(() -> binding.svLog.fullScroll(View.FOCUS_DOWN));
    }

    /**
     * 更新连接状态指示
     */
    private void updateStatus(String status, boolean active) {
        if (binding == null) return;
        if (active) {
            binding.viewStatusDot.setBackgroundResource(R.drawable.bg_status_connected);
        } else {
            binding.viewStatusDot.setBackgroundResource(R.drawable.bg_status_disconnected);
        }
        binding.tvStatus.setText(status);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
