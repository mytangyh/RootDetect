package com.example.rootcheck

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle

class MyApplication : Application() {
    var isAppInForeground = false
    override fun onCreate() {
        super.onCreate()
        instance = this
        val myServiceIntent = Intent(this, MyService::class.java)
        startService(myServiceIntent)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // 应用启动
            }

            override fun onActivityStarted(activity: Activity) {
                // 应用进入前台
                isAppInForeground = true
            }

            override fun onActivityResumed(activity: Activity) {
                // 应用可见
            }

            override fun onActivityPaused(activity: Activity) {
                // 应用失去焦点
            }

            override fun onActivityStopped(activity: Activity) {
                // 应用进入后台
                isAppInForeground = false
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // 保存状态
            }

            override fun onActivityDestroyed(activity: Activity) {
                // 销毁
            }
        })
    }
    private fun isBack(context:Context):Boolean{
        val activityManager  = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses
        for (app in appProcesses){
            if (app.processName.equals(context.packageName)) {
                return app.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND
            }
        }
        return false
    }
    companion object {
        private lateinit var instance:Application

        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }
}