package com.hjst.gather.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvishew.xlog.XLog;
import com.hjst.gather.model.InfoBean;
import com.hjst.gather.databinding.FragmentOkhttpTestBinding;
import com.hjst.gather.http_utils.HttpUtils;
import com.hjst.gather.ui.InfoRecordAdapter;
import com.hjst.gather.utils.UtilTools;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 云端数据测试页面：拉取 AutoService 每 10 秒上报的历史记录列表并展示
 */
public class OkHttpTestFragment extends Fragment {

    /** 自动刷新间隔，与 AutoService 上报周期对齐 */
    private static final long AUTO_REFRESH_INTERVAL_MS = 10_000L;

    private FragmentOkhttpTestBinding binding;
    private InfoRecordAdapter adapter;

    /** 主线程 Handler + 复用同一个 Runnable，不每轮新建对象 */
    private final Handler refreshHandler = new Handler(Looper.getMainLooper());
    /** 请求进行中标志：上一发未回来就跳过本轮定时拉取，避免请求叠加 */
    private boolean isLoading;
    /** 手指是否按在列表上：按住期间刷新不打断用户浏览，抬起才回到顶部 */
    private boolean userTouching;

    private final Runnable autoRefreshTask = new Runnable() {
        @Override
        public void run() {
            loadHistory(false);
            refreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOkhttpTestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new InfoRecordAdapter();
        binding.rvRecords.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecords.setAdapter(adapter);
        // 行高固定，跳过每次刷新的重新测量
        binding.rvRecords.setHasFixedSize(true);
        // 只记录触摸状态、不消费事件，不影响列表正常滚动
        binding.rvRecords.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    userTouching = true;
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    userTouching = false;
                    break;
            }
            return false;
        });

        binding.btnRefresh.setOnClickListener(v -> loadHistory(true));

        // 进入页面立即拉一次，之后每 10 秒静默自动刷新
        loadHistory(true);
        refreshHandler.postDelayed(autoRefreshTask, AUTO_REFRESH_INTERVAL_MS);
    }

    /**
     * 拉取上报历史列表（HttpUtils 回调已切回主线程）
     *
     * @param manual 手动刷新才有按钮禁用/失败 Toast，定时刷新静默进行为
     */
    private void loadHistory(boolean manual) {
        if (binding == null || isLoading) return;
        isLoading = true;
        if (manual) {
            binding.tvStatus.setText("正在拉取...");
            binding.btnRefresh.setEnabled(false);
        }

        HttpUtils.getInstance().getDeviceInfo(UtilTools.getDeviceId(), new HttpUtils.ResultCallback<List<InfoBean>>() {
            @Override
            public void onSuccess(@Nullable List<InfoBean> data) {
                isLoading = false;
                if (binding == null) return;
                binding.btnRefresh.setEnabled(true);
                int count = data == null ? 0 : data.size();
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                binding.tvStatus.setText((manual ? "手动拉取" : "自动刷新")
                        + "：共 " + count + " 条记录，更新于 " + time);
                binding.tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                adapter.submitList(data);
                // 手指没触碰屏幕时回到顶部看最新记录；正在触摸则保持当前位置不打断用户
                if (!userTouching) {
                    binding.rvRecords.scrollToPosition(0);
                }
                XLog.d("云端数据测试：拉取到历史记录 " + count + " 条");
            }

            @Override
            public void onFailure(@NonNull String message) {
                isLoading = false;
                if (binding == null) return;
                binding.btnRefresh.setEnabled(true);
                binding.tvStatus.setText("拉取失败：" + message);
                // 自动失败只记状态行，不弹 Toast 骚扰
                if (manual) {
                    Toast.makeText(requireContext(), "拉取失败：" + message, Toast.LENGTH_SHORT).show();
                }
                XLog.e("云端数据测试：拉取失败 " + message);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 页面销毁必须停表，否则 Handler 持有 Fragment 泄漏且后台空转发请求
        refreshHandler.removeCallbacks(autoRefreshTask);
        binding = null;
    }

}
