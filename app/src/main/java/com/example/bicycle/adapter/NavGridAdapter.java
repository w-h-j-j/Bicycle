package com.example.bicycle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.bicycle.R;

import java.util.List;

/**
 * 底部导航 GridView 适配器
 */
public class NavGridAdapter extends BaseAdapter {

    public static class NavItem {
        public final int id;
        public final int iconRes;
        public final String title;

        public NavItem(int id, int iconRes, String title) {
            this.id = id;
            this.iconRes = iconRes;
            this.title = title;
        }
    }

    private final Context context;
    private final List<NavItem> items;
    private int selectedPosition = 0;

    private final int colorSelected;
    private final int colorNormal;

    public NavGridAdapter(Context context, List<NavItem> items) {
        this.context = context;
        this.items = items;
        this.colorSelected = ContextCompat.getColor(context, R.color.nav_selected);
        this.colorNormal = ContextCompat.getColor(context, R.color.nav_normal);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        int old = selectedPosition;
        selectedPosition = position;
        // 只刷新变化的项
        if (old >= 0 && old < items.size()) notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public NavItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_nav_grid, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NavItem item = getItem(position);
        boolean isSelected = (position == selectedPosition);

        holder.ivIcon.setImageResource(item.iconRes);
        holder.ivIcon.setColorFilter(isSelected ? colorSelected : colorNormal);

        holder.tvTitle.setText(item.title);
        holder.tvTitle.setTextColor(isSelected ? colorSelected : colorNormal);

        return convertView;
    }

    static class ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;

        ViewHolder(View view) {
            ivIcon = view.findViewById(R.id.iv_icon);
            tvTitle = view.findViewById(R.id.tv_title);
        }
    }
}
