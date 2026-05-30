package com.example.bicycle.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.bicycle.R;
import com.example.bicycle.XLog;

/**
 * 智能弹窗管理器
 * 功能：
 * 1. 每 5 秒自动弹一次弹窗
 * 2. 10 种不同的丝滑动画效果循环展示
 * 3. 每次弹窗使用不同的背景渐变色
 */
public class PopupManager {

    private static final int POPUP_INTERVAL = 5000; // 5秒间隔
    private static final int ANIMATION_DURATION = 400; // 动画时长

    private Context context;
    private PopupWindow currentPopup;
    private View currentPopupView;
    private int currentAnimationIndex = 0;
    private boolean isShowing = false;

    // 10种动画效果
    private static final String[] ANIMATION_NAMES = {
            "淡入上滑",
            "底部滑入",
            "左侧滑入",
            "右侧滑入",
            "弹性缩放",
            "顶部滑入",
            "旋转弹入",
            "缩放淡入",
            "卡片上滑",
            "斜向弹入"
    };

    // 10种背景渐变色
    private static final int[][] GRADIENT_COLORS = {
            {Color.parseColor("#FF6B6B"), Color.parseColor("#FF8E53")},  // 橙红渐变
            {Color.parseColor("#667EEA"), Color.parseColor("#764BA2")},  // 紫蓝渐变
            {Color.parseColor("#00C9FF"), Color.parseColor("#92FE9D")},  // 蓝绿渐变
            {Color.parseColor("#FC5C7D"), Color.parseColor("#6A82FB")},  // 粉紫渐变
            {Color.parseColor("#F093FB"), Color.parseColor("#F5576C")},  // 粉红渐变
            {Color.parseColor("#4FACFE"), Color.parseColor("#00F2FE")},  // 天蓝渐变
            {Color.parseColor("#43E97B"), Color.parseColor("#38F9D7")},  // 青绿渐变
            {Color.parseColor("#FA709A"), Color.parseColor("#FEE140")},  // 黄粉渐变
            {Color.parseColor("#A8ED5A"), Color.parseColor("#FEB25B")},  // 黄绿渐变
            {Color.parseColor("#3023AE"), Color.parseColor("#536976")}   // 深蓝渐变
    };

    // 弹窗内容
    private static final String[] ICONS = {"", "", "", "", "", "", "", "", "", ""};
    private static final String[] TITLES = {
            " 恭喜中奖",
            " 每日任务",
            " 小技巧",
            "🔔 提醒",
            "🎁 惊喜礼物",
            "⚡ 快速提示",
            "🌟 五星好评",
            "🎨 精选推荐",
            " 数据统计",
            "🚴 开始骑行"
    };
    private static final String[] CONTENTS = {
            "您获得了一张优惠券",
            "完成 3 次骑行获得奖励",
            "骑行前记得检查胎压和刹车",
            "记得完成今天的骑行目标哦！",
            "点击查看详情",
            "开启通知获取最新消息",
            "您的支持是我们最大的动力",
            "发现更多精彩内容",
            "本周骑行 128 公里",
            "记录您的每一段旅程"
    };
    private static final String[] BUTTON_TEXTS = {
            "立即领取",
            "去完成任务",
            "知道了",
            "立即出发",
            "点击查看",
            "开启",
            "去评价",
            "查看更多",
            "查看详情",
            "立即开始"
    };

    private android.os.Handler handler;
    private Runnable popupRunnable;

    public PopupManager(Context context) {
        this.context = context;
        this.handler = new android.os.Handler(android.os.Looper.getMainLooper());
    }

    /**
     * 启动定时弹窗
     */
    public void startAutoPopup() {
        popupRunnable = new Runnable() {
            @Override
            public void run() {
                showNextPopup();
                handler.postDelayed(this, POPUP_INTERVAL);
            }
        };
        // 立即显示第一个
        handler.postDelayed(popupRunnable, 2000);
    }

    /**
     * 停止定时弹窗
     */
    public void stopAutoPopup() {
        if (popupRunnable != null) {
            handler.removeCallbacks(popupRunnable);
        }
        dismissCurrentPopup();
    }

    /**
     * 显示下一个弹窗（循环 10 种动画）
     */
    private void showNextPopup() {
        if (isShowing) {
            dismissCurrentPopup(() -> {
                showPopupWithAnimation(currentAnimationIndex);
                currentAnimationIndex = (currentAnimationIndex + 1) % 10;
            });
        } else {
            showPopupWithAnimation(currentAnimationIndex);
            currentAnimationIndex = (currentAnimationIndex + 1) % 10;
        }
    }

