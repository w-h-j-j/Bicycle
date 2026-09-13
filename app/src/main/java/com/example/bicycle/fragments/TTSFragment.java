package com.example.bicycle.fragments;


import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bicycle.MainActivity;
import com.example.bicycle.R;
import com.example.bicycle.databinding.FragmentTtsBinding;
import com.example.bicycle.utils.TTSManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TTSFragment extends Fragment {

    private static final String TAG = "TTSFragment";

    private FragmentTtsBinding binding;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTtsBinding.inflate(inflater, container, false);
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
        binding.tvLog.setMovementMethod(new ScrollingMovementMethod());

        // 初始化 TTS 引擎并设置回调
        TTSManager tts = TTSManager.getInstance();
        tts.init(getContext(), ttsCallback);

        // 播报按钮
        binding.btnSpeak.setOnClickListener(v -> {
            String text = binding.etText.getText().toString().trim();
            if (text.isEmpty()) {
                appendLog("文本为空，无法播报");
                return;
            }
            if (!tts.isInitialized()) {
                appendLog("合成引擎未初始化");
                return;
            }
            tts.speak(text);
        });

        // 停止按钮
        binding.btnStop.setOnClickListener(v -> {
            tts.stop();
            appendLog("停止播报");
        });
    }

    // ==================== TTS 回调 ====================

    private final TTSManager.TTSCallback ttsCallback = new TTSManager.TTSCallback() {
        @Override
        public void onInitResult(boolean success, int errorCode) {
            updateStatus(success);
            if (success) {
                appendLog("SDK 初始化成功");
            } else {
                appendLog("SDK 初始化失败，错误码: " + errorCode);
            }
        }

        @Override
        public void onSpeakBegin() {
            appendLog("开始播放");
        }

        @Override
        public void onSpeakCompleted() {
            appendLog("播放完成");
        }

        @Override
        public void onSpeakError(int errorCode, String description) {
            appendLog("播放出错: " + description);
            Log.e(TAG, "TTS error: " + errorCode + " - " + description);
        }

        @Override
        public void onSpeakPaused() {
            appendLog("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            appendLog("继续播放");
        }

        @Override
        public void onSpeakProgress(int percent) {
            Log.d(TAG, "播放进度: " + percent + "%");
        }

        @Override
        public void onBufferProgress(int percent) {
            Log.d(TAG, "缓冲进度: " + percent + "%");
        }
    };

    // ==================== 工具方法 ====================

    /**
     * 更新 SDK 连接状态
     */
    public void updateStatus(boolean connected) {
        if (binding == null) return;
        if (connected) {
            binding.viewStatusDot.setBackgroundResource(R.drawable.bg_status_connected);
            binding.tvStatus.setText("已连接");
        } else {
            binding.viewStatusDot.setBackgroundResource(R.drawable.bg_status_disconnected);
            binding.tvStatus.setText("未连接");
        }
    }

    /**
     * 追加一条日志（自动滚动到底部）
     */
    public void appendLog(String msg) {
        if (binding == null) return;
        String time = sdf.format(new Date());
        String line = "[" + time + "] " + msg + "\n";
        binding.tvLog.append(line);
        binding.svLog.post(() -> binding.svLog.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onDestroyView() {
        // 移除回调，避免 Fragment 销毁后回调到已释放的 binding
        TTSManager.getInstance().setCallback(null);
        super.onDestroyView();
        binding = null;
    }
}
