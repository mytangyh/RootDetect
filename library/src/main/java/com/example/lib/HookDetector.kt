package com.example.lib

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader


class HookDetector : IDetection {


    fun check_proc_task(): Boolean {
        val dir = File("/proc/self/task/")
        val tasks = dir.listFiles()
        if (tasks != null) {
            for (task in tasks) {
                try {
                    val reader = BufferedReader(FileReader(task.absolutePath + "/status"))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        if (line?.contains("gmain") == true || line?.contains("pool-frida") == true || line?.contains(
                                "gdbus"
                            ) == true || (line?.contains("gum-js-loop") == true)
                        ) {
                            return true
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }


    override fun isDetected(context: Context): Boolean {
        return false
    }

    override fun getResults(): List<String> {
        TODO("Not yet implemented")
    }
}