    /**
     * 显示指定动画效果的弹窗
     */
    private void showPopupWithAnimation(int animationIndex) {
        if (currentPopup != null && currentPopup.isShowing()) {
            currentPopup.dismiss();
        }

        // 创建弹窗视图
        LayoutInflater inflater = LayoutInflater.from(context);
        View popupView = inflater.inflate(R.layout.popup_common, null);

        // 设置弹窗内容
        setupPopupContent(popupView, animationIndex);

        // 创建 PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setElevation(16);

        // 居中显示
        currentPopup = popupWindow;
        currentPopupView = popupView;
        isShowing = true;

        popupWindow.showAtLocation(
                ((android.app.Activity) context).getWindow().getDecorView(),
                Gravity.CENTER,
                0,
                0
        );

        // 播放进入动画
        playEnterAnimation(popupView, animationIndex);

        XLog.d("弹窗动画 " + (animationIndex + 1) + " - " + ANIMATION_NAMES[animationIndex] + " 显示");
    }

    /**
     * 设置弹窗内容（不同动画的个性化内容）
     */
    private void setupPopupContent(View popupView, int animationIndex) {
        // 设置背景渐变
        View root = popupView.findViewById(R.id.popup_root);
        if (root != null) {
            GradientDrawable bg = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    GRADIENT_COLORS[animationIndex]
            );
            bg.setCornerRadius(24);
            root.setBackground(bg);
        }

        // 设置内容
        TextView icon = popupView.findViewById(R.id.popup_icon);
        TextView title = popupView.findViewById(R.id.popup_title);
        TextView content = popupView.findViewById(R.id.popup_content);
        Button button = popupView.findViewById(R.id.popup_btn);

        if (icon != null) icon.setText(ICONS[animationIndex]);
        if (title != null) title.setText(TITLES[animationIndex]);
        if (content != null) content.setText(CONTENTS[animationIndex]);
        if (button != null) button.setText(BUTTON_TEXTS[animationIndex]);
    }

    /**
     * 播放弹窗进入动画（10种不同效果）
     */
    private void playEnterAnimation(View popupView, int animationIndex) {
        popupView.setAlpha(0f);

        switch (animationIndex) {
            case 0: // 淡入上滑
                popupView.setTranslationY(80f);
                popupView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                break;

            case 1: // 底部滑入
                popupView.setTranslationY(200f);
                popupView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .start();
                break;

            case 2: // 左侧滑入
                popupView.setTranslationX(-200f);
                popupView.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .start();
                break;

            case 3: // 右侧滑入
                popupView.setTranslationX(200f);
                popupView.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .start();
                break;

            case 4: // 弹性缩放
                popupView.setScaleX(0.5f);
                popupView.setScaleY(0.5f);
                popupView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new OvershootInterpolator(2.0f))
                        .start();
                break;

            case 5: // 顶部滑入
                popupView.setTranslationY(-200f);
                popupView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new DecelerateInterpolator(1.5f))
                        .start();
                break;

            case 6: // 旋转弹入
                popupView.setRotation(-20f);
                popupView.setScaleX(0.6f);
                popupView.setScaleY(0.6f);
                popupView.animate()
                        .alpha(1f)
                        .rotation(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION + 100)
                        .setInterpolator(new OvershootInterpolator(1.5f))
                        .start();
                break;

            case 7: // 缩放淡入
                popupView.setScaleX(0.7f);
                popupView.setScaleY(0.7f);
                popupView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                break;

            case 8: // 卡片上滑
                popupView.setTranslationY(60f);
                popupView.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
                break;

            case 9: // 斜向弹入
                popupView.setTranslationX(-100f);
                popupView.setTranslationY(100f);
                popupView.setScaleX(0.8f);
                popupView.setScaleY(0.8f);
                popupView.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION + 50)
                        .setInterpolator(new OvershootInterpolator(1.8f))
                        .start();
                break;
        }
    }

    /**
     * 关闭当前弹窗（带动画）
     */
    public void dismissCurrentPopup() {
        dismissCurrentPopup(null);
    }

    /**
     * 关闭当前弹窗（带反向动画）
     */
    public void dismissCurrentPopup(Runnable onComplete) {
        if (currentPopup == null || currentPopupView == null || !isShowing) {
            if (onComplete != null) onComplete.run();
            return;
        }

        isShowing = false;
        View popupView = currentPopupView;

        // 播放退出动画（反向）
        popupView.animate()
                .alpha(0f)
                .translationY(50f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(300)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    if (currentPopup != null) {
                        currentPopup.dismiss();
                    }
                    currentPopup = null;
                    currentPopupView = null;
                    if (onComplete != null) onComplete.run();
                    XLog.d("弹窗关闭");
                })
                .start();
    }

    /**
     * 获取当前动画索引
     */
    public int getCurrentAnimationIndex() {
        return currentAnimationIndex;
    }

    /**
     * 是否正在显示
     */
    public boolean isShowing() {
        return isShowing;
    }
}