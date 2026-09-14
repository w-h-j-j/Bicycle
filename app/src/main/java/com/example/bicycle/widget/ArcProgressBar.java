package com.example.bicycle.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.bicycle.R;

public class ArcProgressBar extends View {

    private int progress;
    private int maxProgress;
    private float mStartAngle;
    private float mTotalSweepAngle;
    private int resBackgroundId;
    private int resProgressId;
    private Bitmap bitmapBackground, bitmapProgress;
    private RectF mDrawRectF;       // 绘制区域（弧形的外接矩形）

    Paint paintBackground;

    public ArcProgressBar(Context context) {
        super(context);
    }

    public ArcProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ArcProgressBar);
        progress = typedArray.getInt(R.styleable.ArcProgressBar_progress, 0);
        maxProgress = typedArray.getInt(R.styleable.ArcProgressBar_max_progress, 100);
        mStartAngle = typedArray.getInt(R.styleable.ArcProgressBar_start_angle, 135);
        mTotalSweepAngle = typedArray.getInt(R.styleable.ArcProgressBar_sweep_angle, 270);
        resBackgroundId = typedArray.getResourceId( R.styleable.ArcProgressBar_image_background, R.mipmap.icon_background);
        resProgressId = typedArray.getResourceId( R.styleable.ArcProgressBar_image_progress, R.mipmap.icon_progress);
        bitmapBackground = BitmapFactory.decodeResource(getResources(), resBackgroundId);
        bitmapProgress = BitmapFactory.decodeResource(getResources(), resProgressId);
        typedArray.recycle(); // 释放资源

        mDrawRectF = new RectF();
        paintBackground= new Paint();
        paintBackground.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // 取宽高中的较小值，保证控件为正方形
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 绘制区域的边距 = 环形宽度的一半（避免弧形边缘超出控件）
        float padding = 12 / 2f;
        mDrawRectF.set(
                padding,          // 左
                padding,          // 上
                w - padding,      // 右
                h - padding       // 下
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. 绘制背景环形
        canvas.drawBitmap(bitmapBackground, 0, 0, paintBackground);
        canvas.save();

        float progressSweepAngle = (progress / 100f) * mTotalSweepAngle;
        // 2. 绘制进度
        cropSemicircle(canvas, bitmapProgress, mStartAngle, progressSweepAngle);
    }

    /**
     * 对正方形图片进行扇形裁剪
     * @param originalBitmap 原始图片
     * @param startAngle 起始角度（数学坐标系，逆时针计算）
     * @param sweepAngle 扫过角度（数学坐标系，逆时针计算）
     */
    public void cropSemicircle(Canvas canvas, Bitmap originalBitmap, float startAngle, float sweepAngle) {
        // 创建一个新的Bitmap用于存储裁剪结果
//        Bitmap resultBitmap = Bitmap.createBitmap(
//                originalBitmap.getWidth(),
//                originalBitmap.getHeight(),
//                Bitmap.Config.ARGB_8888
//        );
//        Canvas canvas = new Canvas(resultBitmap);

        // 设置画布背景为透明
        canvas.drawColor(Color.TRANSPARENT/*, PorterDuff.Mode.CLEAR*/);

        // 创建Path对象，表示扇形区域
        Path path = new Path();

        // 获取图片中心点
        float centerX = originalBitmap.getWidth() / 2.0f;
        float centerY = originalBitmap.getHeight() / 2.0f;
        float radius = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight()) / 2.0f;

        // 计算起始角度（转换为Android坐标系）
        // Android坐标系中角度顺时针计算，数学坐标系中角度逆时针计算
        // 所以需要转换：Android角度 = 360° - 数学角度
        //float androidStartAngle = 360 - startAngle;
        float androidStartAngle = startAngle;

        // 创建扇形路径
        path.moveTo(centerX, centerY);
        path.arcTo(new RectF(centerX - radius, centerY - radius,
                        centerX + radius, centerY + radius),
                androidStartAngle, sweepAngle);
        path.lineTo(centerX, centerY);
        path.close();

        // 设置裁剪路径
        canvas.clipPath(path);

        // 绘制原始图片
        canvas.drawBitmap(originalBitmap, 0, 0, null);

    }

    public void setProgress(int progress) {
        if (progress < 0) progress = 0;
        if (progress > 100) progress = 100;
        if (this.progress == progress) return;
        this.progress = progress;
        invalidate();
    }

    public int getProgress() {
        return progress;
    }
}
