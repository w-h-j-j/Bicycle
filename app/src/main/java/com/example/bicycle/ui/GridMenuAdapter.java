package com.example.bicycle.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bicycle.R;
import com.example.bicycle.model.GridItemInfo;

import java.util.List;

/**
 * 主界面网格菜单适配器
 */
public class GridMenuAdapter extends RecyclerView.Adapter<GridMenuAdapter.ViewHolder> {

    private final List<GridItemInfo> itemList;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(GridItemInfo item, int position);
    }

    public GridMenuAdapter(List<GridItemInfo> itemList) {
        this.itemList = itemList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grid_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GridItemInfo item = itemList.get(position);

        holder.ivIcon.setImageResource(item.getIconRes());
        holder.tvTitle.setText(item.getTitle());
        holder.iconContainer.setBackgroundResource(item.getBgColorRes());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickListener != null) {
                    clickListener.onItemClick(item, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        FrameLayout iconContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            iconContainer = itemView.findViewById(R.id.icon_container);
        }
    }
}
