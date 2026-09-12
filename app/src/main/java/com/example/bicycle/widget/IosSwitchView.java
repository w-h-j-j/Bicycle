package com.example.bicycle.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

/**
 * 仿IOS按钮
 * */
public class IosSwitchView extends View {

    private static final int DEFAULT_WIDTH_DP = 96;
    private static final int DEFAULT_HEIGHT_DP = 52;
    private static final int ANIM_DURATION = 350;

    // 开关两种状态颜色
    private int COLOR_ON = 0xFF41D166;
    private int COLOR_OFF = 0xFFF1F1F1;
    private int thumbColor = Color.WHITE;

    private Paint mTrackPaint;
    private Paint mThumbPaint;
    private Paint mThumbShadowPaint;
    private RectF mTrackRect;

    private int mViewWidth;
    private int mViewHeight;
    private float mThumbRadius;
    private float mMaxThumbOffset;

    private boolean mIsChecked;
    private boolean mIsDragging;
    private float mThumbCurrentOffset;
    private int mTouchSlop;
    private float mDownX;

    private ValueAnimator mValueAnimator;
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    private OnCheckedChangeListener mListener;

    private boolean mIsAnimRunning;
    /** 动画进度 0~1 */
    private float mAnimProgress;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(IosSwitchView view, boolean isChecked);
    }

    public IosSwitchView(Context context) {
        super(context);
        init(context, null);
    }

    public IosSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IosSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mThumbShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrackRect = new RectF();
        mThumbShadowPaint.setColor(0x33000000);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mIsAnimRunning = false;
        mAnimProgress = 0f;
    }

    public void setThumbColor(int thumbColor) {
        this.thumbColor = thumbColor;
        invalidate();
    }

    public void setColorResource(int onColor, int offColor, int thumbColor){
        this.COLOR_ON = onColor;
        this.COLOR_OFF = offColor;
        this.thumbColor = thumbColor;
        invalidate();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mListener = listener;
    }

    public boolean isChecked() {
        return mIsChecked;
    }

    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    /**
     * @param animate true：带动画，轨道颜色在COLOR_ON <-> COLOR_OFF平滑过渡
     *                false：直接切换，无动画，颜色直接跳到目标，不做过渡
     */
    public void setChecked(boolean checked, boolean animate) {
        if (mIsChecked == checked) {
            return;
        }
        mIsChecked = checked;

        if (animate) {
            animateToState();
        } else {
            mThumbCurrentOffset = getTargetOffset();
            mIsAnimRunning = false;
            invalidate();
        }
        if (mListener != null) {
            mListener.onCheckedChanged(this, mIsChecked);
        }
    }

    private float getTargetOffset() {
        return mIsChecked ? mMaxThumbOffset : 0f;
    }

    private void animateToState() {
        if (!isShown()) {
            mThumbCurrentOffset = getTargetOffset();
            mIsAnimRunning = false;
            invalidate();
            return;
        }
        if (mValueAnimator != null && mValueAnimator.isRunning()) {
            mValueAnimator.cancel();
        }

        final float startThumb = mThumbCurrentOffset;
        final float endThumb = getTargetOffset();

        mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        mValueAnimator.setDuration(ANIM_DURATION);
        mValueAnimator.setInterpolator(new DecelerateInterpolator());
        mIsAnimRunning = true;

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mAnimProgress = (float) animation.getAnimatedValue();
                // 滑块位置插值
                mThumbCurrentOffset = startThumb + (endThumb - startThumb) * mAnimProgress;
                invalidate();
            }
        });

        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mThumbCurrentOffset = endThumb;
                mIsAnimRunning = false;
                mAnimProgress = mIsChecked ? 1f : 0f;
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsAnimRunning = false;
                invalidate();
            }
        });
        mValueAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float density = getResources().getDisplayMetrics().density;
        int defW = (int) (DEFAULT_WIDTH_DP * density + 0.5f);
        int defH = (int) (DEFAULT_HEIGHT_DP * density + 0.5f);

        int newW = resolveSize(defW, widthMeasureSpec);
        int newH = resolveSize(defH, heightMeasureSpec);
        mViewWidth = newW;
        mViewHeight = newH;

        mThumbRadius = mViewHeight / 2f - 4f;
        mMaxThumbOffset = mViewWidth - getPaddingLeft() - getPaddingRight() - mThumbRadius * 2;
        if (mMaxThumbOffset < 0) mMaxThumbOffset = 0;

        if (!mIsDragging && (mValueAnimator == null || !mValueAnimator.isRunning())) {
            mThumbCurrentOffset = getTargetOffset();
        }
        setMeasuredDimension(mViewWidth, mViewHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int padLeft = getPaddingLeft();
        int padTop = getPaddingTop();
        int padRight = getPaddingRight();
        int padBottom = getPaddingBottom();

        mTrackRect.set(padLeft, padTop, mViewWidth - padRight, mViewHeight - padBottom);
        float trackHeight = mTrackRect.height();
        float radius = trackHeight / 2f;

        int drawColor;
        if (mIsAnimRunning) {
            // 动画中：颜色插值
            if(mIsChecked){
                // 关 → 开：F1F1F1 → 41D166
                drawColor = (int) mArgbEvaluator.evaluate(mAnimProgress, COLOR_OFF, COLOR_ON);
            }else{
                // 开 → 关：41D166 → F1F1F1
                drawColor = (int) mArgbEvaluator.evaluate(mAnimProgress, COLOR_ON, COLOR_OFF);
            }
        } else {
            // 非动画，直接目标色
            drawColor = mIsChecked ? COLOR_ON : COLOR_OFF;
        }
        mTrackPaint.setColor(drawColor);
        canvas.drawRoundRect(mTrackRect, radius, radius, mTrackPaint);

        float cx = padLeft + mThumbRadius + mThumbCurrentOffset;
        float cy = padTop + trackHeight / 2f;

        canvas.drawCircle(cx, cy + 1.2f, mThumbRadius, mThumbShadowPaint);
        mThumbPaint.setColor(thumbColor);
        canvas.drawCircle(cx, cy, mThumbRadius, mThumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        float x = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mValueAnimator != null && mValueAnimator.isRunning()) {
                    mValueAnimator.cancel();
                }
                mDownX = x;
                mIsDragging = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                float deltaX = x - mDownX;
                if (!mIsDragging && Math.abs(deltaX) > mTouchSlop) {
                    mIsDragging = true;
                    mDownX = x;
                }
                if (mIsDragging) {
                    mThumbCurrentOffset += deltaX;
                    if (mThumbCurrentOffset < 0) mThumbCurrentOffset = 0;
                    if (mThumbCurrentOffset > mMaxThumbOffset) mThumbCurrentOffset = mMaxThumbOffset;
                    mDownX = x;
                    invalidate();
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (!mIsDragging) {
                    mIsChecked = !mIsChecked;
                    if (mListener != null) mListener.onCheckedChanged(this, mIsChecked);
                } else {
                    boolean newState = mThumbCurrentOffset > mMaxThumbOffset / 2f;
                    if (newState != mIsChecked) {
                        mIsChecked = newState;
                        if (mListener != null) mListener.onCheckedChanged(this, mIsChecked);
                    }
                }
                animateToState();
                mIsDragging = false;
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null) {
            if (mValueAnimator.isRunning()) mValueAnimator.cancel();
            mValueAnimator = null;
        }
    }
}
