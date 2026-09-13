package com.example.bicycle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 闪屏页 —— 停留 2 秒后自动跳转至 MainActivity
 */
public class SplashActivity extends AppCompatActivity {

    /** 闪屏总停留时长（毫秒） */
    private static final long SPLASH_DURATION = 2000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 隐藏系统导航栏和状态栏，实现全屏沉浸
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        startAnimations();
    }

    private void startAnimations() {
        // ===== 1. Logo 容器：弹性缩放 + 淡入 =====
        View logoContainer = findViewById(R.id.splash_logo_container);
        logoContainer.setScaleX(0.3f);
        logoContainer.setScaleY(0.3f);
        logoContainer.setAlpha(0f);
        logoContainer.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(900)
                .setInterpolator(new OvershootInterpolator())
                .setStartDelay(200)
                .start();

        // ===== 2. Logo 图标：持续缓慢旋转 =====
        View icon = findViewById(R.id.splash_icon);
        icon.animate()
                .rotationBy(360f)
                .setDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(400)
                .start();

        // ===== 3. 标题 "BICYCLE"：从下方滑入 + 淡入 =====
        View title = findViewById(R.id.splash_title);
        title.setAlpha(0f);
        title.setTranslationY(30f);
        title.animate()
                .alpha(1f).translationY(0f)
                .setDuration(700)
                .setStartDelay(500)
                .start();

        // ===== 4. 副标题：从上方滑入 + 淡入 =====
        View subtitle = findViewById(R.id.splash_subtitle);
        subtitle.setAlpha(0f);
        subtitle.setTranslationY(-15f);
        subtitle.animate()
                .alpha(1f).translationY(0f)
                .setDuration(700)
                .setStartDelay(700)
                .start();

        // ===== 5. 版本号：淡入 =====
        View version = findViewById(R.id.splash_version);
        version.setAlpha(0f);
        version.animate()
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(900)
                .start();

        // ===== 6. 装饰元素：依次淡入 =====
        fadeIn(findViewById(R.id.splash_deco_circle_1), 600, 300);
        fadeIn(findViewById(R.id.splash_deco_circle_2), 600, 500);
        fadeIn(findViewById(R.id.splash_deco_line), 600, 700);

        // ===== 7. 底部进度条：淡入 + 进度推进 =====
        ProgressBar progressBar = findViewById(R.id.splash_progress);
        progressBar.setAlpha(0f);
        progressBar.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(800)
                .start();

        // 进度条动画：从 0 到 100，在剩余时间内完成
        ObjectAnimator progressAnim = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        progressAnim.setDuration(SPLASH_DURATION - 800);
        progressAnim.setStartDelay(1000);
        progressAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnim.start();

        // ===== 8. 2 秒后跳转 =====
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToMain, SPLASH_DURATION);
    }

    private void fadeIn(View view, long duration, long delay) {
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(delay)
                .start();
    }

    private void navigateToMain() {
        // 整体淡出后跳转
        View rootView = getWindow().getDecorView().getRootView();
        rootView.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        // 使用淡入淡出过渡
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                })
                .start();
    }
}
