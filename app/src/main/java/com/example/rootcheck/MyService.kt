package com.example.rootcheck

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlin.concurrent.thread


class MyService:Service() {
    private val TAG = "MyService"

    override fun onCreate() {
        var i = 1
        while (i < 10) {
            Log.e(TAG, "onCreate ${clip.testgetSp(this)}")
            i++
        }
//        clip.testSp(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var i = 1
        while (i < 10) {
            Log.e(TAG, "onStartCommand ${clip.testgetSp(this)}")
            i++
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy: ${clip.testgetSp(this)}")
    }
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}