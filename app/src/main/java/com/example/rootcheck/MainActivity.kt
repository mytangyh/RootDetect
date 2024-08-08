package com.example.rootcheck

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.rootcheck.databinding.ActivityMainBinding
import kotlin.io.encoding.ExperimentalEncodingApi
import android.Manifest
import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.webkit.WebView.setWebContentsDebuggingEnabled
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        KeyStoreUtil.createKeys()
        init()

    }

    private fun startThreads() {
        val executorService: ExecutorService = Executors.newFixedThreadPool(5)

        for (i in 1..50) {
            executorService.execute {
                val versionInfo = getVersionInfo()
                runOnUiThread {
                    mbinding.tvTest.append("Thread $i: $versionInfo\n")
                }
            }
        }

        executorService.shutdown()
    }

    private fun getVersionInfo(): String {
        return try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            "Version Name: ${packageInfo.versionName}, Version Code: ${packageInfo.versionCode}"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "Version Info Not Found"
        }
    }

    private fun init() {
        mbinding.passNew.transformationMethod = CustomPasswordTransformationMethod()

        mbinding.checkBtn.setOnClickListener {

            startActivity(Intent(this, SecurityActivity::class.java))
        }
        mbinding.towTabBtn.setOnClickListener {
            startActivity(Intent(this, TabLayoutActivity::class.java))
        }
        mbinding.dialogBtn.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog()
            bottomSheetDialog.show(supportFragmentManager, "BottomSheetDialog")
        }
        var count = 0
        mbinding.buildBtn.setOnClickListener {
//            mbinding.tvTest.text = BuildHelper.getBuildInfo()
            // 获取版本号
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            mbinding.tvTest.text = "${
                Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            }$version  ${count++}\n"
//            mbinding.tvTest.text = RequestPrivacy(this).getPhoneState()
            startThreads()
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


