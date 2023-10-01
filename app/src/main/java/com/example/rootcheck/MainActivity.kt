package com.example.rootcheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    private fun init(){


        mbinding.checkBtn.setOnClickListener {
            val rootDetection = RootDetector()

            val isRooted = rootDetection.isDetected()
            val results = rootDetection.getResults()
            mbinding.resultText.text = "Is Rooted: $isRooted\nresults: $results"
        }
    }
}