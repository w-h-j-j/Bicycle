package com.example.bicycle;

import android.os.Bundle;
import android.view.View;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.bicycle.databinding.ActivityMainBinding;
import com.example.bicycle.fragments.MapFragment;
import com.example.bicycle.model.GridItemInfo;
import com.example.bicycle.ui.GridMenuAdapter;
import com.example.bicycle.fragments.DashboardFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private final List<GridItemInfo> menuItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

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
                "地图",
                R.drawable.bg_icon_circle_green,
                MapFragment.class.getName()
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
            Log.e(TAG, "Fragment 类未找到：" + item.getFragmentClassName(), e);
            return;
        } catch (Exception e) {
            Log.e(TAG, "Fragment 实例化失败：" + item.getFragmentClassName(), e);
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

    @Override
    public void onBackPressed() {
        if (binding.fragmentContainer.getVisibility() == View.VISIBLE) {
            navigateBack();
        } else {
            super.onBackPressed();
        }
    }
}
