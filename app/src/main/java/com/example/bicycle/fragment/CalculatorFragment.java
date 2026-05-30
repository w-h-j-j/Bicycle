package com.example.bicycle.fragment;

import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.bicycle.R;
import com.example.bicycle.XLog;
import com.example.bicycle.databinding.FragmentCalculatorBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 计算器 Fragment
 * 功能：
 * 1. 基础四则运算
 * 2. 百分比计算
 * 3. 小数支持
 * 4. 震动反馈效果
 */
public class CalculatorFragment extends Fragment implements View.OnClickListener {

    private FragmentCalculatorBinding binding;
    private Vibrator vibrator;

    private String currentInput = "0";
    private String expression = "";
    private double firstOperand = 0;
    private String operator = "";
    private boolean isNewInput = true;
    private boolean hasDecimal = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_calculator, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        XLog.d("CalculatorFragment 创建");
        initViews();
        initVibrator();
    }

    private void initViews() {
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

        binding.btnAdd.setOnClickListener(this);
        binding.btnSubtract.setOnClickListener(this);
        binding.btnMultiply.setOnClickListener(this);
        binding.btnDivide.setOnClickListener(this);
        binding.btnPercent.setOnClickListener(this);
        binding.btnDecimal.setOnClickListener(this);

        binding.btnClear.setOnClickListener(this);
        binding.btnDelete.setOnClickListener(this);
        binding.btnEquals.setOnClickListener(this);
    }

    private void initVibrator() {
        vibrator = (Vibrator) requireContext().getSystemService(Vibrator.class);
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(30);
        }
    }

    @Override
    public void onClick(View v) {
        vibrate();

        int id = v.getId();
        if (id == R.id.btn_0 || id == R.id.btn_1 || id == R.id.btn_2 ||
            id == R.id.btn_3 || id == R.id.btn_4 || id == R.id.btn_5 ||
            id == R.id.btn_6 || id == R.id.btn_7 || id == R.id.btn_8 || id == R.id.btn_9) {
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

    private void onOperatorClick(String op) {
        if (!expression.isEmpty() && !isNewInput) {
            calculate();
        }

        firstOperand = Double.parseDouble(currentInput);
        operator = op;
        expression = currentInput + " " + op;
        isNewInput = true;
        hasDecimal = false;
        updateDisplay();
    }

    private void onPercentClick() {
        try {
            double value = Double.parseDouble(currentInput);
            if (!expression.isEmpty() && !operator.isEmpty()) {
                value = firstOperand * (value / 100);
            } else {
                value = value / 100;
            }
            currentInput = formatNumber(value);
            hasDecimal = currentInput.contains(".");
            updateDisplay();
        } catch (NumberFormatException e) {
            XLog.e("百分比计算错误: " + e.getMessage());
        }
    }

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

    private void onClearClick() {
        currentInput = "0";
        expression = "";
        firstOperand = 0;
        operator = "";
        isNewInput = true;
        hasDecimal = false;
        updateDisplay();
    }

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

    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        } else {
            BigDecimal bd = new BigDecimal(number);
            bd = bd.setScale(8, RoundingMode.HALF_UP);
            return bd.stripTrailingZeros().toPlainString();
        }
    }

    private void updateDisplay() {
        binding.tvResult.setText(currentInput);
        binding.tvExpression.setText(expression);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (vibrator != null) {
            vibrator.cancel();
        }
        binding = null;
    }
}