package com.example.rootcheck

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log




class TwoService:Service() {
    private val TAG = "TwoService"

    override fun onCreate() {
        var i = 1
        while (i < 50) {
            Thread.sleep(82)
            Log.d(TAG, "onCreate SP $i : ${SpTest.testSp()}")

            i++
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var i = 1
        while (i < 50) {
            Thread.sleep(20)
            Log.d(TAG, "onStartCommand SP$i : ${SpTest.testSp()}")

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