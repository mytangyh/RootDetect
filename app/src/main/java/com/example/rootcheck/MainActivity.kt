package com.example.rootcheck

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.rootcheck.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    private var TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        init()

    }

    private fun init() {
        mbinding.passNew.transformationMethod = CustomPasswordTransformationMethod()

        mbinding.checkBtn.setOnClickListener {

            startActivity(Intent(this, SecurityActivity::class.java))
        }
        mbinding.towTabBtn.setOnClickListener {
            startActivity(Intent(this, TabLayoutActivity::class.java))
        }

    }


    class CustomPasswordTransformationMethod : PasswordTransformationMethod() {

        override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
            // 自定义密码转换逻辑
            return CustomPasswordCharSequence(source)
        }

        private class CustomPasswordCharSequence(private val source: CharSequence?) : CharSequence {

            override val length: Int
                get() = source?.length ?: 0

            override fun get(index: Int): Char {
                // 自定义密码显示的字符，这里使用了'*'
                return '*'
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                // 返回部分源字符序列
                return source?.subSequence(startIndex, endIndex) ?: ""
            }
        }
    }

}


