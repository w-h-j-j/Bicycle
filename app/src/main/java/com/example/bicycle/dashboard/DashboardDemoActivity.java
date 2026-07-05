package com.example.bicycle.dashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bicycle.R;
import com.example.bicycle.databinding.ActivityDashboardDemoBinding;
import com.example.bicycle.widget.AudiDashboard;

/**
 * 奥迪仪表盘演示页面
 */
public class DashboardDemoActivity extends AppCompatActivity {

    private ActivityDashboardDemoBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isDemoRunning = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initListeners();
    }

    private void initViews() {
        // 返回按钮
        binding.ivBack.setOnClickListener(v -> finish());
    }

    private void initListeners() {
        // 转速滑块
        binding.seekbarRpm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.tvRpmValue.setText(String.valueOf(progress));
                if (fromUser) {
                    binding.audiDashboard.setRPM(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 速度滑块
        binding.seekbarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.tvSpeedValue.setText(String.valueOf(progress));
                if (fromUser) {
                    binding.audiDashboard.setSpeed(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 演示按钮
        binding.btnDemo.setOnClickListener(v -> {
            if (isDemoRunning) {
                stopDemo();
            } else {
                startDemo();
            }
        });

        // 归零按钮
        binding.btnReset.setOnClickListener(v -> {
            stopDemo();
            binding.seekbarRpm.setProgress(0);
            binding.seekbarSpeed.setProgress(0);
            binding.audiDashboard.setValues(0, 0);
        });
    }

    /**
     * 开始演示动画
     */
    private void startDemo() {
        isDemoRunning = true;
        binding.btnDemo.setText("停止演示");
        Toast.makeText(this, "开始演示", Toast.LENGTH_SHORT).show();

        // 模拟加速过程
        simulateAcceleration();
    }

    /**
     * 停止演示
     */
    private void stopDemo() {
        isDemoRunning = false;
        binding.btnDemo.setText("演示动画");
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 模拟加速过程
     */
    private void simulateAcceleration() {
        if (!isDemoRunning) return;

        // 阶段 1：起步加速 0 -> 60 km/h
        animateTo(1500, 60, 2000, () -> {
            if (!isDemoRunning) return;

            // 阶段 2：继续加速 60 -> 120 km/h
            animateTo(3500, 120, 2000, () -> {
                if (!isDemoRunning) return;

                // 阶段 3：高速行驶 120 -> 200 km/h
                animateTo(5500, 200, 2500, () -> {
                    if (!isDemoRunning) return;

                    // 阶段 4：极速 200 -> 280 km/h
                    animateTo(7500, 280, 2000, () -> {
                        if (!isDemoRunning) return;

                        // 阶段 5：减速停车
                        animateTo(0, 0, 3000, () -> {
                            if (isDemoRunning) {
                                // 循环演示
                                handler.postDelayed(this::simulateAcceleration, 1000);
                            }
                        });
                    });
                });
            });
        });
    }

    /**
     * 平滑过渡到目标值
     */
    private void animateTo(int targetRPM, int targetSpeed, long duration, Runnable onComplete) {
        if (!isDemoRunning || binding == null) return;

        binding.audiDashboard.setValues(targetRPM, targetSpeed);
        binding.seekbarRpm.setProgress(targetRPM);
        binding.seekbarSpeed.setProgress(targetSpeed);

        handler.postDelayed(onComplete, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopDemo();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}