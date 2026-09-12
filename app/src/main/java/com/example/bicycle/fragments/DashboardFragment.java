package com.example.bicycle.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bicycle.MainActivity;
import com.example.bicycle.databinding.FragmentDashboardBinding;
import com.example.bicycle.widget.IosSwitchView;


public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;

    // 距离 & 时间计算
    private double totalDistanceKm = 0;   // 累计距离（公里）
    private long totalElapsedSec = 0;     // 累计时间（秒）
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private static final long TICK_INTERVAL = 1000; // 1秒刷新一次

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 返回按钮
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateBack();
                }
            }
        });

        binding.iosSwitch.setOnCheckedChangeListener(new IosSwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(IosSwitchView view, boolean isChecked) {
                if (isChecked) binding.dashboardView.startSimulation();
                else binding.dashboardView.stopSimulation();
            }
        });

        // 启动定时刷新（1秒一次）
        startTimer();
    }

    /**
     * 每秒定时任务：根据当前速度计算行驶距离和行驶时间
     */
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding == null || binding.dashboardView == null) return;

            float speedKmh = binding.dashboardView.getCurrentSpeed();

            // 距离累加：速度(km/h) × 时间间隔(h)
            totalDistanceKm += speedKmh * (TICK_INTERVAL / 3600000.0);
            totalElapsedSec++;

            // 刷新距离 UI
            binding.tvDistance.setText(String.format("%.2f", totalDistanceKm));

            // 刷新时间 UI（格式 HH:MM:SS）
            long hours = totalElapsedSec / 3600;
            long minutes = (totalElapsedSec % 3600) / 60;
            long seconds = totalElapsedSec % 60;
            binding.tvDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

            timerHandler.postDelayed(this, TICK_INTERVAL);
        }
    };

    private void startTimer() {
        timerHandler.postDelayed(timerRunnable, TICK_INTERVAL);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onDestroyView() {
        stopTimer();
        super.onDestroyView();
        binding = null;
    }
}
