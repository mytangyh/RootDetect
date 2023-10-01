package com.example.lib

interface IDetection {
    fun isDetected(): Boolean
    fun getResults(): List<String>
}