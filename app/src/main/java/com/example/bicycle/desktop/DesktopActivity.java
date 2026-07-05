package com.example.bicycle.desktop;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bicycle.R;
import com.example.bicycle.adapter.DesktopPageAdapter;
import com.example.bicycle.databinding.ActivityDesktopBinding;
import com.example.bicycle.model.DesktopApp;

import java.util.ArrayList;
import java.util.List;

/**
 * 桌面应用展示页面（使用 ViewPager2 实现左右翻页）
 */
public class DesktopActivity extends AppCompatActivity {

    private ActivityDesktopBinding binding;
    private DesktopPageAdapter pageAdapter;
    private List<List<DesktopApp>> pages; // 分页数据

    // 页面指示器
    private List<ImageView> dotViews = new ArrayList<>();

    // 行列配置
    private static final int COLUMN_COUNT = 4;
    private static final int ROW_COUNT = 3;
    private static final int ITEMS_PER_PAGE = COLUMN_COUNT * ROW_COUNT; // 每页 12 个

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDesktopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        loadData();
        setupViewPager();
    }

    private void initViews() {
        // 返回按钮
        binding.ivBack.setOnClickListener(v -> finish());
    }

    /**
     * 加载示例数据
     */
    private void loadData() {
        List<DesktopApp> appList = new ArrayList<>();

        // 模拟应用数据
        String[] appNames = {
                "电话", "短信", "相机", "相册",
                "音乐", "视频", "时钟", "天气",
                "日历", "计算器", "文件", "设置",
                "浏览器", "地图", "邮件", "应用商店",
                "微信", "QQ", "支付宝", "淘宝",
                "抖音", "微博", "快手", "B站",
                "知乎", "豆瓣", "小红书", "美团",
                "饿了么", "滴滴", "高德", "百度",
                "网易云", "酷狗", "QQ音乐", "喜马拉雅",
                "今日头条", "腾讯新闻", "优酷", "爱奇艺",
                "腾讯视频", "芒果TV", "WPS", "钉钉"
        };

        int[] iconResources = {
                android.R.drawable.sym_action_call,
                android.R.drawable.sym_action_email,
                android.R.drawable.ic_menu_camera,
                android.R.drawable.ic_menu_gallery,
                android.R.drawable.ic_media_play,
                android.R.drawable.ic_menu_today,
                android.R.drawable.ic_menu_compass,
                android.R.drawable.ic_menu_info_details
        };

        for (int i = 0; i < appNames.length; i++) {
            int iconRes = iconResources[i % iconResources.length];
            appList.add(new DesktopApp(appNames[i], iconRes));
        }

        // 分页
        pages = new ArrayList<>();
        for (int i = 0; i < appList.size(); i += ITEMS_PER_PAGE) {
            int end = Math.min(i + ITEMS_PER_PAGE, appList.size());
            pages.add(appList.subList(i, end));
        }
    }

    /**
     * 设置 ViewPager2
     */
    private void setupViewPager() {
        pageAdapter = new DesktopPageAdapter(pages, COLUMN_COUNT, ROW_COUNT);
        binding.viewPager.setAdapter(pageAdapter);

        // 设置页面切换监听
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePageIndicator(position, pages.size());
            }
        });

        // 设置页面指示器
        setupPageIndicator(pages.size());
        updatePageIndicator(0, pages.size());
    }

    /**
     * 设置页面指示器
     */
    private void setupPageIndicator(int count) {
        dotViews.clear();
        binding.dotContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            int size = dpToPx(8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            dot.setImageResource(android.R.drawable.radiobutton_off_background);
            dot.setScaleType(ImageView.ScaleType.FIT_CENTER);

            binding.dotContainer.addView(dot);
            dotViews.add(dot);
        }
    }

    /**
     * 更新页面指示器
     */
    private void updatePageIndicator(int currentPage, int totalPages) {
        if (binding == null) return;
        
        // 更新页码文字
        binding.tvPageInfo.setText((currentPage + 1) + "/" + totalPages);

        // 更新点指示器
        for (int i = 0; i < dotViews.size(); i++) {
            if (i == currentPage) {
                // 当前页 - 选中状态
                dotViews.get(i).setColorFilter(getColor(R.color.purple_500));
            } else {
                // 非当前页 - 未选中状态
                dotViews.get(i).setColorFilter(getColor(android.R.color.darker_gray));
            }
        }
    }

    /**
     * dp 转 px
     */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}