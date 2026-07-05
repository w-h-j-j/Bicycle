package com.example.bicycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bicycle.adapter.NavGridAdapter;
import com.example.bicycle.databinding.ActivityMainNewBinding;
import com.example.bicycle.dashboard.DashboardDemoActivity;
import com.example.bicycle.fragment.CalculatorFragment;
import com.example.bicycle.fragment.DashboardFragment;
import com.example.bicycle.fragment.MusicFragment;
import com.example.bicycle.fragment.SerialFragment;
import com.example.bicycle.serial_utils.SerialPortHelper;

import java.util.Random;

/**
 * 主页面 - Fragment 容器
 * 使用底部导航栏切换三个 Fragment：
 * 1. 仪表盘
 * 2. 音乐播放器
 * 3. 计算器
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainNewBinding binding;

    private DashboardFragment dashboardFragment;
    private MusicFragment musicFragment;
    private CalculatorFragment calculatorFragment;
    private SerialFragment serialFragment;

    private Fragment currentFragment;

    // 防抖：两次点击间隔小于 300ms 则忽略
    private long lastClickTime = 0;
    private static final long CLICK_INTERVAL = 300;

    // 导航项列表
    private java.util.List<NavGridAdapter.NavItem> navItems;
    private NavGridAdapter navAdapter;

    // 导航 ID 到 position 映射
    private int getIdPosition(int id) {
        for (int i = 0; i < navItems.size(); i++) {
            if (navItems.get(i).id == id) return i;
        }
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_new);

        XLog.d("主页面启动（Fragment 架构）");

        // 初始化 Fragment（延迟创建）
        if (savedInstanceState == null) {
            dashboardFragment = new DashboardFragment();
            loadFragment(dashboardFragment);
            binding.tvTitle.setText("仪表盘");
            currentFragment = dashboardFragment;
        }

        // 初始化 GridView 导航
        setupGridView();
    }

    private void setupGridView() {
        navItems = new java.util.ArrayList<>();
        navItems.add(new NavGridAdapter.NavItem(R.id.nav_dashboard, android.R.drawable.ic_menu_compass, "仪表盘"));
        navItems.add(new NavGridAdapter.NavItem(R.id.nav_liquid_dashboard, android.R.drawable.ic_menu_myplaces, "液晶仪表"));
        navItems.add(new NavGridAdapter.NavItem(R.id.nav_calculator, android.R.drawable.ic_menu_edit, "计算器"));
        navItems.add(new NavGridAdapter.NavItem(R.id.nav_serial, android.R.drawable.ic_menu_manage, "串口"));

        navAdapter = new NavGridAdapter(this, navItems);
        binding.gridNav.setAdapter(navAdapter);
        binding.gridNav.setSelection(0);

        // 点击事件
        binding.gridNav.setOnItemClickListener((parent, view, position, id) -> {
            // 防抖处理
            long now = System.currentTimeMillis();
            if (now - lastClickTime < CLICK_INTERVAL) {
                return;
            }
            lastClickTime = now;

            NavGridAdapter.NavItem item = navItems.get(position);
            handleNavClick(item.id, position);
        });
    }

    private void handleNavClick(int id, int position) {
        int oldPosition = navAdapter.getSelectedPosition();
        Fragment targetFragment = null;
        String title = "";

        if (id == R.id.nav_dashboard) {
            if (dashboardFragment == null) dashboardFragment = new DashboardFragment();
            targetFragment = dashboardFragment;
            title = "仪表盘";
        } else if (id == R.id.nav_liquid_dashboard) {
            // 打开液晶仪表盘页面
            startActivity(new Intent(this, DashboardDemoActivity.class));
            navAdapter.setSelectedPosition(oldPosition); // 恢复选中项
            return;
        } else if (id == R.id.nav_calculator) {
            if (calculatorFragment == null) calculatorFragment = new CalculatorFragment();
            targetFragment = calculatorFragment;
            title = "计算器";
        } else if (id == R.id.nav_serial) {
            if (serialFragment == null) serialFragment = new SerialFragment();
            targetFragment = serialFragment;
            title = "串口调试";
        }

        // 切换 Fragment
        if (targetFragment != null && targetFragment != currentFragment) {
            loadFragment(targetFragment);
            binding.tvTitle.setText(title);
            currentFragment = targetFragment;
            navAdapter.setSelectedPosition(position);
        } else {
            // 点的是当前页，恢复选中
            navAdapter.setSelectedPosition(oldPosition);
        }
    }

    /**
     * 加载 Fragment（使用 hide/show 优化，避免重复创建）
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        // 隐藏所有已存在的 Fragment
        for (Fragment f : fm.getFragments()) {
            transaction.hide(f);
        }

        // 如果 Fragment 未添加过，则添加；否则显示
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment);
        }

        transaction.commitNowAllowingStateLoss();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


}
