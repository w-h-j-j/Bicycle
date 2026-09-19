package com.hjst.gather.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.hjst.gather.model.InfoBean;
import com.hjst.gather.databinding.ItemInfoRecordBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 上报历史记录列表适配器（云端数据测试页专用）
 */
public class InfoRecordAdapter extends RecyclerView.Adapter<InfoRecordAdapter.ViewHolder> {

    private final List<InfoBean> items = new ArrayList<>();

    /**
     * 刷新数据源：DiffUtil 差量更新，只重绘增删/变化的条目，
     * 避免 notifyDataSetChanged 全量重建 item
     */
    public void submitList(@Nullable List<InfoBean> list) {
        List<InfoBean> newItems = list == null ? new ArrayList<>() : new ArrayList<>(list);
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new InfoDiffCallback(items, newItems));
        items.clear();
        items.addAll(newItems);
        diff.dispatchUpdatesTo(this);
    }

    /** 以 time 作为条目唯一标识，内容比较用 InfoBean.equals */
    private static class InfoDiffCallback extends DiffUtil.Callback {

        private final List<InfoBean> oldList;
        private final List<InfoBean> newList;

        InfoDiffCallback(List<InfoBean> oldList, List<InfoBean> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            String oldTime = oldList.get(oldPos).getTime();
            String newTime = newList.get(newPos).getTime();
            return oldTime == null ? newTime == null : oldTime.equals(newTime);
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            // 序号按位置计算：位置变了（新记录插入导致下移）就必须重绑，
            // 否则 DiffUtil 跳过绑定，前面的序号会停留在旧值
            return oldPos == newPos && oldList.get(oldPos).equals(newList.get(newPos));
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInfoRecordBinding binding = ItemInfoRecordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InfoBean bean = items.get(position);
        holder.binding.tvIndex.setText(String.valueOf(position + 1));
        holder.binding.tvTime.setText(bean.getTime());
        // 服务端数字带小数（79.0），展示时取整
        holder.binding.tvBattery.setText(String.format("%.0f%%", bean.getBattery()));
        holder.binding.tvSignal.setText(String.format("%.0fdBm", bean.getSignal()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ItemInfoRecordBinding binding;

        ViewHolder(ItemInfoRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
