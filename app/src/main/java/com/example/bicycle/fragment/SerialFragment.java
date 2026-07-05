package com.example.bicycle.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bicycle.R;
import com.example.bicycle.serial_utils.ByteUtil;
import com.example.bicycle.serial_utils.SerialPortHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SerialFragment extends Fragment {

    private static final int MAX_LOG_LINES = 500;

    // 视图
    private View rootView;
    private TextView tvLog;
    private TextView tvByteCount;
    private ScrollView scrollLog;

    private final StringBuilder logBuilder = new StringBuilder();
    private int totalBytes = 0;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_serial, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("SerialFragment", "创建");

        tvLog = view.findViewById(R.id.tv_log);
        tvByteCount = view.findViewById(R.id.tv_byte_count);
        scrollLog = view.findViewById(R.id.scroll_log);
        Button btnClear = view.findViewById(R.id.btn_clear);

        // 设置日志回调
        SerialPortHelper.getInstance().setCallback(payload -> {
            totalBytes += payload.length;
            String timestamp = sdf.format(new Date());
            String hexStr = ByteUtil.bytesToHex(payload);
            String line = /*"[" + timestamp + "] RX (" + payload.length + "B): " +*/ hexStr + "\n";

            logBuilder.append(line);

            // 限制日志行数，防止 OOM
            int newlineCount = 0;
            for (int i = 0; i < logBuilder.length(); i++) {
                if (logBuilder.charAt(i) == '\n') newlineCount++;
            }
            if (newlineCount >= MAX_LOG_LINES) {
                int mid = logBuilder.indexOf("\n", logBuilder.length() / 2);
                if (mid >= 0) logBuilder.delete(0, mid + 1);
            }

            tvLog.setText(logBuilder.toString());
            tvByteCount.setText(totalBytes + " 字节");

            // 自动滚动到底部
            //scrollLog.post(() -> scrollLog.fullScroll(ScrollView.FOCUS_DOWN));
        });

        // 清空按钮
        btnClear.setOnClickListener(v -> {
            logBuilder.setLength(0);
            totalBytes = 0;
            tvLog.setText("");
            tvByteCount.setText("0 字节");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        logBuilder.setLength(0);
        rootView = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("SerialFragment onResume", "");
        SerialPortHelper.getInstance().openSerial();
        handler.sendEmptyMessage(1);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("SerialFragment onPause", "");
        handler.removeMessages(1);
        SerialPortHelper.getInstance().closeSerial();
    }

    Random random = new Random();
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 1){
                handler.removeMessages(1);
                handler.sendEmptyMessageDelayed(1, 100);
                byte head_1 = (byte) 0xAB;
                byte head_2 = (byte) 0xBA;
                byte len_1 = (byte) 0x00;
                byte len_2 = (byte) 0x0A;
                byte data_1 = (byte) random.nextInt(255);
                byte data_2 = (byte) random.nextInt(255);
                byte data_3 = (byte) random.nextInt(255);
                byte data_4 = (byte) random.nextInt(255);
                byte data_5 = (byte) random.nextInt(255);
                byte data_6 = (byte) random.nextInt(255);
                byte data_7 = (byte) random.nextInt(255);
                byte data_8 = (byte) random.nextInt(255);
                byte data_9 = (byte) random.nextInt(255);
                byte data_10 = (byte) random.nextInt(255);
                byte sum = (byte) ((len_1 + len_2 + data_1 + data_2 + data_3 + data_4 + data_5 + data_6 + data_7 + data_8 + data_9 + data_10) & 0xFF);

                byte[] bytes = new byte[]{head_1, head_2, len_1, len_2, data_1, data_2,
                        data_3, data_4, data_5, data_6, data_7, data_8, data_9, data_10, sum};
                SerialPortHelper.getInstance().onSerialRawRead(bytes);
            }
            return false;
        }
    });

}
