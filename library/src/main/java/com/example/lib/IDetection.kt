package com.example.lib

import android.content.Context

interface IDetection {
    fun isDetected(context: Context): Boolean
    fun getResults(): List<String>
}