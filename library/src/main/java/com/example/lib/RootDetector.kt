package com.example.lib

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import kotlin.system.measureTimeMillis

/**
 * Author : Administrator
 * Time : 2023/09/17
 * Desc :
 */
class RootDetector : IDetection {
    private val detectedResults = mutableListOf<String>()

    /**
     * 检测是否存在特定文件
     *
     * @return 如果发现特定文件存在，返回true；否则返回false。
     */
    private fun detectFiles(): Boolean {
        // 用于存储检测结果的列表
        val results = mutableListOf<String>()

        // 定义可能包含特定文件的目录路径
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
            "/system/usr/we-need-root/",
            "/vendor/bin/",
            "/data/local/",
            "/cache/",
            "/dev"
        )

        // 需要检测的特定文件列表
        val files = arrayOf(
            "su",
            "busybox",
            "magisk",
            ".magisk"
        )

        // 生成所有可能的文件路径组合
        val filePaths = places.flatMap { place ->
            files.map { file -> "$place$file" }
        }

        // 将存在的文件路径添加到结果列表中
        results.addAll(filePaths.filter { fileExists(it) })

        // 额外的特定文件列表
        val etherFile = arrayOf(
            "/data/misc/hide_my_applist",
            "/data/system/xlua",
            "/storage/emulated/0/TWRP",
            "/storage/emulated/TWRP",
            "/data/data/com.saurik.substrate",
            "/data/misc/clipboard",
            "/system/app/Superuser.apk"
        )

        // 将存在的额外文件路径添加到结果列表中
        results.addAll(etherFile.filter { fileExists(it) })

        // 将检测结果记录到全局结果列表中
        if (results.isNotEmpty()){
            detectedResults.add("File exists:${results.toList()}")
        }

        // 如果存在特定文件，返回true；否则返回false
        return results.isNotEmpty()
    }


    /**
     * 主动执行SU检测是否具有Root权限。
     *
     * @return 如果具有Root权限，返回true；否则返回false。
     */
    private fun detectRootPermission(): Boolean {
        val isRoot: Boolean

        try {
            // 创建一个新的进程以执行"su"命令
            val process = Runtime.getRuntime().exec("su")

            // 获取进程的输出流并写入测试命令
            val os = process.outputStream
            os.write("echo test".toByteArray())
            os.flush()
            os.close()

            // 等待进程执行完毕并获取退出值
            val exitValue = process.waitFor()

            // 如果退出值为0，表示具有Root权限
            isRoot = exitValue == 0
        } catch (e: Exception) {
            Log.e(TAG, "detectRootPermission: $e", )
            return false
        }
        detectedResults.add("Root permission is $isRoot")
        return isRoot
    }


    /**
     * 检查特定目录是否以可读写权限挂载。
     *
     * @return 如果发现挂载为可写权限，则返回true；否则返回false。
     */
    private fun isPathWritable(mountPoint: String, mountOptions: String): Boolean {
        val pathsThatShouldNotBeWritable = arrayOf(
            "/system",
            "/system/bin",
            "/system/sbin",
            "/system/xbin",
            "/vendor/bin",
            "/sbin",
            "/etc"
        )

        if (pathsThatShouldNotBeWritable.any { it.equals(mountPoint, ignoreCase = true) }) {
            val cleanedMountOptions = mountOptions.replace("(", "").replace(")", "")
            return cleanedMountOptions.split(",").any { it.equals("rw", ignoreCase = true) }
        }
        return false
    }

    private fun getMountPointIndex(sdkVersion: Int): Int {
        return if (sdkVersion > android.os.Build.VERSION_CODES.M) 2 else 1
    }

    private fun getMountOptionsIndex(sdkVersion: Int): Int {
        return if (sdkVersion > android.os.Build.VERSION_CODES.M) 5 else 3
    }

    private fun checkForRWPaths(): Boolean {
        try {
            val inputStream = Runtime.getRuntime().exec("mount").inputStream ?: return false
            val propVal = inputStream.bufferedReader().use { it.readText() }
            val lines = propVal.split("\n")
            val sdkVersion = android.os.Build.VERSION.SDK_INT
            val mountPointIndex = getMountPointIndex(sdkVersion)
            val mountOptionsIndex = getMountOptionsIndex(sdkVersion)
            for (line in lines) {
                val args = line.split(" ")


                if (args.size < mountPointIndex + 1 || args.size < mountOptionsIndex + 1) {
                    continue
                }

                val mountPoint = args[mountPointIndex]
                val mountOptions = args[mountOptionsIndex]

                if (isPathWritable(mountPoint, mountOptions)) {
                    detectedResults.add("$mountPoint 路径以rw权限挂载! $line \n")
                    return true
                }
            }
        } catch (e: IOException) {
            return false
        }
        return false
    }


    /**
     * 检查设备是否具有调试模式。
     *
     * @return 如果设备处于调试模式，返回true；否则返回false。
     */
    private fun checkDeviceDebuggable(): Boolean {
        val buildTags = android.os.Build.TAGS
        detectedResults.add(buildTags)
        return buildTags != null && buildTags.contains("test-keys")
    }
    /**
     * 检查的secureProp和debugProp属性。
     *
     * @return
     */
    private fun checkAttribute():Boolean{
        val secureProp = getRoProperty("ro.secure")
        val debugProp = getRoProperty("ro.debuggable")
        detectedResults.add("Se:$secureProp de: $debugProp")
        return false
    }
    private fun getRoProperty(propertyName: String): Int {
        val propertyValue: String? = try {
            val roSecureObj = Class.forName("android.os.SystemProperties")
                .getMethod("get", String::class.java)
                .invoke(null, propertyName) as String?
            roSecureObj
        } catch (e: Exception) {
            null
        }

        return when (propertyValue) {
            null -> 1
            "0" -> 0
            else -> 1
        }
    }


    override fun isDetected(context: Context): Boolean {
        checkForRWPaths()
        return detectRootPermission()||detectFiles()||checkForRWPaths()
    }
    companion object{
        var TAG = "TAG"
    }

    override fun getResults(): List<String> {
        return detectedResults
    }

    private fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}