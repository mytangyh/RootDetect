package com.example.rootcheck

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lib.EmulatorDetectorNew
import com.example.lib.HookDetector
import com.example.lib.LogUtil
import com.example.lib.ProxyDetector
import com.example.rootcheck.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    private var TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        init()
        mbinding.viewPager.adapter = MyPagerAdapter(this)
        TabLayoutMediator(mbinding.tabLayout, mbinding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Tab 1"
                1 -> tab.text = "Tab 2"
            }
        }.attach()
//                val myServiceIntent = Intent(this@MainActivity, MyService::class.java)
//        startService(myServiceIntent)
//        val twoService = Intent(this, TwoService::class.java)
//        startService(twoService)
//        var i = 1
//        while (i < 40) {
//            sleep(100)
//            Log.e(TAG, "MainActivity SP $i : ${SpTest.testSp()}")
//
//            i++
//        }

    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        mbinding.passNew.transformationMethod = CustomPasswordTransformationMethod()

//        val rootDetection = RootDetector()
//        val isRooted = rootDetection.isDetected(this)
//        MMKVUtils.put("isRooted","isRooted")
        val emulatorDetectorNew = EmulatorDetectorNew()
        var detected = false
        var checkFrida: Boolean

//        val measureTimeMillis = measureTimeMillis{
//            checkFrida = HookDetector.isDetected()
//        }
        val buildStr="\nBRAND:${Build.BRAND} \nDEVICE:${Build.DEVICE} \nRELEASE:${Build.VERSION.RELEASE} \nMODEL:${Build.MODEL}"



//        if (measureTimeMillis>0) {
//
//            val builder = AlertDialog.Builder(this)
//            builder.setTitle("checkFrida!!")
//            builder.setMessage("time:$measureTimeMillis frida:$checkFrida \nbuild:$buildStr")
//            builder.setPositiveButton("确定") { _, _ ->
////                finish()
//            }
//            val dialog = builder.create()
//            dialog.show()
//        }
//        clip.testSp(this)
        LogUtil.d("sp:${clip.testgetSp(this)}")

//        mbinding.webview.loadDataWithBaseURL(null,str,"text/html", "utf-8",null)
        mbinding.checkBtn.setOnClickListener {
//            val rootDetection = RootDetector()

//            val isRooted = rootDetection.isDetected()
//            val results = rootDetection.getResults()
//            var distinguishVM = Emulator.instance?.distinguishVM(baseContext, 1)
//            mbinding.resultText.text = "Is Rooted: " + isRooted + "\nresults: " + results + "\n " + distinguishVM.toString()
//            val str="com.hexin.yuqing"
//            val appInstalled = isAppInstalled(this, str)


//            val sp = clip.testgetSp(this)
//            var i = 1
//            while (i < 50) {
//                Log.d(TAG, "click SP $i : ${SpTest.testSp()}")
//
//                i++
//            }
            val xposedExists = HookDetector.isXposedExists()
            val eposedExistByThrow = HookDetector.isEposedExistByThrow()
            val tryShutdownXposed = HookDetector.tryShutdownXposed()
            val classCheck = HookDetector.classCheck()
            val exceptionCheck = HookDetector.exceptionCheck()
            Toast.makeText(this, toastMessage(), Toast.LENGTH_SHORT).show()

//            val checkHttpProxy = ProxyDetector.checkHttpProxy()
//            val vpnConnected1 = ProxyDetector.isVpnConnected(this)
//            val deviceInVPN = ProxyDetector.isDeviceInVPN()

            val showText = "getCountry:${getCountry(this).also { LogUtil.d(it) }}\n getNetName:${getNetName(this)}"
            mbinding.resultText.text = "proxy:${ProxyDetector.isDetected(this)}\n showText\n:$showText"



        }

    }
    private fun toastMessage(): String {
        return "我未被劫持"
    }
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    private fun getCountry(context: Context): String {
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        return tm.networkCountryIso
    }
    private fun getNetName(context: Context): String {
        val tm = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        return tm.simOperatorName
    }

    class MyPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 2 // 两个标签页
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DetailFragment.newInstance("", "")
                1 -> AbstractFragment.newInstance("", "")
                else -> throw IllegalArgumentException("Invalid position")
            }
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


