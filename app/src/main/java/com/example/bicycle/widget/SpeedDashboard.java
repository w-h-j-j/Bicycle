package com.example.bicycle.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.example.bicycle.R;

/**
 * 自定义速度仪表盘
 * 使用 icon_background.png 作为背景弧线，icon_progress.png 作为进度弧线
 * 速度范围 0~220 km/h，点击 Logcat 中日志可跳转到源码行
 *
 * 原理：
 * 1. 背景：绘制完整的 icon_background.png（270°弧线轨道）
 * 2. 进度：通过 Canvas clipPath 裁剪，只显示从起点到当前速度对应角度的 icon_progress.png 部分
 * 3. 中心文字：显示当前速度数值和单位
 * 4. 刻度：在弧线外侧绘制速度刻度标签
 */
public class SpeedDashboard extends View {

    // ======================== 默认参数 ========================
    private static final int DEFAULT_MAX_SPEED = 220;
    private static final int DEFAULT_START_ANGLE = 135;   // 弧线起始角度（左下方）
    private static final int DEFAULT_SWEEP_ANGLE = 270;   // 弧线扫过角度

    // ======================== 绘制参数 ========================
    private int maxSpeed = DEFAULT_MAX_SPEED;
    private int currentSpeed = 0;
    private int startAngle = DEFAULT_START_ANGLE;
    private int sweepAngle = DEFAULT_SWEEP_ANGLE;

    // 图片资源
    private Bitmap bgBitmap;     // 背景 arc 图
    private Bitmap progressBitmap; // 进度 arc 图

    // Paint
    private Paint speedTextPaint;
    private Paint unitTextPaint;
    private Paint tickTextPaint;
    private Paint tickLinePaint;
    private Paint innerCirclePaint;

    // View 尺寸
    private int viewWidth;
    private int viewHeight;
    private float centerX;
    private float centerY;
    private float arcRadius;

    public SpeedDashboard(Context context) {
        this(context, null);
    }

