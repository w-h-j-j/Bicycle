package com.example.bicycle.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bicycle.R;

/**
 * 自定义桌面应用列表控件
 * 支持设置行列数量、左右翻页效果
 */
public class DesktopRecyclerView extends RecyclerView {

    // 行列配置
    private int columnCount = 4;  // 默认 4 列
    private int rowCount = 3;     // 默认 3 行
    private int itemCountPerPage; // 每页显示数量

    // 翻页相关
    private OverScroller scroller;
    private VelocityTracker velocityTracker;
    private int currentPage = 0;
    private int totalPages = 1;
    private float downX;
    private float lastX;
    private boolean isDragging = false;

    // 翻页速度阈值（像素/秒）
    private static final int MIN_FLING_VELOCITY = 500;

    // 页面宽度
    private int pageWidth = 0;

    // 翻页监听
    private OnPageChangeListener onPageChangeListener;

    public DesktopRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public DesktopRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DesktopRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // 读取自定义属性
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DesktopRecyclerView);
            columnCount = typedArray.getInt(R.styleable.DesktopRecyclerView_columnCount, 4);
            rowCount = typedArray.getInt(R.styleable.DesktopRecyclerView_rowCount, 3);
            typedArray.recycle();
        }

        itemCountPerPage = columnCount * rowCount;

        // 初始化 OverScroller
        scroller = new OverScroller(context, new DecelerateInterpolator(1.5f));

        // 设置布局管理器
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columnCount);
        setLayoutManager(layoutManager);

        // 禁用默认滚动
        setLayoutManager(new GridLayoutManager(getContext(), columnCount) {
            @Override
            public boolean canScrollVertically() {
                return false; // 禁止垂直滚动
            }
        });

        // 启用水平滚动
        setHorizontalScrollBarEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        pageWidth = w - getPaddingLeft() - getPaddingRight();
        calculateTotalPages();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                lastX = ev.getX();
                isDragging = false;
                
                // 停止滚动
                if (!scroller.isFinished()) {
                    scroller.abortAnimation();
                }
                
                // 初始化速度追踪器
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                float deltaX = lastX - ev.getX();
                lastX = ev.getX();
                
                // 水平滚动
                scrollBy((int) deltaX, 0);
                isDragging = true;
                
                velocityTracker.addMovement(ev);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000);
                    float velocityX = velocityTracker.getXVelocity();

                    // 计算滑动距离
                    float distanceX = downX - lastX;

                    // 根据滑动速度或距离判断是否翻页
                    if (Math.abs(velocityX) > MIN_FLING_VELOCITY) {
                        // 快速滑动
                        if (velocityX < 0) {
                            nextPage();
                        } else {
                            previousPage();
                        }
                    } else if (Math.abs(distanceX) > pageWidth * 0.3) {
                        // 滑动距离超过 30% 页面宽度
                        if (distanceX > 0) {
                            nextPage();
                        } else {
                            previousPage();
                        }
                    } else {
                        // 回弹到当前页
                        scrollToPage(currentPage);
                    }
                    
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                isDragging = false;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 下一页
     */
    public void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            scrollToPage(currentPage);
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageChanged(currentPage, totalPages);
            }
        } else {
            // 已经是最后一页，回弹
            scrollToPage(currentPage);
        }
    }

    /**
     * 上一页
     */
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            scrollToPage(currentPage);
            if (onPageChangeListener != null) {
                onPageChangeListener.onPageChanged(currentPage, totalPages);
            }
        } else {
            // 已经是第一页，回弹
            scrollToPage(currentPage);
        }
    }

    /**
     * 滚动到指定页
     */
    private void scrollToPage(int page) {
        if (pageWidth <= 0) return;
        
        int targetX = page * pageWidth;
        scroller.startScroll(getScrollX(), 0, targetX - getScrollX(), 0, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 设置行列数量
     */
    public void setGridSize(int columns, int rows) {
        this.columnCount = columns;
        this.rowCount = rows;
        this.itemCountPerPage = columns * rows;
        
        if (getLayoutManager() instanceof GridLayoutManager) {
            ((GridLayoutManager) getLayoutManager()).setSpanCount(columns);
        }
        
        calculateTotalPages();
    }

    /**
     * 计算总页数
     */
    private void calculateTotalPages() {
        if (getAdapter() != null && itemCountPerPage > 0) {
            int totalItems = getAdapter().getItemCount();
            totalPages = (int) Math.ceil((double) totalItems / itemCountPerPage);
            if (totalPages < 1) totalPages = 1;
        } else {
            totalPages = 1;
        }
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);
        calculateTotalPages();
        currentPage = 0;
    }

    /**
     * 获取当前页
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * 获取总页数
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * 获取每页显示数量
     */
    public int getItemCountPerPage() {
        return itemCountPerPage;
    }

    /**
     * 设置翻页监听
     */
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.onPageChangeListener = listener;
    }

    /**
     * 翻页监听接口
     */
    public interface OnPageChangeListener {
        void onPageChanged(int currentPage, int totalPages);
    }
}