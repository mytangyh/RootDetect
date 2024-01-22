package com.example.lib

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader


class HookDetector {

    companion object {
        fun isDetected(): Boolean {
            return Native.checkFrida() || check_proc_task()
        }

            fun printFileNames() {
                try {
                    val process = Runtime.getRuntime().exec("ls /data/local/tmp/")
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        println(line)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        private fun check_proc_task(): Boolean {
            val dir = File("/proc/self/task/")
            val tasks = dir.listFiles()
            if (tasks != null) {
                for (task in tasks) {
                    try {
                        val reader = BufferedReader(FileReader(task.absolutePath + "/status"))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
//                            Log.d("TAG", "$line")
                            if (line?.contains("gmain") == true || line?.contains("pool-frida") == true || line?.contains(
                                    "gdbus"
                                ) == true || (line?.contains("gum-js-loop") == true)||(line?.contains("linjector ") == true)
                            ) {
                                Log.e("TAG", "$line")
//                                return true
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return false
        }
    }


}