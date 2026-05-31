package com.example.bicycle.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private Runnable autoPopupRunnable;
    private Runnable initAnimRunnable;

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

    @Override
    public void onResume() {
        super.onResume();
        // 恢复弹窗
        if (popupManager != null) {
            popupManager.startAutoPopup();
            XLog.d("DashboardFragment 恢复弹窗");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 暂停弹窗（切到其他 Tab 时停止）
        if (popupManager != null) {
            popupManager.stopAutoPopup();
            XLog.d("DashboardFragment 暂停弹窗");
        }
    }

    private void initDashboard() {
        binding.btnSpeedUp.setOnClickListener(this);
        binding.btnSpeedDown.setOnClickListener(this);

        // 启动时动画到初始速度
        binding.speedDashboard.animateToSpeed(220, 800);
        initAnimRunnable = () -> {
            if (binding != null) {
                binding.speedDashboard.animateToSpeed(0, 800);
            }
        };
        binding.speedDashboard.postDelayed(initAnimRunnable, 850);
    }

    private void initPopup() {
        popupManager = new PopupManager(requireContext());

        // 2秒后自动启动弹窗（保存 Runnable 引用，方便取消）
        autoPopupRunnable = () -> {
            if (popupManager != null && isAdded()) {
                popupManager.startAutoPopup();
                XLog.d("自动启动定时弹窗");
            }
        };
        binding.speedDashboard.postDelayed(autoPopupRunnable, 2000);
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

        // 取消所有未执行的 Runnable
        if (binding != null) {
            binding.speedDashboard.removeCallbacks(autoPopupRunnable);
            binding.speedDashboard.removeCallbacks(initAnimRunnable);
        }

        // 停止弹窗
        if (popupManager != null) {
            popupManager.stopAutoPopup();
            popupManager = null;
        }
        binding = null;
    }
}