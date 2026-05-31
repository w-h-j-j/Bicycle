package com.example.bicycle;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bicycle.databinding.ActivityMainNewBinding;
import com.example.bicycle.fragment.CalculatorFragment;
import com.example.bicycle.fragment.DashboardFragment;
import com.example.bicycle.fragment.MusicFragment;

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

    private Fragment currentFragment;

    // 防抖：两次点击间隔小于 300ms 则忽略
    private long lastClickTime = 0;
    private static final long CLICK_INTERVAL = 300;

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

        // 底部导航栏监听
        binding.bottomNav.setOnItemSelectedListener(item -> {
            // 防抖处理
            long now = System.currentTimeMillis();
            if (now - lastClickTime < CLICK_INTERVAL) {
                return currentFragment != null;
            }
            lastClickTime = now;

            int id = item.getItemId();
            Fragment targetFragment = null;
            String title = "";

            if (id == R.id.nav_dashboard) {
                if (dashboardFragment == null) dashboardFragment = new DashboardFragment();
                targetFragment = dashboardFragment;
                title = "仪表盘";
            } else if (id == R.id.nav_music) {
                if (musicFragment == null) musicFragment = new MusicFragment();
                targetFragment = musicFragment;
                title = "音乐播放器";
            } else if (id == R.id.nav_calculator) {
                if (calculatorFragment == null) calculatorFragment = new CalculatorFragment();
                targetFragment = calculatorFragment;
                title = "计算器";
            }

            // 避免重复加载同一个 Fragment
            if (targetFragment != null && targetFragment != currentFragment) {
                loadFragment(targetFragment);
                binding.tvTitle.setText(title);
                currentFragment = targetFragment;
            }
            return true;
        });
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
}
