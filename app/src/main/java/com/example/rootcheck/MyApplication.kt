package com.example.rootcheck

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

class MyApplication : Application() {
    var isAppInForeground = false
    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks(AppLifecycleTracker())
    }
    inner class AppLifecycleTracker : ActivityLifecycleCallbacks {

        private var activityReferences = 0
        private var isActivityChangingConfigurations = false

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                // App enters foreground
            }
        }

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            isActivityChangingConfigurations = activity.isChangingConfigurations
            if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                // App enters background
                Toast.makeText(applicationContext, "App moved to background", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }
    companion object {
        private lateinit var instance:Application

        fun applicationContext() : Context {
            return instance.applicationContext
        }
    }
}