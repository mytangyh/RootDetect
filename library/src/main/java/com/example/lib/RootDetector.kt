package com.example.lib

import android.util.Log
import java.io.File
import java.io.IOException
import java.util.Scanner

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
    private fun checkForRWPaths() {
        val pathsThatShouldNotBeWritable = arrayOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin",
            "/etc",
        )

        try {
            val inputStream = Runtime.getRuntime().exec("mount").inputStream ?: return
            val propVal = Scanner(inputStream).useDelimiter("\\A").next()
            val lines = propVal.split("\n")
            Log.d(TAG, "lines: $lines")
            val sdkVersion = android.os.Build.VERSION.SDK_INT

            for (line in lines) {
                val args = line.split(" ")
                Log.d(TAG, "args: $args")

                if ((sdkVersion <= android.os.Build.VERSION_CODES.M && args.size < 4) ||
                    (sdkVersion > android.os.Build.VERSION_CODES.M && args.size < 6)
                ) {
                    Log.e(TAG, "Error formatting mount line: $line")
                    detectedResults.add("Error formatting mount line: $line")
                    continue
                }

                val mountPoint: String
                val mountOptions: String

                if (sdkVersion > android.os.Build.VERSION_CODES.M) {
                    mountPoint = args[2]
                    mountOptions = args[5]
                } else {
                    mountPoint = args[1]
                    mountOptions = args[3]
                }

                if (pathsThatShouldNotBeWritable.any { it.equals(mountPoint, ignoreCase = true) }) {
                    if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.M) {
                        val cleanedMountOptions = mountOptions.replace("(", "").replace(")", "")
                        if (cleanedMountOptions.split(",").any { it.equals("rw", ignoreCase = true) }) {
                            Log.e(TAG, "$mountPoint 路径以rw权限挂载! $line")
                            detectedResults.add("$mountPoint 路径以rw权限挂载! $line \n")
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading mount information", e)
        } catch (e: NoSuchElementException) {
            Log.e(TAG, "Error reading mount information", e)
        }
    }

    companion object {
        private const val TAG = "MountChecker"
    }

    override fun isDetected(): Boolean {
        detectRootPermission()
        detectFiles()
        checkForRWPaths()
        return true
    }

    override fun getResults(): List<String> {
        return detectedResults
    }
    private fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}