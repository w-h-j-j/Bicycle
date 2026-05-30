package com.example.bicycle.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bicycle.R;
import com.example.bicycle.model.Music;

import java.util.List;

/**
 * 音乐列表适配器
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private List<Music> musicList;
    private int playingPosition = -1;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public MusicAdapter(List<Music> musicList) {
        this.musicList = musicList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * 更新正在播放的位置
     */
    public void setPlayingPosition(int position) {
        int oldPosition = playingPosition;
        playingPosition = position;
        if (oldPosition >= 0) notifyItemChanged(oldPosition);
        if (playingPosition >= 0) notifyItemChanged(playingPosition);
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Music music = musicList.get(position);
        holder.tvTitle.setText(music.getTitle());
        holder.tvArtist.setText(music.getArtist());
        holder.tvDuration.setText(music.getFormattedDuration());

        // 显示播放图标
        if (position == playingPosition) {
            holder.tvPlayIcon.setVisibility(View.VISIBLE);
            holder.tvPlayIcon.setText("▶");
        } else {
            holder.tvPlayIcon.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList != null ? musicList.size() : 0;
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlayIcon;
        TextView tvTitle;
        TextView tvArtist;
        TextView tvDuration;

        MusicViewHolder(View itemView) {
            super(itemView);
            tvPlayIcon = itemView.findViewById(R.id.tv_play_icon);
            tvTitle = itemView.findViewById(R.id.tv_music_title);
            tvArtist = itemView.findViewById(R.id.tv_music_artist);
            tvDuration = itemView.findViewById(R.id.tv_music_duration);
        }
    }
}