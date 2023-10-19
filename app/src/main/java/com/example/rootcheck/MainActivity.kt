package com.example.rootcheck

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lib.Emulator
import com.example.lib.EmulatorDetector
import com.example.lib.RootDetector
import com.example.rootcheck.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var mbinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mbinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mbinding.root)
        init()
    }
    @SuppressLint("SetTextI18n")
    private fun init(){
//        getExternalFilesDir(null)


        mbinding.checkBtn.setOnClickListener {
//            val rootDetection = RootDetector()

//            val isRooted = rootDetection.isDetected()
//            val results = rootDetection.getResults()
            val emulatorDetector = EmulatorDetector().getResults(this)
//            var distinguishVM = Emulator.instance?.distinguishVM(baseContext, 1)
//            mbinding.resultText.text = "Is Rooted: " + isRooted + "\nresults: " + results + "\n " + distinguishVM.toString()
            mbinding.resultText.text = emulatorDetector.toString()

        }
    }
}