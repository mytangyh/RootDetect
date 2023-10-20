package com.example.lib

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.telephony.TelephonyManager
import java.io.*
import java.util.*

class EmulatorDetector : IDetection {

    /**
     * 检测 ro.kernel.qemu 是否为1，内核 qemu
     *
     * @return 1 为内核 qemu，0 不是内核 qemu
     */
    private fun hasQEmuProps(): Int {
        val propertyValue = System.getProperty("ro.kernel.qemu")
        return if (propertyValue == "1") 1 else 0
    }

    /**
     * 检查是否存在知名的 QEMU 文件或驱动
     *
     * @return 1 如果找到任何已知的 QEMU 文件或驱动，0 如果未找到。
     */
    private fun checkForQEMU(): Int {
        val knownFiles = arrayOf(
            "/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace", "/system/bin/qemu.props", "/system/bin/qemud"
        )


        var result = 0

        for (fileName in knownFiles) {
            val file = File(fileName)
            if (file.exists()) {
                result = 1
                break
            }
        }

        val knownQEMUDrivers = "goldfish"
        for (driversFile in arrayOf(
            File("/proc/tty/drivers"), File("/proc/cpuinfo")
        )) {
            if (driversFile.exists() && driversFile.canRead()) {
                val data = ByteArray(1024)
                try {
                    val inputStream = FileInputStream(driversFile)
                    inputStream.read(data)
                    inputStream.close()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                val driverData = String(data)

                if (driverData.contains(knownQEMUDrivers)) {
                    result = 1
                    break
                }
            }
        }

        return result
    }


    private fun checkBuildInfo(): Int {
        var result = 0

        // 检测CPU架构
        val supportedABIs = Build.SUPPORTED_ABIS
        val primaryABI = supportedABIs.firstOrNull() ?: ""
        result += if (primaryABI.contains("x86")) 1 else 0

        // 检测唯一识别码FINGERPRINT
        val isGeneric = Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.startsWith("generic_x86")
        result += if (isGeneric) 1 else 0
        val hasTestKeys =
            Build.FINGERPRINT.toLowerCase(Locale.getDefault()).contains("test-keys") || Build.FINGERPRINT.toLowerCase(
                Locale.getDefault()
            ).contains("dev-keys")
        result += if (hasTestKeys) 1 else 0

        // 检测MODEL
        val isEmulator =
            Build.MODEL.contains("Emulator") || Build.MODEL.contains("google_sdk") || Build.MODEL.contains("Android SDK built for x86") || Build.MODEL.contains(
                "Android SDK built for x86_64"
            )
        result += if (isEmulator) 1 else 0

        // 检测厂商信息
        val isGenymotion = Build.MANUFACTURER.contains("Genymotion") || Build.MANUFACTURER.contains("unknown")
        result += if (isGenymotion) 1 else 0

        // 检测BRAND、HARDWARE、DEVICE信息
        val isGenericBrand = Build.BRAND.startsWith("generic") || Build.BRAND.startsWith("generic_x86")
        result += if (isGenericBrand) 1 else 0
        val isGoldfishHardware = Build.HARDWARE == "goldfish"
        result += if (isGoldfishHardware) 1 else 0
        val isVbox86pDevice =
            Build.DEVICE == "vbox86p" || Build.DEVICE.startsWith("generic") || Build.DEVICE.startsWith("generic_x86") || Build.DEVICE.startsWith(
                "generic_x86_64"
            )
        result += if (isVbox86pDevice) 1 else 0

        // 检测PRODUCT信息
        val isGoogleProduct =
            Build.PRODUCT == "google_sdk" || Build.PRODUCT == "sdk" || Build.PRODUCT == "sdk_google" || Build.PRODUCT == "sdk_x86" || Build.PRODUCT == "vbox86p" || Build.PRODUCT == "sdk_google_phone_x86"
        result += if (isGoogleProduct) 1 else 0

        return result
    }

    /**
     * 判断CPU是否为电脑来判断模拟器
     * 注意安卓平板可能为Intel
     *
     * @return 1 为模拟器，0 不是模拟器
     */
    private fun checkCpu(): Int {
        var isEmulator = 0 // 默认为不是模拟器
        try {
            val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
            val reader = BufferedReader(InputStreamReader(process.inputStream, "utf-8"))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                if (line?.toLowerCase(Locale.getDefault())
                        ?.contains("intel") == true || line?.toLowerCase(Locale.getDefault())?.contains("amd") == true
                ) {
                    isEmulator = 1
                    break // 只要检测到一次就可以退出循环
                }
            }
            reader.close()
            process.waitFor()
        } catch (ex: IOException) {
            // 处理异常，可以根据需要添加日志或其他操作
        } catch (ex: InterruptedException) {
            // 处理异常，可以根据需要添加日志或其他操作
        }

        return isEmulator
    }

    /**
     * 特征参数-基带信息
     * 待测试
     */
    fun checkBaseBandValue(): Int {
        val baseBandVersion = System.getProperty("gsm.version.baseband")

        if (baseBandVersion.isNullOrEmpty()) {
            return 1
        } else if (baseBandVersion.contains("1.0.0.0")) {
            return 1
        } else {
            return 0
        }
    }



    /**
     * 获取所有传感器名字
     *
     * @param context
     * @return
     */
    private fun getAllSensors(context: Context): List<String> {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val sensorNames = mutableListOf<String>()
        for (sensor in sensorList) {
            sensorNames.add(sensor.name)
        }
        return sensorNames
    }


    private fun getOperatorName(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.networkOperatorName
    }

    override fun isDetected(): Boolean {
        TODO("Not yet implemented")
    }

    fun getResults(context: Context): List<String> {
        return listOf(
            "getAllSensors:  " + getAllSensors(context).toString() + "\n",
            "hasQEmuProps:" + hasQEmuProps() + "\n",
            "checkForQEMU:" + checkForQEMU().toString() + "\n",
        )
    }

    override fun getResults(): List<String> {

        return listOf(
        )
    }

}

