package com.example.bicycle.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bicycle.R;
import com.example.bicycle.model.DesktopApp;

import java.util.List;

/**
 * 桌面页面适配器（ViewPager2）
 */
public class DesktopPageAdapter extends RecyclerView.Adapter<DesktopPageAdapter.PageViewHolder> {

    private List<List<DesktopApp>> pages; // 分页后的数据
    private static int columnCount = 4;
    private static int rowCount = 3;

    public DesktopPageAdapter(List<List<DesktopApp>> pages) {
        this.pages = pages;
    }

    public DesktopPageAdapter(List<List<DesktopApp>> pages, int columnCount, int rowCount) {
        this.pages = pages;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.page_desktop, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        holder.bind(pages.get(position));
    }

    @Override
    public int getItemCount() {
        return pages != null ? pages.size() : 0;
    }

    /**
     * ViewHolder
     */
    static class PageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout gridContainer;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            // 使用 itemView 本身作为容器（page_desktop.xml 的根布局）
            if (itemView instanceof LinearLayout) {
                gridContainer = (LinearLayout) itemView;
                gridContainer.setOrientation(LinearLayout.VERTICAL);
            } else {
                // 如果不是 LinearLayout，创建一个
                gridContainer = new LinearLayout(itemView.getContext());
                gridContainer.setOrientation(LinearLayout.VERTICAL);
                gridContainer.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                ));
                if (itemView instanceof ViewGroup) {
                    ((ViewGroup) itemView).addView(gridContainer);
                }
            }
        }

        public void bind(List<DesktopApp> apps) {
            gridContainer.removeAllViews();

            // 按行添加
            for (int row = 0; row < rowCount; row++) {
                LinearLayout rowLayout = new LinearLayout(gridContainer.getContext());
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0, 1.0f
                ));

                for (int col = 0; col < columnCount; col++) {
                    int index = row * columnCount + col;
                    if (index < apps.size()) {
                        DesktopApp app = apps.get(index);
                        View itemView = createAppItemView(app);
                        rowLayout.addView(itemView, new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1.0f
                        ));
                    } else {
                        // 空占位
                        View emptyView = new View(gridContainer.getContext());
                        emptyView.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1.0f
                        ));
                        rowLayout.addView(emptyView);
                    }
                }

                gridContainer.addView(rowLayout);
            }
        }

        /**
         * 创建单个应用视图
         */
        private View createAppItemView(DesktopApp app) {
            LinearLayout layout = new LinearLayout(gridContainer.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            layout.setPadding(12, 12, 12, 12);
            layout.setBackgroundResource(android.R.attr.selectableItemBackground);

            // 图标
            ImageView ivIcon = new ImageView(gridContainer.getContext());
            int iconSize = dpToPx(56);
            ivIcon.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
            ivIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            ivIcon.setImageResource(app.getIconResId());

            // 名称
            TextView tvName = new TextView(gridContainer.getContext());
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            textParams.setMargins(0, dpToPx(8), 0, 0);
            tvName.setLayoutParams(textParams);
            tvName.setText(app.getName());
            tvName.setTextColor(0xFF000000);
            tvName.setTextSize(12);
            tvName.setMaxLines(2);
            tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tvName.setGravity(android.view.Gravity.CENTER);

            layout.addView(ivIcon);
            layout.addView(tvName);

            // 点击事件
            layout.setOnClickListener(v -> {
                Toast.makeText(gridContainer.getContext(),
                        "点击了: " + app.getName(),
                        Toast.LENGTH_SHORT).show();
            });

            // 长按事件
            layout.setOnLongClickListener(v -> {
                Toast.makeText(gridContainer.getContext(),
                        "长按了: " + app.getName(),
                        Toast.LENGTH_SHORT).show();
                return true;
            });

            return layout;
        }

        private int dpToPx(int dp) {
            return (int) (dp * gridContainer.getContext().getResources().getDisplayMetrics().density);
        }
    }
}