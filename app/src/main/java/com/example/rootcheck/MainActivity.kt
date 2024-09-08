package com.example.rootcheck

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rootcheck.databinding.ActivityMainBinding
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
            val bottomSheetDialog = BottomSheetDialogOne()
            bottomSheetDialog.show(supportFragmentManager, "BottomSheetDialog")
        }
        var count = 0
        mbinding.buildBtn.setOnClickListener {
            mbinding.tvTest.text = BuildHelper.getBuildInfo()
            // 获取版本号
            val version = packageManager.getPackageInfo(packageName, 0).versionName
//            mbinding.tvTest.text = "${
//                Settings.Secure.getString(
//                    contentResolver,
//                    Settings.Secure.ANDROID_ID
//                )
//            }$version  ${count++}\n"
//            mbinding.tvTest.text = RequestPrivacy(this).getPhoneState()
//            startThreads()
//            mbinding.tvTest.text = BuildHelper
            if ("HONOR".equals(Build.BRAND)){
                Toast.makeText(this, "\"HONOR\".equals(Build.BRAND)", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "HONOR.notEquals(Build.BRAND)", Toast.LENGTH_SHORT).show()
            }
        }
        mbinding.webBtn.setOnClickListener {
            startActivity(Intent(this, WebViewActivity::class.java))
        }
        mbinding.imageBtn.setOnClickListener {
            startActivity(Intent(this, ImageActivity::class.java))
        }
        mbinding.RSABtn.setOnClickListener {
            startActivity(Intent(this, RsaActivity::class.java))
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


