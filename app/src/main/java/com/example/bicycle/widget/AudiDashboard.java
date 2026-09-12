package com.example.bicycle.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

/**
 * 奥迪风格液晶仪表盘
 * 左侧转速表，右侧速度表
 */
public class AudiDashboard extends View {

    private static final int DURATION_TIME = 500;

    // 当前值
    private float currentRPM = 0;      // 当前转速
    private float currentSpeed = 0;    // 当前速度
    private float targetRPM = 0;       // 目标转速
    private float targetSpeed = 0;     // 目标速度

    // 动画
    private ValueAnimator rpmAnimator;
    private ValueAnimator speedAnimator;

    // 画笔
    private Paint bgPaint;             // 背景画笔
    private Paint scalePaint;          // 刻度画笔
    private Paint numberPaint;         // 数字画笔
    private Paint pointerPaint;        // 指针画笔
    private Paint arcPaint;            // 圆弧画笔
    private Paint glowPaint;           // 发光效果画笔
    private Paint centerPaint;         // 中心圆画笔

    // 尺寸
    private int width;
    private int height;
    private float radius;              // 表盘半径
    private float centerX;             // 中心X
    private float centerY;             // 中心Y

    // 配置
    private static final float START_ANGLE = 135f;    // 起始角度
    private static final float SWEEP_ANGLE = 270f;    // 扫描角度
    private static final int RPM_MAX = 8000;          // 最大转速
    private static final int SPEED_MAX = 280;         // 最大速度

    // 颜色
    private static final int COLOR_BG = Color.parseColor("#0A0E27");
    private static final int COLOR_SCALE = Color.parseColor("#1A1F3A");
    private static final int COLOR_NUMBER = Color.parseColor("#8B92A8");
    private static final int COLOR_RPM = Color.parseColor("#FF4757");      // 转速红色
    private static final int COLOR_SPEED = Color.parseColor("#00D2FF");    // 速度蓝色
    private static final int COLOR_POINTER = Color.parseColor("#FFFFFF");
    private static final int COLOR_GLOW = Color.parseColor("#40FFFFFF");

    public AudiDashboard(Context context) {
        this(context, null);
    }

    public AudiDashboard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudiDashboard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化画笔
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scalePaint.setStyle(Paint.Style.STROKE);
        scalePaint.setStrokeCap(Paint.Cap.ROUND);

        numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        numberPaint.setStyle(Paint.Style.FILL);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setAntiAlias(true);

        pointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointerPaint.setStyle(Paint.Style.FILL);
        pointerPaint.setStrokeCap(Paint.Cap.ROUND);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);

        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        // 计算中心点和半径（调大两倍）
        float centerXTotal = w / 2f;
        float centerYTotal = h / 2f;
        float halfWidth = w / 4f; // 每个表盘占一半宽度

        radius = Math.min(halfWidth, height) * 0.82f * 2.0f;  // 调大两倍

        // 防止 radius 为 0 或负数
        if (radius <= 0) {
            radius = 200; // 默认值（也调大）
        }

        centerX = centerXTotal;
        centerY = centerYTotal;

        // 设置字体大小（调大两倍）
        numberPaint.setTextSize(radius * 0.12f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 防止 radius 未初始化
        if (radius <= 0) {
            return;
        }

        // 绘制背景
        drawBackground(canvas);

        // 绘制左侧转速表
        //drawTachometer(canvas, centerX - radius - radius * 0.15f, centerY);

        // 绘制右侧速度表
        drawSpeedometer(canvas, centerX/* + radius*/ /*+ radius * 0.15f*/, centerY);
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Canvas canvas) {
        // 深色渐变背景
        bgPaint.setColor(COLOR_BG);
        canvas.drawRect(0, 0, width, height, bgPaint);
    }

    /**
     * 绘制转速表（左侧）
     */
    private void drawTachometer(Canvas canvas, float cx, float cy) {
        // 绘制刻度
        drawScale(canvas, cx, cy, COLOR_RPM, RPM_MAX, 8, currentRPM);

        // 绘制数字
        drawNumbers(canvas, cx, cy, COLOR_NUMBER, RPM_MAX, 8, "x1000rpm");

        // 绘制圆弧进度
        drawArcProgress(canvas, cx, cy, COLOR_RPM, RPM_MAX, currentRPM);

        // 绘制指针
        drawPointer(canvas, cx, cy, COLOR_RPM, RPM_MAX, currentRPM);

        // 绘制中心圆
        drawCenterCircle(canvas, cx, cy);

        // 绘制数值显示
        drawValueDisplay(canvas, cx, cy + radius * 0.55f, (int) currentRPM, "rpm", COLOR_RPM);
    }

    /**
     * 绘制速度表（右侧）
     */
    private void drawSpeedometer(Canvas canvas, float cx, float cy) {
        // 绘制刻度
        drawScale(canvas, cx, cy, COLOR_SPEED, SPEED_MAX, 14, currentSpeed);

        // 绘制数字
        drawNumbers(canvas, cx, cy, COLOR_NUMBER, SPEED_MAX, 14, "km/h");

        // 绘制圆弧进度
        drawArcProgress(canvas, cx, cy, COLOR_SPEED, SPEED_MAX, currentSpeed);

        // 绘制指针
        drawPointer(canvas, cx, cy, COLOR_SPEED, SPEED_MAX, currentSpeed);

        // 绘制中心圆
        drawCenterCircle(canvas, cx, cy);

        // 绘制数值显示
        drawValueDisplay(canvas, cx, cy + radius * 0.55f, (int) currentSpeed, "km/h", COLOR_SPEED);
    }

    /**
     * 绘制刻度
     */
    private void drawScale(Canvas canvas, float cx, float cy, int color, int max, int count, float currentValue) {
        float scaleRadius = radius * 0.9f;
        scalePaint.setStrokeWidth(radius * 0.015f);

        for (int i = 0; i <= count; i++) {
            float value = (max / (float) count) * i;
            float angle = START_ANGLE + (SWEEP_ANGLE / count) * i;
            float radian = (float) Math.toRadians(angle);

            boolean isRedZone = (color == COLOR_RPM && value >= 6000);

            // 刻度线长度
            float innerRadius = scaleRadius - (i % 2 == 0 ? radius * 0.08f : radius * 0.04f);

            float x1 = cx + (float) Math.cos(radian) * innerRadius;
            float y1 = cy + (float) Math.sin(radian) * innerRadius;
            float x2 = cx + (float) Math.cos(radian) * scaleRadius;
            float y2 = cy + (float) Math.sin(radian) * scaleRadius;

            // 红色区域刻度变红
            scalePaint.setColor(isRedZone ? COLOR_RPM : COLOR_SCALE);
            canvas.drawLine(x1, y1, x2, y2, scalePaint);
        }
    }

    /**
     * 绘制数字
     */
    private void drawNumbers(Canvas canvas, float cx, float cy, int color, int max, int count, String unit) {
        float numberRadius = radius * 0.75f;

        for (int i = 0; i <= count; i += 2) { // 每隔一个画数字
            float value = (max / (float) count) * i;
            float angle = START_ANGLE + (SWEEP_ANGLE / count) * i;
            float radian = (float) Math.toRadians(angle);

            float x = cx + (float) Math.cos(radian) * numberRadius;
            float y = cy + (float) Math.sin(radian) * numberRadius;

            numberPaint.setColor(color);
            String text = String.valueOf(value / (max == RPM_MAX ? 1000 : 1));
            canvas.drawText(text, x, y + numberPaint.getTextSize() / 3f, numberPaint);
        }
    }

    /**
     * 绘制圆弧进度
     */
    private void drawArcProgress(Canvas canvas, float cx, float cy, int color, int max, float currentValue) {
        float arcRadius = radius * 0.95f;
        float arcWidth = radius * 0.06f;

        arcPaint.setStrokeWidth(arcWidth);
        arcPaint.setColor(color);

        // 发光效果
        arcPaint.setShadowLayer(radius * 0.15f, 0, 0, color);

        RectF rectF = new RectF(cx - arcRadius, cy - arcRadius, cx + arcRadius, cy + arcRadius);
        float sweep = (currentValue / max) * SWEEP_ANGLE;

        canvas.drawArc(rectF, START_ANGLE, sweep, false, arcPaint);

        // 清除阴影
        arcPaint.setShadowLayer(0, 0, 0, 0);
    }

    /**
     * 绘制指针
     */
    private void drawPointer(Canvas canvas, float cx, float cy, int color, int max, float currentValue) {
        float pointerLength = radius * 0.85f;
        float pointerWidth = radius * 0.03f;

        float angle = START_ANGLE + (currentValue / max) * SWEEP_ANGLE;
        float radian = (float) Math.toRadians(angle);

        // 指针路径
        Path pointerPath = new Path();
        float tipX = cx + (float) Math.cos(radian) * pointerLength;
        float tipY = cy + (float) Math.sin(radian) * pointerLength;

        // 指针尖端
        pointerPath.moveTo(tipX, tipY);

        // 指针底部
        float baseAngle1 = radian + (float) Math.PI / 2;
        float baseAngle2 = radian - (float) Math.PI / 2;
        float baseX1 = cx + (float) Math.cos(baseAngle1) * pointerWidth;
        float baseY1 = cy + (float) Math.sin(baseAngle1) * pointerWidth;
        float baseX2 = cx + (float) Math.cos(baseAngle2) * pointerWidth;
        float baseY2 = cy + (float) Math.sin(baseAngle2) * pointerWidth;

        pointerPath.lineTo(baseX1, baseY1);
        pointerPath.lineTo(baseX2, baseY2);
        pointerPath.close();

        // 指针渐变
        LinearGradient gradient = new LinearGradient(
                cx, cy, tipX, tipY,
                new int[]{color, Color.WHITE},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP
        );
        pointerPaint.setShader(gradient);

        // 发光效果
        pointerPaint.setShadowLayer(radius * 0.2f, 0, 0, color);

        canvas.drawPath(pointerPath, pointerPaint);

        // 清除效果
        pointerPaint.setShader(null);
        pointerPaint.setShadowLayer(0, 0, 0, 0);
    }

    /**
     * 绘制中心圆
     */
    private void drawCenterCircle(Canvas canvas, float cx, float cy) {
        float centerRadius = radius * 0.08f;

        // 外圈发光
        centerPaint.setColor(COLOR_GLOW);
        canvas.drawCircle(cx, cy, centerRadius * 2, centerPaint);

        // 中心圆
        RadialGradient gradient = new RadialGradient(
                cx, cy, centerRadius,
                new int[]{Color.WHITE, Color.parseColor("#CCCCCC")},
                null,
                Shader.TileMode.CLAMP
        );
        centerPaint.setShader(gradient);
        canvas.drawCircle(cx, cy, centerRadius, centerPaint);
        centerPaint.setShader(null);
    }

    /**
     * 绘制数值显示
     */
    private void drawValueDisplay(Canvas canvas, float cx, float cy, int value, String unit, int color) {
        // 数值
        numberPaint.setTextSize(radius * 0.28f);
        numberPaint.setColor(Color.WHITE);
        numberPaint.setFakeBoldText(true);
        canvas.drawText(String.valueOf(value), cx, cy, numberPaint);

        // 单位
        numberPaint.setTextSize(radius * 0.1f);
        numberPaint.setColor(COLOR_NUMBER);
        numberPaint.setFakeBoldText(false);
        canvas.drawText(unit, cx, cy + radius * 0.18f, numberPaint);
    }

    /**
     * 设置转速（带动画）
     */
    public void setRPM(float rpm) {
        targetRPM = Math.max(0, Math.min(rpm, RPM_MAX));

        if (rpmAnimator != null) {
            rpmAnimator.cancel();
        }

        rpmAnimator = ValueAnimator.ofFloat(currentRPM, targetRPM);
        rpmAnimator.setDuration(600);
        rpmAnimator.setInterpolator(new DecelerateInterpolator(2f));
        rpmAnimator.addUpdateListener(animation -> {
            currentRPM = (float) animation.getAnimatedValue();
            if (getWidth() > 0 && getHeight() > 0) {  // 确保 View 已经布局
                postInvalidate();
            }
        });
        rpmAnimator.start();
    }

    /**
     * 获取当前速度（动画中间值，用于距离计算）
     */
    public float getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * 设置速度（带动画）
     */
    public void setSpeed(float speed) {
        targetSpeed = Math.max(0, Math.min(speed, SPEED_MAX));

        if (speedAnimator != null) {
            speedAnimator.cancel();
        }

        speedAnimator = ValueAnimator.ofFloat(currentSpeed, targetSpeed);
        speedAnimator.setDuration(600);
        speedAnimator.setInterpolator(new DecelerateInterpolator(2f));
        speedAnimator.addUpdateListener(animation -> {
            currentSpeed = (float) animation.getAnimatedValue();
            if (getWidth() > 0 && getHeight() > 0) {  // 确保 View 已经布局
                postInvalidate();
            }
        });
        speedAnimator.start();
    }

    /**
     * 同时设置转速和速度
     */
    public void setValues(float rpm, float speed) {
        setRPM(rpm);
        setSpeed(speed);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (rpmAnimator != null) {
            rpmAnimator.cancel();
        }
        if (speedAnimator != null) {
            speedAnimator.cancel();
        }

        handler.removeCallbacksAndMessages(null);
    }

    public void startSimulation(){
        handler.sendEmptyMessageDelayed(1, 800);
    }

    public void stopSimulation(){
        handler.removeMessages(1);
        postDelayed(() -> setSpeed(0f), DURATION_TIME);
    }

    Random  random = new Random();
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1){
                removeMessages(1);
                sendEmptyMessageDelayed(1, DURATION_TIME);

                int maxDrop = 50;
                int last = random.nextInt(281);   // 0..280
                int lower = Math.max(0, last - maxDrop);
                last = lower + random.nextInt(281 - lower);
                setSpeed(last);
            }
        }
    };
}