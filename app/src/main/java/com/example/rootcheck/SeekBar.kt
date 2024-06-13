package com.example.rootcheck

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rootcheck.ui.TextSeekBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SeekBar : AppCompatActivity() {
    private val updateInterval = 100L // 每秒更新一次
    private var progress = 0
    private val maxProgress = 100

    private lateinit var seekBar: TextSeekBar
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_seek_bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        seekBar = findViewById<TextSeekBar>(R.id.seekBar)
        startUpdatingSeekBar()
    }

    private fun startUpdatingSeekBar() {
        scope.launch {
            while (progress <= maxProgress) {
                seekBar.setPercent(progress/100f, "$progress%")
                progress += 1

                delay(updateInterval)
            }
        }
    }
}