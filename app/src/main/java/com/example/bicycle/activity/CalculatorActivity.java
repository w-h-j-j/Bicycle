package com.example.bicycle.activity;

import android.os.Bundle;
import android.os.Vibrator;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.bicycle.R;
import com.example.bicycle.XLog;
import com.example.bicycle.databinding.ActivityCalculatorBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算器页面
 * 功能：
 * 1. 基础四则运算（加减乘除）
 * 2. 百分比计算
 * 3. 小数支持
 * 4. 震动反馈效果
 */
public class CalculatorActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityCalculatorBinding binding;
    private Vibrator vibrator;

    // 计算器状态
    private String currentInput = "0";         // 当前输入
    private String expression = "";            // 表达式
    private double firstOperand = 0;           // 第一个操作数
    private String operator = "";              // 运算符
    private boolean isNewInput = true;         // 是否新输入
    private boolean hasDecimal = false;        // 是否已有小数点

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calculator);

        XLog.d("计算器启动");
        initViews();
        initVibrator();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        // 数字按钮
        binding.btn0.setOnClickListener(this);
        binding.btn1.setOnClickListener(this);
        binding.btn2.setOnClickListener(this);
        binding.btn3.setOnClickListener(this);
        binding.btn4.setOnClickListener(this);
        binding.btn5.setOnClickListener(this);
        binding.btn6.setOnClickListener(this);
        binding.btn7.setOnClickListener(this);
        binding.btn8.setOnClickListener(this);
        binding.btn9.setOnClickListener(this);

        // 运算符按钮
        binding.btnAdd.setOnClickListener(this);
        binding.btnSubtract.setOnClickListener(this);
        binding.btnMultiply.setOnClickListener(this);
        binding.btnDivide.setOnClickListener(this);
        binding.btnPercent.setOnClickListener(this);
        binding.btnDecimal.setOnClickListener(this);

        // 功能按钮
        binding.btnClear.setOnClickListener(this);
        binding.btnDelete.setOnClickListener(this);
        binding.btnEquals.setOnClickListener(this);
    }

    /**
     * 初始化震动器
     */
    private void initVibrator() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    /**
     * 触发震动反馈
     */
    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            // 轻微震动 30ms
            vibrator.vibrate(30);
        }
    }

    @Override
    public void onClick(View v) {
        // 触发震动
        vibrate();

        int id = v.getId();
        if (id == R.id.btn_0 || id == R.id.btn_1 || id == R.id.btn_2 ||
            id == R.id.btn_3 || id == R.id.btn_4 || id == R.id.btn_5 ||
            id == R.id.btn_6 || id == R.id.btn_7 || id == R.id.btn_8 || id == R.id.btn_9) {
            // 数字按钮
            onNumberClick(((android.widget.Button) v).getText().toString());
        } else if (id == R.id.btn_add) {
            onOperatorClick("+");
        } else if (id == R.id.btn_subtract) {
            onOperatorClick("-");
        } else if (id == R.id.btn_multiply) {
            onOperatorClick("×");
        } else if (id == R.id.btn_divide) {
            onOperatorClick("÷");
        } else if (id == R.id.btn_decimal) {
            onDecimalClick();
        } else if (id == R.id.btn_percent) {
            onPercentClick();
        } else if (id == R.id.btn_clear) {
            onClearClick();
        } else if (id == R.id.btn_delete) {
            onDeleteClick();
        } else if (id == R.id.btn_equals) {
            onEqualsClick();
        }
    }

    /**
     * 数字按钮点击
     */
    private void onNumberClick(String number) {
        if (isNewInput) {
            currentInput = number;
            isNewInput = false;
            hasDecimal = false;
        } else {
            if (currentInput.equals("0")) {
                currentInput = number;
            } else {
                currentInput += number;
            }
        }
        updateDisplay();
    }

    /**
     * 小数点按钮点击
     */
    private void onDecimalClick() {
        if (isNewInput) {
            currentInput = "0.";
            isNewInput = false;
            hasDecimal = true;
        } else if (!hasDecimal) {
            currentInput += ".";
            hasDecimal = true;
        }
        updateDisplay();
    }

    /**
     * 运算符按钮点击
     */
    private void onOperatorClick(String op) {
        if (!expression.isEmpty() && !isNewInput) {
            // 连续运算，先计算之前的结果
            calculate();
        }

        firstOperand = Double.parseDouble(currentInput);
        operator = op;
        expression = currentInput + " " + op;
        isNewInput = true;
        hasDecimal = false;
        updateDisplay();
    }

    /**
     * 百分比按钮点击
     */
    private void onPercentClick() {
        try {
            double value = Double.parseDouble(currentInput);
            if (!expression.isEmpty() && !operator.isEmpty()) {
                // 如果有运算符，计算百分比（如 200 的 50%）
                value = firstOperand * (value / 100);
            } else {
                // 否则直接除以 100
                value = value / 100;
            }
            currentInput = formatNumber(value);
            hasDecimal = currentInput.contains(".");
            updateDisplay();
        } catch (NumberFormatException e) {
            XLog.e("百分比计算错误: " + e.getMessage());
        }
    }

    /**
     * 等于按钮点击
     */
    private void onEqualsClick() {
        if (expression.isEmpty() || operator.isEmpty()) {
            return;
        }

        expression += " " + currentInput + " =";
        calculate();
        expression = "";
        operator = "";
        isNewInput = true;
        updateDisplay();
    }

    /**
     * 清除按钮点击
     */
    private void onClearClick() {
        currentInput = "0";
        expression = "";
        firstOperand = 0;
        operator = "";
        isNewInput = true;
        hasDecimal = false;
        updateDisplay();
    }

    /**
     * 删除按钮点击
     */
    private void onDeleteClick() {
        if (!isNewInput && currentInput.length() > 1) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            if (currentInput.endsWith(".")) {
                hasDecimal = false;
            }
        } else {
            currentInput = "0";
            isNewInput = true;
            hasDecimal = false;
        }
        updateDisplay();
    }

    /**
     * 执行计算
     */
    private void calculate() {
        if (operator.isEmpty()) return;

        double secondOperand = Double.parseDouble(currentInput);
        double result = 0;

        switch (operator) {
            case "+":
                result = firstOperand + secondOperand;
                break;
            case "-":
                result = firstOperand - secondOperand;
                break;
            case "×":
                result = firstOperand * secondOperand;
                break;
            case "÷":
                if (secondOperand == 0) {
                    currentInput = "错误";
                    expression = "不能除以零";
                    return;
                }
                result = firstOperand / secondOperand;
                break;
        }

        currentInput = formatNumber(result);
        hasDecimal = currentInput.contains(".");
    }

    /**
     * 格式化数字
     */
    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        } else {
            // 保留最多 8 位小数，去除末尾的 0
            BigDecimal bd = new BigDecimal(number);
            bd = bd.setScale(8, RoundingMode.HALF_UP);
            return bd.stripTrailingZeros().toPlainString();
        }
    }

    /**
     * 更新显示
     */
    private void updateDisplay() {
        binding.tvResult.setText(currentInput);
        binding.tvExpression.setText(expression);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}