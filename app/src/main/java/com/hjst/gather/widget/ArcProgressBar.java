package com.hjst.gather.widget;


import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjst.gather.R;

import java.util.Random;

/**
 * 图片环形进度条
 * <p>
 * 两张圆环图（底环 + 进度环）配合扇形裁剪呈现弧形进度：
 * 进度环按 View 实际尺寸等比缩放绘制（不再要求图片像素与控件尺寸一致），
 * 通过 clipPath 扇形裁剪露出对应比例的进度环。
 * <p>
 * 角度采用 Android 坐标系：顺时针，3点钟方向为0°，默认 135° 起始扫 270°。
 */
public class ArcProgressBar extends View {

    private static final long ANIM_DURATION = 500L;

    private int progress;
    private int maxProgress = 100;
    private float mStartAngle = 135f;
    private float mTotalSweepAngle = 270f;
    private float mRingPadding;   // 绘制区域内缩边距（px）

    private Bitmap bitmapBackground;
    private Bitmap bitmapProgress;

    private final RectF mDrawRectF = new RectF();   // 弧形外接矩形，随尺寸变化重算
    private final RectF mClipRectF = new RectF();   // 扇形裁剪复用矩形（避免每帧分配）
    private final Path mClipPath = new Path();      // 扇形裁剪复用路径（避免每帧分配）
    private final Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Nullable
    private ValueAnimator mProgressAnimator;

    public ArcProgressBar(Context context) {
        this(context, null);
    }

    public ArcProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ArcProgressBar);
        progress = typedArray.getInt(R.styleable.ArcProgressBar_progress, 0);
        maxProgress = typedArray.getInt(R.styleable.ArcProgressBar_max_progress, 100);
        mStartAngle = typedArray.getInt(R.styleable.ArcProgressBar_start_angle, 135);
        mTotalSweepAngle = typedArray.getInt(R.styleable.ArcProgressBar_sweep_angle, 270);
        mRingPadding = typedArray.getDimension(R.styleable.ArcProgressBar_ring_padding, dp2px(2));
        int resBackgroundId = typedArray.getResourceId(R.styleable.ArcProgressBar_image_background, R.mipmap.icon_background);
        int resProgressId = typedArray.getResourceId(R.styleable.ArcProgressBar_image_progress, R.mipmap.icon_progress);
        typedArray.recycle(); // 释放资源

        if (maxProgress <= 0) {
            maxProgress = 100;
        }
        progress = Math.max(0, Math.min(progress, maxProgress));

        bitmapBackground = decodeSafely(resBackgroundId);
        bitmapProgress = decodeSafely(resProgressId);

        // 缩放绘制时开启双线性过滤，避免弧形锯齿
        mBitmapPaint.setFilterBitmap(true);
    }

    private Bitmap decodeSafely(int resId) {
        if (resId == 0) return null;
        try {
            return BitmapFactory.decodeResource(getResources(), resId);
        } catch (Exception e) {
            return null;
        }
    }

    private float dp2px(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 取宽高中的较小值，保证控件为正方形
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 绘制区域内缩一个边距，避免弧形抗锯齿边缘被控件裁切
        mDrawRectF.set(mRingPadding, mRingPadding, w - mRingPadding, h - mRingPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. 绘制背景环形（整图缩放到绘制区域）
        if (bitmapBackground != null) {
            canvas.drawBitmap(bitmapBackground, null, mDrawRectF, mBitmapPaint);
        }
        if (bitmapProgress == null) return;

        // 2. 按 进度/最大值 换算扫过角度，扇形裁剪绘制进度环
        float progressSweepAngle = progress * 1.0f / maxProgress * mTotalSweepAngle;
        if (progressSweepAngle > 0f) {
            cropSemicircle(canvas, bitmapProgress, mStartAngle, progressSweepAngle);
        }
    }

    /**
     * 对进度环图片按扇形区域裁剪绘制
     * 内部自行 save/restore，不会把裁剪状态泄漏给调用方
     *
     * @param bitmap     进度环图（中心透明）
     * @param startAngle 起始角度（Android坐标系，顺时针，3点钟为0°）
     * @param sweepAngle 扫过角度（顺时针）
     */
    private void cropSemicircle(Canvas canvas, Bitmap bitmap, float startAngle, float sweepAngle) {
        int saveCount = canvas.save();
        try {
            float centerX = mDrawRectF.centerX();
            float centerY = mDrawRectF.centerY();
            float radius = Math.min(mDrawRectF.width(), mDrawRectF.height()) / 2.0f;

            mClipRectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            mClipPath.reset();
            mClipPath.moveTo(centerX, centerY);
            mClipPath.arcTo(mClipRectF, startAngle, sweepAngle);
            mClipPath.lineTo(centerX, centerY);
            mClipPath.close();

            canvas.clipPath(mClipPath);
            canvas.drawBitmap(bitmap, null, mDrawRectF, mBitmapPaint);
        } finally {
            canvas.restoreToCount(saveCount);
        }
    }

    /**
     * 设置进度（带默认动画），自动钳位到 [0, maxProgress]
     */
    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    /**
     * 设置进度
     *
     * @param animate true 时使用 ValueAnimator 平滑过渡
     */
    public void setProgress(int progress, boolean animate) {
        progress = Math.max(0, Math.min(progress, maxProgress));
        if (this.progress == progress) return;

        if (!animate) {
            cancelAnimator();
            this.progress = progress;
            invalidate();
            return;
        }

        cancelAnimator();
        mProgressAnimator = ValueAnimator.ofInt(this.progress, progress);
        mProgressAnimator.setDuration(ANIM_DURATION);
        mProgressAnimator.addUpdateListener(animation -> {
            this.progress = (int) animation.getAnimatedValue();
            invalidate();
        });
        mProgressAnimator.start();
    }

    /**
     * 设置进度最大值（须为正数），并对当前进度重新钳位
     */
    public void setMaxProgress(int maxProgress) {
        if (maxProgress <= 0) return;
        this.maxProgress = maxProgress;
        setProgress(Math.min(progress, maxProgress), false);
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    private void cancelAnimator() {
        if (mProgressAnimator != null) {
            mProgressAnimator.cancel();
            mProgressAnimator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnimator();
        super.onDetachedFromWindow();
    }

    public void startSimulation(){
        handler.sendEmptyMessageDelayed(1, 800);
    }

    public void stopSimulation(){
        handler.removeMessages(1);
        postDelayed(() -> setProgress(0), ANIM_DURATION);
    }

    Random random = new Random();
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                removeMessages(1);
                sendEmptyMessageDelayed(1, ANIM_DURATION);

                int maxDrop = 5;
                int last = random.nextInt(101);   // 0..280
                int lower = Math.max(0, last - maxDrop);
                last = lower + random.nextInt(101 - lower);
                setProgress(last);
            }
        }
    };
}
