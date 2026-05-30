package com.example.bicycle.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * 滚动控件
 * */
public class HorizontalAutoScrollTextView extends AppCompatTextView {

    private boolean isAutoScrolling = false;
    private int scrollSpeed = 1; // 滚动速度（像素/帧）
    private long scrollDelay = 20L; // 滚动间隔（毫秒）
    private long repeatDelay = 200L; // 从头开始前的延迟（毫秒）
    private Handler handler = new Handler(Looper.getMainLooper());

    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAutoScrolling) return;

            // 向右滚动
            scrollBy(scrollSpeed, 0);

            // 获取文本的实际宽度
            float textWidth = 0;
            if (getLayout() != null) {
                textWidth = getLayout().getLineWidth(0);
            } else {
                textWidth = getPaint().measureText(getText().toString());
            }

            // 如果滚动超过文本宽度，回到起点
            if (getScrollX() >= textWidth) {
                // 短暂暂停后回到起点
                isAutoScrolling = false;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollTo(0, 0);
                        isAutoScrolling = true;
                        handler.post(scrollRunnable);
                    }
                }, repeatDelay);
            } else {
                handler.postDelayed(this, scrollDelay);
            }
        }
    };

    public HorizontalAutoScrollTextView(Context context) {
        super(context);
        init();
    }

    public HorizontalAutoScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalAutoScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 禁用焦点
        setFocusable(false);
        setFocusableInTouchMode(false);

        // 确保单行显示
        setMaxLines(1);
        setEllipsize(null); // 不显示省略号
        setSingleLine(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 测量文本宽度
        float textWidth = 0;
        if (getLayout() != null) {
            textWidth = getLayout().getLineWidth(0);
        } else {
            textWidth = getPaint().measureText(getText().toString());
        }

        int viewWidth = getMeasuredWidth();

        // 如果文本宽度超过视图宽度，启用滚动
        if (textWidth > viewWidth && !isAutoScrolling) {
            startAutoScroll();
        } else if (textWidth <= viewWidth && isAutoScrolling) {
            stopAutoScroll();
        }
    }

    public void startAutoScroll() {
        if (isAutoScrolling) return;

        isAutoScrolling = true;
        scrollTo(0, 0); // 重置到起始位置
        handler.post(scrollRunnable);
    }

    public void stopAutoScroll() {
        isAutoScrolling = false;
        handler.removeCallbacks(scrollRunnable);
        scrollTo(0, 0); // 回到起始位置
    }

    public void setScrollSpeed(int speed) {
        this.scrollSpeed = speed;
    }

    public void setScrollDelay(long delay) {
        this.scrollDelay = delay;
    }

    public void setRepeatDelay(long delay) {
        this.repeatDelay = delay;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        // 文本变化时检查是否需要滚动
        post(new Runnable() {
            @Override
            public void run() {
                float textWidth = getPaint().measureText(getText().toString());
                int viewWidth = getWidth();

                if (textWidth > viewWidth && !isAutoScrolling) {
                    startAutoScroll();
                } else if (textWidth <= viewWidth && isAutoScrolling) {
                    stopAutoScroll();
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoScroll();
    }
}
