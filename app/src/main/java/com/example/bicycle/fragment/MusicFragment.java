package com.example.bicycle.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bicycle.R;
import com.example.bicycle.XLog;
import com.example.bicycle.adapter.MusicAdapter;
import com.example.bicycle.databinding.FragmentMusicBinding;
import com.example.bicycle.model.Music;
import com.example.bicycle.service.MusicPlayerService;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐播放器 Fragment
 * 功能：
 * 1. 加载本地音乐列表
 * 2. 播放/暂停控制
 * 3. 上一曲/下一曲切换
 * 4. 进度条拖动
 */
public class MusicFragment extends Fragment {

    private FragmentMusicBinding binding;
    private MusicPlayerService musicService;
    private boolean isBound = false;
    private MusicAdapter musicAdapter;
    private List<Music> musicList = new ArrayList<>();
    private Handler progressHandler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicPlayerBinder binder = (MusicPlayerService.MusicPlayerBinder) service;
            musicService = binder.getService();
            isBound = true;
            musicService.setPlaylist(musicList);
            XLog.d("音乐服务已连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            musicService = null;
            XLog.d("音乐服务已断开");
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        XLog.d("MusicFragment 创建");
        initRecyclerView();
        bindService();
        initControls();
        checkAndRequestPermission();
    }

    private void initRecyclerView() {
        musicAdapter = new MusicAdapter(musicList);
        musicAdapter.setOnItemClickListener(position -> {
            if (isBound && musicService != null) {
                musicService.playMusic(position);
                updatePlayingUI();
                updatePlayPauseButton();
                startProgressUpdate();
            }
        });

        binding.recyclerMusicList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerMusicList.setAdapter(musicAdapter);
    }

    private void bindService() {
        Intent intent = new Intent(requireContext(), MusicPlayerService.class);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initControls() {
        binding.btnPlayPause.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.togglePlayPause();
                updatePlayPauseButton();
                updatePlayingUI();
                if (musicService.isPlaying()) {
                    startProgressUpdate();
                } else {
                    stopProgressUpdate();
                }
            }
        });

        binding.btnPrevious.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playPrevious();
                updatePlayingUI();
                updatePlayPauseButton();
                startProgressUpdate();
            }
        });

        binding.btnNext.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                musicService.playNext();
                updatePlayingUI();
                updatePlayPauseButton();
                startProgressUpdate();
            }
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void checkAndRequestPermission() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            loadMusicList();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "权限已授予，正在加载音乐...", Toast.LENGTH_SHORT).show();
                loadMusicList();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("需要存储权限")
                .setMessage("音乐播放器需要访问您的存储空间以加载音乐文件。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    binding.tvMusicCount.setText("需要存储权限");
                })
                .setCancelable(false)
                .show();
    }

    private void loadMusicList() {
        new Thread(() -> {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.DATA
            };

            String selection = MediaStore.Audio.Media.DURATION + " > 0";
            String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

            Cursor cursor = requireContext().getContentResolver().query(uri, projection, selection, null, sortOrder);
            if (cursor != null) {
                musicList.clear();
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                while (cursor.moveToNext()) {
                    String title = cursor.getString(titleColumn);
                    if (title != null && !title.isEmpty()) {
                        Music music = new Music(
                                cursor.getLong(idColumn),
                                title,
                                cursor.getString(artistColumn) != null ? cursor.getString(artistColumn) : "未知艺术家",
                                cursor.getString(albumColumn) != null ? cursor.getString(albumColumn) : "未知专辑",
                                cursor.getLong(durationColumn),
                                cursor.getString(dataColumn),
                                0
                        );
                        musicList.add(music);
                    }
                }
                cursor.close();

                requireActivity().runOnUiThread(() -> {
                    musicAdapter.notifyDataSetChanged();
                    binding.tvMusicCount.setText(musicList.size() + " 首歌曲");
                });
            }
        }).start();
    }

    private void updatePlayPauseButton() {
        if (musicService != null && musicService.isPlaying()) {
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void updatePlayingUI() {
        if (musicService != null) {
            Music currentMusic = musicService.getCurrentMusic();
            if (currentMusic != null) {
                binding.tvCurrentTitle.setText(currentMusic.getTitle());
                binding.tvCurrentArtist.setText(currentMusic.getArtist());
                binding.tvTotalDuration.setText(currentMusic.getFormattedDuration());
            }
            musicAdapter.setPlayingPosition(musicService.getCurrentPosition());
            updatePlayPauseButton();
        }
    }

    private void startProgressUpdate() {
        if (progressRunnable == null) {
            progressRunnable = () -> {
                if (isBound && musicService != null) {
                    int currentProgress = musicService.getCurrentProgress();
                    int duration = musicService.getDuration();
                    if (duration > 0) {
                        binding.seekBar.setMax(duration);
                        binding.seekBar.setProgress(currentProgress);
                        binding.tvCurrentProgress.setText(formatTime(currentProgress));
                        binding.tvTotalDuration.setText(formatTime(duration));
                    }
                    updatePlayPauseButton();
                }
                progressHandler.postDelayed(progressRunnable, 500);
            };
        }
        progressHandler.post(progressRunnable);
    }

    private void stopProgressUpdate() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = milliseconds / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopProgressUpdate();
        if (isBound) {
            requireActivity().unbindService(serviceConnection);
            isBound = false;
        }
        binding = null;
    }
}