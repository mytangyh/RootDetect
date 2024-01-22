package com.example.rootcheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {

    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        test();

    }
    private void test(){
        this.getWindow().getDecorView().post(() -> {
            final  ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm.hasPrimaryClip()){
                ClipData cd = cm.getPrimaryClip();
                if (cd!=null){
                    String content = cd.getItemAt(0).coerceToText(mContext).toString();
                    Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}