    public SpeedDashboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedDashboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initPaint();
        initBitmap();
    }

    // ======================== 初始化 ========================

    private void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SpeedDashboard);
            maxSpeed = ta.getInt(R.styleable.SpeedDashboard_maxSpeed, DEFAULT_MAX_SPEED);
            currentSpeed = ta.getInt(R.styleable.SpeedDashboard_currentSpeed, 0);
            startAngle = ta.getInt(R.styleable.SpeedDashboard_startAngle, DEFAULT_START_ANGLE);
            sweepAngle = ta.getInt(R.styleable.SpeedDashboard_sweepAngle, DEFAULT_SWEEP_ANGLE);
            ta.recycle();
        }
    }

    private void initPaint() {
        // 速度数字
        speedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        speedTextPaint.setColor(0xFFFFFFFF);
        speedTextPaint.setTextSize(72f);
        speedTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        speedTextPaint.setTextAlign(Paint.Align.CENTER);

        // 单位文字 km/h
        unitTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        unitTextPaint.setColor(0xB3FFFFFF);  // 70%白色
        unitTextPaint.setTextSize(16f);
        unitTextPaint.setTextAlign(Paint.Align.CENTER);

        // 刻度文字
        tickTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickTextPaint.setColor(0x80FFFFFF);  // 50%白色
        tickTextPaint.setTextSize(12f);
        tickTextPaint.setTextAlign(Paint.Align.CENTER);

        // 刻度线
        tickLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickLinePaint.setColor(0x60FFFFFF);  // 37%白色
        tickLinePaint.setStrokeWidth(1.5f);

        // 内圈装饰
        innerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerCirclePaint.setColor(0x15FFFFFF);  // 6%白色
        innerCirclePaint.setStyle(Paint.Style.STROKE);
        innerCirclePaint.setStrokeWidth(2f);
    }

    private void initBitmap() {
        bgBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_background);
        progressBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_progress);
    }

    // ======================== 测量 ========================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // 保持正方形
        int size = Math.min(widthSize, heightSize);
        // 去掉padding
        int width = size - getPaddingLeft() - getPaddingRight();
        int height = size - getPaddingTop() - getPaddingBottom();
        int finalSize = Math.min(width, height) + getPaddingLeft() + getPaddingRight();

        setMeasuredDimension(finalSize, finalSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w - getPaddingLeft() - getPaddingRight();
        viewHeight = h - getPaddingTop() - getPaddingBottom();
        centerX = getPaddingLeft() + viewWidth / 2f;
        centerY = getPaddingTop() + viewHeight / 2f;
        arcRadius = Math.min(viewWidth, viewHeight) / 2f;

        // 根据View尺寸缩放速度文字
        speedTextPaint.setTextSize(arcRadius * 0.28f);
        unitTextPaint.setTextSize(arcRadius * 0.08f);
        tickTextPaint.setTextSize(arcRadius * 0.06f);
    }

    // ======================== 绘制 ========================

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackgroundArc(canvas);
        drawProgressArc(canvas);
        drawInnerCircle(canvas);
        drawTickMarks(canvas);
        drawSpeedText(canvas);
    }

    /**
     * 绘制背景弧线（完整的270°轨道）
     */
    private void drawBackgroundArc(Canvas canvas) {
        if (bgBitmap == null) return;
        drawScaledBitmap(canvas, bgBitmap);
    }

    /**
     * 绘制进度弧线（裁剪只显示当前速度对应的弧段）
     */
    private void drawProgressArc(Canvas canvas) {
        if (progressBitmap == null || currentSpeed <= 0) return;

        float sweep = (currentSpeed / (float) maxSpeed) * sweepAngle;
        if (sweep <= 0) return;

        canvas.save();

        // 创建裁剪路径：从中心出发的扇形区域
        Path clipPath = new Path();
        clipPath.moveTo(centerX, centerY);

        // 扇形外弧
        RectF arcRect = new RectF(
                centerX - arcRadius,
                centerY - arcRadius,
                centerX + arcRadius,
                centerY + arcRadius
        );
        clipPath.arcTo(arcRect, startAngle, sweep);
        clipPath.lineTo(centerX, centerY);
        clipPath.close();

        canvas.clipPath(clipPath);
        drawScaledBitmap(canvas, progressBitmap);

        canvas.restore();
    }

    /**
     * 将Bitmap缩放绘制到View中心区域
     */
    private void drawScaledBitmap(Canvas canvas, Bitmap bitmap) {
        float bmWidth = bitmap.getWidth();
        float bmHeight = bitmap.getHeight();
        float scale = (arcRadius * 2f) / Math.max(bmWidth, bmHeight);

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(
                centerX - bmWidth * scale / 2f,
                centerY - bmHeight * scale / 2f
        );

        canvas.drawBitmap(bitmap, matrix, null);
    }

    /**
     * 绘制内圈装饰环
     */
    private void drawInnerCircle(Canvas canvas) {
        float innerRadius = arcRadius * 0.38f;
        canvas.drawCircle(centerX, centerY, innerRadius, innerCirclePaint);
    }

    /**
     * 绘制刻度线和刻度标签
     */
    private void drawTickMarks(Canvas canvas) {
        int tickInterval = 20; // 每20km/h一个主刻度
        int totalTicks = maxSpeed / tickInterval;
        float tickOuterRadius = arcRadius * 0.82f;  // 刻度线外端
        float tickInnerRadius = arcRadius * 0.76f;   // 刻度线内端
        float labelRadius = arcRadius * 0.68f;       // 刻度文字位置

        for (int i = 0; i <= totalTicks; i++) {
            int speed = i * tickInterval;
            float angle = startAngle + (speed / (float) maxSpeed) * sweepAngle;
            double rad = Math.toRadians(angle);

            // 刻度线
            float outerX = centerX + (float) (tickOuterRadius * Math.cos(rad));
            float outerY = centerY + (float) (tickOuterRadius * Math.sin(rad));
            float innerX = centerX + (float) (tickInnerRadius * Math.cos(rad));
            float innerY = centerY + (float) (tickInnerRadius * Math.sin(rad));
            canvas.drawLine(outerX, outerY, innerX, innerY, tickLinePaint);

            // 刻度文字（旋转绘制）
            float labelX = centerX + (float) (labelRadius * Math.cos(rad));
            float labelY = centerY + (float) (labelRadius * Math.sin(rad));

            canvas.save();
            canvas.rotate(angle + 90, labelX, labelY);
            canvas.drawText(String.valueOf(speed), labelX, labelY + tickTextPaint.getTextSize() / 3f, tickTextPaint);
            canvas.restore();
        }
    }

    /**
     * 绘制中心速度数字和单位
     */
    private void drawSpeedText(Canvas canvas) {
        // 速度数字
        String speedStr = String.valueOf(currentSpeed);
        float textY = centerY + speedTextPaint.getTextSize() / 3f;
        canvas.drawText(speedStr, centerX, textY, speedTextPaint);

        // km/h 单位
        float unitY = textY + speedTextPaint.getTextSize() * 0.6f;
        canvas.drawText("km/h", centerX, unitY, unitTextPaint);
    }

    // ======================== 公开方法 ========================

    /**
     * 设置当前速度
     */
    public void setSpeed(int speed) {
        if (speed < 0) speed = 0;
        if (speed > maxSpeed) speed = maxSpeed;
        this.currentSpeed = speed;
        invalidate();
    }

    /**
     * 获取当前速度
     */
    public int getSpeed() {
        return currentSpeed;
    }

    /**
     * 设置最大速度
     */
    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
        invalidate();
    }

    /**
     * 动画过渡到目标速度
     */
    public void animateToSpeed(int targetSpeed, int durationMs) {
        if (targetSpeed < 0) targetSpeed = 0;
        if (targetSpeed > maxSpeed) targetSpeed = maxSpeed;

        int startSpeed = currentSpeed;
        int diff = targetSpeed - startSpeed;
        int steps = Math.max(Math.abs(diff), 1);
        int stepDuration = durationMs / steps;

        for (int i = 1; i <= steps; i++) {
            int speed = startSpeed + (diff * i / steps);
            postDelayed(() -> setSpeed(speed), i * stepDuration);
        }


    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (bgBitmap != null && !bgBitmap.isRecycled()) {
            bgBitmap.recycle();
            bgBitmap = null;
        }
        if (progressBitmap != null && !progressBitmap.isRecycled()) {
            progressBitmap.recycle();
            progressBitmap = null;
        }
    }
}