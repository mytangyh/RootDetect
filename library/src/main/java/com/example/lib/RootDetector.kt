package com.example.lib

import android.util.Log
import java.io.File

/**
 * Author : Administrator
 * Time : 2023/09/17
 * Desc :
 */
class RootDetector : IDetection{
    private val detectedResults = mutableListOf<String>()

    //  检查特定文件是否存在
    private fun detectFiles():Boolean{
        val results = mutableListOf<String>()
        val places = arrayOf(
            "/sbin/",
            "/system/bin/",
            "/system/sbin/",
            "/system/xbin/",
            "/proc/self/root/bin/",
            "/data/local/xbin/",
            "/data/local/bin/",
            "/system/sd/xbin/",
            "/system/bin/failsafe/",
            "/vendor/bin/",
            "/data/local/",
            "/cache/"
        )

        val files = arrayOf(
            "su",
            "busybox",
            "magisk"
        )

        val filePaths = places.flatMap { place ->
            files.map { file -> "$place$file" }
        }
        results.addAll(filePaths.filter { fileExists(it) })

        val etherFile = arrayOf(
            "/data/misc/hide_my_applist",
            "/data/system/xlua",
            "/storage/emulated/0/TWRP",
            "/storage/emulated/TWRP",
            "/data/data/com.saurik.substrate",
            "/data/misc/clipboard"
        )
        results.addAll(etherFile.filter { fileExists(it) })
        detectedResults.add("File exists:${results.toList()}")
        return results.isNotEmpty()

    }

    // 执行su命令，主动申请超级用户
    private fun detectRootPermission(): Boolean {
        var isRoot = false
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = process.outputStream
            os.write("echo test".toByteArray())
            os.flush()
            os.close()
            val exitValue = process.waitFor()
            isRoot = exitValue == 0
        } catch (e: Exception) {
            Log.d("TAG", "detectRootPermission: $isRoot")
        }
        if (isRoot) {
            detectedResults.add("Root permission is granted")
        }
        return isRoot
    }
    // 查特定路径是否有写权限
    private fun detectWritePermission(): Boolean {
        val places = arrayOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sys",
            "/sbin",
            "/etc",
            "/proc",
            "/dev"
        )
        val results = places.map { place ->
            val file = File(place)
            file.canWrite()
        }
        detectedResults.add("Write permission:${results.toList()}")
        return results.any { it }
    }
    override fun isDetected(): Boolean {
        detectWritePermission()
        detectRootPermission()
        detectFiles()
        return true
    }

    override fun getResults(): List<String> {
        return detectedResults
    }
    private fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}