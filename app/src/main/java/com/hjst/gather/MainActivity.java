package com.hjst.gather;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.elvishew.xlog.XLog;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

import com.hjst.gather.databinding.ActivityMainBinding;
import com.hjst.gather.fragments.SerialPortFragment;
import com.hjst.gather.model.GridItemInfo;
import com.hjst.gather.ui.GridMenuAdapter;
import com.hjst.gather.fragments.DashboardFragment;
import com.hjst.gather.fragments.OkHttpTestFragment;
import com.hjst.gather.fragments.TTSFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private final List<GridItemInfo> menuItems = new ArrayList<>();

    /** 存储权限请求器（Android 6.0 ~ 10） */
    private ActivityResultLauncher<String[]> storagePermissionLauncher;
    /** 所有文件访问权限请求器（Android 11+） */
    private ActivityResultLauncher<Intent> manageStoragePermissionLauncher;
    /** 通知权限请求器（Android 13+） */
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // 注册权限回调
        registerPermissionLaunchers();

        // 请求存储权限
        requestStoragePermissions();

        // 申请通知权限并启动设备信息自动上报前台服务
        requestNotificationPermission();
        startAutoService();

        // 初始化菜单数据
        initMenuData();

        // 初始化 RecyclerView（3列网格）
        binding.rvMenu.setLayoutManager(new GridLayoutManager(this, 3));
        GridMenuAdapter adapter = new GridMenuAdapter(menuItems);
        adapter.setOnItemClickListener(new GridMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GridItemInfo item, int position) {
                navigateToFragment(item);
            }
        });
        binding.rvMenu.setAdapter(adapter);
    }

    /**
     * 构建菜单数据源 —— 后续新增功能模块在此追加即可
     */
    private void initMenuData() {
        menuItems.add(new GridItemInfo(
                R.drawable.ic_dashboard,
                "仪表盘",
                R.drawable.bg_icon_circle_blue,
                DashboardFragment.class.getName()
        ));
        menuItems.add(new GridItemInfo(
                R.drawable.ic_map_marker,
                "串口调试",
                R.drawable.bg_icon_circle_green,
                SerialPortFragment.class.getName()
        ));
        menuItems.add(new GridItemInfo(
                R.drawable.ic_tts,
                "语音播报",
                R.drawable.bg_icon_circle_orange,
                TTSFragment.class.getName()
        ));
        menuItems.add(new GridItemInfo(
                R.drawable.ic_network,
                "云端数据",
                R.drawable.bg_icon_circle_purple,
                OkHttpTestFragment.class.getName()
        ));
    }

    /**
     * 点击 Item → 通过反射创建 Fragment 并覆盖显示
     * 后续新增 Fragment 只需在 initMenuData() 加一条数据即可，无需改此处
     */
    private void navigateToFragment(GridItemInfo item) {
        Fragment targetFragment;
        try {
            Class<?> clazz = Class.forName(item.getFragmentClassName());
            targetFragment = (Fragment) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            XLog.e(TAG + "   Fragment 类未找到：" + item.getFragmentClassName() + " - " + e.getMessage());
            return;
        } catch (Exception e) {
            XLog.e(TAG + "   Fragment 实例化失败：" + item.getFragmentClassName() + " - " + e.getMessage());
            return;
        }

        // 隐藏网格菜单，显示 Fragment 容器
        binding.layoutGrid.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);

        // 加载 Fragment
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.fragment_container, targetFragment, item.getFragmentClassName())
                .commit();
    }

    /**
     * Fragment 返回主界面
     */
    public void navigateBack() {
        // 移除 Fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
        // 隐藏 Fragment 容器，恢复网格菜单
        binding.fragmentContainer.setVisibility(View.GONE);
        binding.layoutGrid.setVisibility(View.VISIBLE);
    }

    // ==================== 存储权限 ====================

    private void registerPermissionLaunchers() {
        // Android 6.0 ~ 10：申请读写外部存储
        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = result.values().stream().allMatch(granted -> granted);
                    if (allGranted) {
                        XLog.d(TAG + "   存储权限已授予");
                    } else {
                        XLog.w(TAG + "   存储权限被拒绝，部分功能可能不可用");
                        Toast.makeText(this, "存储权限未授予，部分功能可能不可用", Toast.LENGTH_LONG).show();
                    }
                });

        // Android 11+：申请所有文件访问权限
        manageStoragePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            XLog.d(TAG + "   所有文件访问权限已授予");
                        } else {
                            XLog.w(TAG + "   所有文件访问权限被拒绝");
                            Toast.makeText(this, "文件访问权限未授予，部分功能可能不可用", Toast.LENGTH_LONG).show();
                        }
                    }
                });

        // Android 13+：申请通知权限（拒绝不影响服务运行，仅不显示通知）
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        XLog.d(TAG + "   通知权限已授予");
                    } else {
                        XLog.w(TAG + "   通知权限被拒绝，前台服务通知将不显示");
                    }
                });
    }

    // ==================== 自动上报服务 ====================

    /**
     * 启动设备信息定时上报前台服务（重复启动幂等）
     */
    private void startAutoService() {
        ContextCompat.startForegroundService(this, new Intent(this, AutoService.class));
        XLog.d(TAG + "   AutoService 已启动");
    }

    /**
     * Android 13+ 通知为运行时权限，需动态申请；低版本无需处理
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+：需要引导用户跳转到系统设置页开启「所有文件访问权限」
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                manageStoragePermissionLauncher.launch(intent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 ~ 10：动态申请读写权限
            List<String> needRequest = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                needRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                needRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!needRequest.isEmpty()) {
                storagePermissionLauncher.launch(needRequest.toArray(new String[0]));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            navigateBack();
        } else {
            super.onBackPressed();
        }
    }
}
