package com.example.bicycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.bicycle.databinding.ActivitySplashBinding;

/**
 * 闪屏页 - 第二版（精致版）
 * 保留进度动画和状态文字，停留时间调整为 2.5 秒
 */
public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private static final int SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        XLog.d("闪屏页启动 - 精致版 2.5秒");
        playEnhancedAnimations();
    }

    /**
     * 精致版动画序列（第二版风格，保持2.5秒停留）
     */
    private void playEnhancedAnimations() {
        // 1. 装饰元素淡入 (0ms)
        animateDecoElements();

        // 2. Logo 弹性缩放 + 光晕 (100ms)
        animateView(binding.logoContainer, 100, 900, 0.0f, 1.0f, true);

        // 3. 应用名称上滑淡入 (300ms)
        animateView(binding.tvAppName, 300, 700, 0.0f, 1.0f, false);

        // 4. 标语下滑淡入 (450ms)
        animateView(binding.tvSlogan, 450, 700, 0.0f, 1.0f, false);

        // 5. 加载状态 + 进度条 (600ms)
        animateView(binding.tvLoading, 600, 500, 0.0f, 1.0f, false);
        animateView(binding.progressRing, 650, 500, 0.0f, 1.0f, false);
        animateView(binding.tvProgress, 700, 400, 0.0f, 1.0f, false);

        // 6. 版本信息淡入 (850ms)
        animateView(binding.tvVersion, 850, 400, 0.0f, 0.5f, false);

        // 7. 启动进度动画（1秒内快速完成）
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            startFastProgressAnimation();
        }, 900);

        // 8. 2.5秒后跳转主界面
        handler.postDelayed(this::navigateToMain, SPLASH_DURATION);
    }

    /**
     * 装饰元素动画（圆环 + 线条）
     */
    private void animateDecoElements() {
        animateView(binding.decoCircle1, 0, 1200, 0.0f, 0.08f, false);
        animateView(binding.decoCircle2, 200, 1200, 0.0f, 0.06f, false);
        animateView(binding.decoLine1, 400, 1000, 0.0f, 0.0f, false);
        animateView(binding.decoLine2, 600, 1000, 0.0f, 0.0f, false);
    }

    /**
     * 单个 View 的入场动画
     */
    private void animateView(View view, int delayMs, int durationMs,
                             float fromAlpha, float toAlpha, boolean isLogo) {
        view.setAlpha(fromAlpha);
        view.setTranslationY(20f);

        if (isLogo) {
            view.setScaleX(0.5f);
            view.setScaleY(0.5f);
        }

        view.postDelayed(() -> {
            ViewPropertyAnimator animator = view.animate()
                    .alpha(toAlpha)
                    .translationY(0f)
                    .setDuration(durationMs)
                    .setInterpolator(isLogo
                            ? new OvershootInterpolator(2.0f)
                            : new DecelerateInterpolator(1.5f));

            if (isLogo) {
                animator.scaleX(1.0f).scaleY(1.0f);
            }

            animator.start();
        }, delayMs);
    }

    /**
     * 快速进度条动画（1秒内 0% → 100%，适配 2.5 秒停留）
     */
    private void startFastProgressAnimation() {
        Handler handler = new Handler(Looper.getMainLooper());
        int totalSteps = 100;
        int stepDuration = 10;  // 每步 10ms，总共 1 秒

        for (int i = 1; i <= totalSteps; i++) {
            int progress = i;
            handler.postDelayed(() -> {
                if (binding != null) {
                    binding.progressRing.setProgress(progress);
                    binding.tvProgress.setText(progress + "%");

                    if (progress < 40) {
                        binding.tvLoading.setText("正在初始化...");
                    } else if (progress < 70) {
                        binding.tvLoading.setText("加载资源中...");
                    } else if (progress < 95) {
                        binding.tvLoading.setText("准备就绪...");
                    } else {
                        binding.tvLoading.setText("即将进入");
                    }
                }
            }, i * stepDuration);
        }
    }

    /**
     * 跳转到主界面（丝滑过渡）
     */
    private void navigateToMain() {
        XLog.d("跳转到主界面");

        binding.splashRoot.animate()
                .alpha(0f)
                .setDuration(500)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                    finish();
                })
                .start();
    }

    @Override
    public void onBackPressed() {
        // 闪屏页禁止返回
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding.splashRoot.animate().cancel();
        }
    }
}