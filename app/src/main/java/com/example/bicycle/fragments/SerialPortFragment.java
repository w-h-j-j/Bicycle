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
import com.example.bicycle.databinding.FragmentSerialPortBinding;
import com.example.bicycle.serial_utils.ByteUtil;
import com.example.bicycle.utils.DataReceiveManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SerialPortFragment extends Fragment {

    private FragmentSerialPortBinding binding;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    private int receiveCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSerialPortBinding.inflate(inflater, container, false);
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
            receiveCount = 0;
        });

        // 注册数据监听
        DataReceiveManager.getInstance().registerListener(listener);
        appendLog("已注册数据监听，等待串口数据...");
        updateStatus(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DataReceiveManager.getInstance().unRegisterListener(listener);
        binding = null;
    }

    private final DataReceiveManager.IDataListener listener = new DataReceiveManager.IDataListener() {
        @Override
        public void onCarDataReceive(byte[] bytes) {
            receiveCount++;
            String hex = ByteUtil.bytesToHex(bytes);
            appendLog("[" + receiveCount + "] " + hex);
            // 底部原始数据行同步更新
            if (binding != null) {
                binding.tvRawData.setText(hex);
            }
        }
    };

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
    private void updateStatus(boolean connected) {
        if (binding == null) return;
        if (connected) {
            binding.viewStatusDot.setBackgroundResource(R.drawable.bg_status_connected);
            binding.tvStatus.setText("已连接");
        } else {
            binding.viewStatusDot.setBackgroundResource(R.drawable.bg_status_disconnected);
            binding.tvStatus.setText("未连接");
        }
    }
}
