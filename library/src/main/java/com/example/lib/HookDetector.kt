package com.example.lib

import android.app.ActivityManager
import android.content.Context
import java.io.File

class HookDetector :IDetection {

    private fun findFridaFile(): Boolean {
        val directory = File("/data/local/tmp/")

        // 检查目录是否存在
        if (directory.exists() && directory.isDirectory) {
            // 获取目录下的所有文件
            val files = directory.listFiles()

            // 遍历文件列表，检查文件名是否包含 "frida"
            files?.let {
                for (file in it) {
                    LogUtil.d(file.name)
                    if (file.name.contains("frida", ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }
    fun getFileNamesInDataLocalTmp(context: Context): List<String> {
        // 获取 Context.getExternalFilesDir() 方法返回的文件夹
        val directory = context.getExternalFilesDir("/data/local/tmp/")

        // 获取所有文件
        val files = directory?.listFiles()

        // 将文件名存储到列表中
        val fileNames = mutableListOf<String>()
        if (files != null) {
            for (file in files) {
                fileNames.add(file.name)
            }
        }

        // 返回文件名列表
        return fileNames
    }
    override fun isDetected(context: Context): Boolean {
        getFileNamesInDataLocalTmp(context)
      return findFridaFile()
    }

    override fun getResults(): List<String> {
        TODO("Not yet implemented")
    }
}