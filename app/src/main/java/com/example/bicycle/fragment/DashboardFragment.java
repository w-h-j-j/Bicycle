package com.example.bicycle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.bicycle.R;
import com.example.bicycle.XLog;
import com.example.bicycle.databinding.FragmentDashboardBinding;
import com.example.bicycle.widget.PopupManager;

/**
 * 仪表盘 Fragment
 * 包含速度仪表盘和弹窗功能
 */
public class DashboardFragment extends Fragment implements View.OnClickListener {

    private FragmentDashboardBinding binding;
    private PopupManager popupManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dashboard, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        XLog.d("DashboardFragment 创建");
        initDashboard();
        initPopup();
    }

    private void initDashboard() {
        binding.btnSpeedUp.setOnClickListener(this);
        binding.btnSpeedDown.setOnClickListener(this);

        // 启动时动画到初始速度
        binding.speedDashboard.animateToSpeed(220, 800);
        binding.speedDashboard.postDelayed(() -> binding.speedDashboard.animateToSpeed(0, 800), 850);
    }

    private void initPopup() {
        popupManager = new PopupManager(requireContext());

        // 2秒后自动启动弹窗
        binding.speedDashboard.postDelayed(() -> {
            if (popupManager != null) {
                popupManager.startAutoPopup();
                XLog.d("自动启动定时弹窗");
            }
        }, 2000);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int currentSpeed = binding.speedDashboard.getSpeed();

        if (id == R.id.btn_speed_up) {
            int newSpeed = Math.min(currentSpeed + 10, 220);
            binding.speedDashboard.animateToSpeed(newSpeed, 300);
            XLog.d("加速到: " + newSpeed + " km/h");
        } else if (id == R.id.btn_speed_down) {
            int newSpeed = Math.max(currentSpeed - 10, 0);
            binding.speedDashboard.animateToSpeed(newSpeed, 300);
            XLog.d("减速到: " + newSpeed + " km/h");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (popupManager != null) {
            popupManager.stopAutoPopup();
        }
        binding = null;
    }
}