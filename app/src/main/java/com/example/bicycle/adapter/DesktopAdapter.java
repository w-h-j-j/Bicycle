package com.example.bicycle.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bicycle.R;
import com.example.bicycle.model.DesktopApp;

import java.util.List;

/**
 * 桌面应用适配器
 */
public class DesktopAdapter extends RecyclerView.Adapter<DesktopAdapter.DesktopViewHolder> {

    private List<DesktopApp> appList;
    private OnItemClickListener clickListener;

    public DesktopAdapter(List<DesktopApp> appList) {
        this.appList = appList;
    }

    @NonNull
    @Override
    public DesktopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_desktop_app, parent, false);
        return new DesktopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DesktopViewHolder holder, int position) {
        DesktopApp app = appList.get(position);
        holder.bind(app);

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(app, position);
            }
        });

        // 长按事件
        holder.itemView.setOnLongClickListener(v -> {
            if (clickListener != null) {
                return clickListener.onItemLongClick(app, position);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return appList != null ? appList.size() : 0;
    }

    /**
     * ViewHolder
     */
    static class DesktopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName;

        public DesktopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvName = itemView.findViewById(R.id.tv_name);
        }

        public void bind(DesktopApp app) {
            ivIcon.setImageResource(app.getIconResId());
            tvName.setText(app.getName());
        }
    }

    /**
     * 设置点击监听
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * 点击监听接口
     */
    public interface OnItemClickListener {
        void onItemClick(DesktopApp app, int position);
        boolean onItemLongClick(DesktopApp app, int position);
    }
}