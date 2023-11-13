package com.example.rootcheck

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.lib.EmulatorDetector
import com.example.rootcheck.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)

        mbinding.viewPager.adapter=MyPagerAdapter(this)
        TabLayoutMediator(mbinding.tabLayout, mbinding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Tab 1"
                1 -> tab.text = "Tab 2"
            }
        }.attach()
        init()
    }
    @SuppressLint("SetTextI18n")
    private fun init(){
//        getExternalFilesDir(null)


        mbinding.checkBtn.setOnClickListener {
//            val rootDetection = RootDetector()

//            val isRooted = rootDetection.isDetected()
//            val results = rootDetection.getResults()
            val emulatorDetector = EmulatorDetector()
            val detected = emulatorDetector.isDetected(this)
            val results = emulatorDetector.getResults()
//            var distinguishVM = Emulator.instance?.distinguishVM(baseContext, 1)
//            mbinding.resultText.text = "Is Rooted: " + isRooted + "\nresults: " + results + "\n " + distinguishVM.toString()
//            val hook = Hook()
//            val tr = hook.detectXposedModules(this, true)
//            val fa = hook.detectXposedModules(this, false)
            mbinding.resultText.text = "detected:$detected\n,results:$results"


        }
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