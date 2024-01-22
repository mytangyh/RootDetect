package com.example.rootcheck;

import static android.text.InputType.TYPE_CLASS_TEXT;
import static android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

public class SecureEditText extends androidx.appcompat.widget.AppCompatEditText {
    public SecureEditText(Context context) {
        super(context);
    }

    public SecureEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SecureEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    private void init() {
        // 添加文本变化监听器
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 在文本变化之前执行的操作
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 在文本变化时执行的操作
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


        });

        // 设置输入类型为密码
        setInputType(TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD);
    }
  }
