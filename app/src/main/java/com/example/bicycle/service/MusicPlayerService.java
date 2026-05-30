package com.example.bicycle.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import com.example.bicycle.model.Music;
import com.example.bicycle.XLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 音乐播放服务
 * 负责音乐的播放、暂停、上下曲切换
 */
public class MusicPlayerService extends Service {

    private MediaPlayer mediaPlayer;
    private List<Music> musicList = new ArrayList<>();
    private int currentPosition = 0;
    private boolean isPlaying = false;

    // Binder 用于 Activity 与 Service 通信
    private final IBinder binder = new MusicPlayerBinder();

    public class MusicPlayerBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            // 播放完成后自动下一曲
            playNext();
        });
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            XLog.e("MediaPlayer 错误: " + what + ", " + extra);
            return false;
        });
    }

    /**
     * 设置播放列表
     */
    public void setPlaylist(List<Music> playlist) {
        this.musicList = playlist;
    }

    /**
     * 获取播放列表
     */
    public List<Music> getPlaylist() {
        return musicList;
    }

    /**
     * 播放指定索引的歌曲
     */
    public void playMusic(int position) {
        if (musicList.isEmpty() || position < 0 || position >= musicList.size()) {
            XLog.w("播放列表为空或索引越界");
            return;
        }

        currentPosition = position;
        Music music = musicList.get(position);

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(music.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            XLog.d("播放: " + music.getTitle() + " - " + music.getArtist());
        } catch (IOException e) {
            XLog.e("播放音乐失败: " + e.getMessage());
            e.printStackTrace();
            isPlaying = false;
        }
    }

    /**
     * 播放/暂停
     */
    public void togglePlayPause() {
        if (mediaPlayer == null) return;

        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
    }

    /**
     * 播放下一曲
     */
    public void playNext() {
        if (musicList.isEmpty()) return;
        currentPosition = (currentPosition + 1) % musicList.size();
        playMusic(currentPosition);
    }

    /**
     * 播放上一曲
     */
    public void playPrevious() {
        if (musicList.isEmpty()) return;
        currentPosition = (currentPosition - 1 + musicList.size()) % musicList.size();
        playMusic(currentPosition);
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 获取当前播放位置
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * 获取当前播放进度（毫秒）
     */
    public int getCurrentProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 获取当前歌曲时长
     */
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * 获取当前播放的歌曲
     */
    public Music getCurrentMusic() {
        if (musicList.isEmpty() || currentPosition < 0 || currentPosition >= musicList.size()) {
            return null;
        }
        return musicList.get(currentPosition);
    }

    /**
     * 跳转到指定进度
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        XLog.d("音乐播放服务已销毁");
    }
}