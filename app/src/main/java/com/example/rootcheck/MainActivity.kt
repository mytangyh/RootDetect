package com.example.rootcheck

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lib.Hook
import com.example.lib.HookDetector
import com.example.lib.Native
import com.example.lib.RootDetector
import com.example.rootcheck.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.tencent.mmkv.MMKV


class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MMKV.initialize(this)
        if (!MMKVUtils.getString("first").equals("first")){
            MMKVUtils.saveString("first","first")
            Log.d("TAG", "onCreate: first")
        }

        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        init()
        mbinding.viewPager.adapter=MyPagerAdapter(this)
        TabLayoutMediator(mbinding.tabLayout, mbinding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Tab 1"
                1 -> tab.text = "Tab 2"
            }
        }.attach()
    }
    @SuppressLint("SetTextI18n")
    private fun init(){
        val rootDetection = RootDetector()
        val isRooted = rootDetection.isDetected(this)
        MMKVUtils.put("isRooted","isRooted")
        if (isRooted){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Root Warning!!")
            builder.setMessage(MMKVUtils.getString("isRooted"))
            builder.setPositiveButton("确定") { _, _ ->
//                finish()
            }
            val dialog = builder.create()
            dialog.show()
        }

//        mbinding.webview.loadDataWithBaseURL(null,str,"text/html", "utf-8",null)
        mbinding.checkBtn.setOnClickListener {
//            val rootDetection = RootDetector()

//            val isRooted = rootDetection.isDetected()
//            val results = rootDetection.getResults()
//            val emulatorDetector = EmulatorDetector()
//            val detected = emulatorDetector.isDetected(this)
//            val results = emulatorDetector.getResults()
//            var distinguishVM = Emulator.instance?.distinguishVM(baseContext, 1)
//            mbinding.resultText.text = "Is Rooted: " + isRooted + "\nresults: " + results + "\n " + distinguishVM.toString()
            val hook = HookDetector()
//            val str="com.hexin.yuqing"
//            val appInstalled = isAppInstalled(this, str)
            val country = getCountry(this)
            val fridaServerRunning = hook.isDetected(this)
            MMKVUtils.put("test","ssss")
            val get = MMKVUtils.get<String>("test")

            val nativeString = Native.getNativeString()
            mbinding.resultText.text = "nativeString:${nativeString}"


        }
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

    class MyPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 2 // 两个标签页
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DetailFragment.newInstance("","")
                1 -> AbstractFragment.newInstance("","")
                else -> throw IllegalArgumentException("Invalid position")
            }
        }
    }


}