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
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 闪屏页 —— 停留 1 秒后自动跳转至 MainActivity
 */
public class SplashActivity extends AppCompatActivity {

    /** 闪屏总停留时长（毫秒） */
    private static final long SPLASH_DURATION = 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 全屏沉浸
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        startAnimations();
    }

    private void startAnimations() {
        // ===== 1. Logo 容器：柔和缩放 + 淡入 =====
        View logoContainer = findViewById(R.id.splash_logo_container);
        logoContainer.setScaleX(0.6f);
        logoContainer.setScaleY(0.6f);
        logoContainer.setAlpha(0f);
        logoContainer.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // ===== 2. 圆形进度环：从 0 推进到 100 =====
        ProgressBar ring = findViewById(R.id.splash_ring);
        ObjectAnimator progressAnim = ObjectAnimator.ofInt(ring, "progress", 0, 100);
        progressAnim.setDuration(SPLASH_DURATION);
        progressAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnim.start();

        // ===== 3. 标题 "BICYCLE"：淡入 + 轻微上移 =====
        View title = findViewById(R.id.splash_title);
        title.setAlpha(0f);
        title.setTranslationY(12f);
        title.animate()
                .alpha(1f).translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // ===== 4. 分隔线：从中心向两侧展开 =====
        View divider = findViewById(R.id.splash_divider);
        divider.setAlpha(0f);
        divider.setScaleX(0f);
        divider.animate()
                .alpha(1f).scaleX(1f)
                .setDuration(350)
                .setStartDelay(350)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // ===== 5. 副标题：淡入 =====
        View subtitle = findViewById(R.id.splash_subtitle);
        subtitle.setAlpha(0f);
        subtitle.animate()
                .alpha(1f)
                .setDuration(350)
                .setStartDelay(450)
                .start();

        // ===== 6. 底部版本号：淡入 =====
        View version = findViewById(R.id.splash_version);
        version.setAlpha(0f);
        version.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(500)
                .start();

        // ===== 7. 1 秒后淡出跳转 =====
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToMain, SPLASH_DURATION);
    }

    private void navigateToMain() {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.animate()
                .alpha(0f)
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                })
                .start();
    }
}
