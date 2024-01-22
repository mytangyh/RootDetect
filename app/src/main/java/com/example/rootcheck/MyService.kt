package com.example.rootcheck

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log




class MyService:Service() {
    private val TAG = "MyService"

    override fun onCreate() {
        Log.e(TAG, "onCreate ${clip.testgetSp(this)}")
//        clip.testSp(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand: ${clip.testgetSp(this)}")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy: ${clip.testgetSp(this)}")
    }
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}