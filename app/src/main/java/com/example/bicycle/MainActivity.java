package com.example.bicycle;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

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

    private final DashboardFragment dashboardFragment = new DashboardFragment();
    private final MusicFragment musicFragment = new MusicFragment();
    private final CalculatorFragment calculatorFragment = new CalculatorFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_new);

        XLog.d("主页面启动（Fragment 架构）");

        // 默认加载仪表盘
        if (savedInstanceState == null) {
            loadFragment(dashboardFragment);
            binding.tvTitle.setText("仪表盘");
        }

        // 底部导航栏监听
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(dashboardFragment);
                binding.tvTitle.setText("仪表盘");
                return true;
            } else if (id == R.id.nav_music) {
                loadFragment(musicFragment);
                binding.tvTitle.setText("音乐播放器");
                return true;
            } else if (id == R.id.nav_calculator) {
                loadFragment(calculatorFragment);
                binding.tvTitle.setText("计算器");
                return true;
            }
            return false;
        });
    }

    /**
     * 加载 Fragment
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
