package com.example.lib

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.telephony.TelephonyManager
import java.io.File

class EmulatorDetector : IDetection {
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

    // 运营商信息
    fun getOperatorName(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.networkOperatorName
    }

    // 获取Build信息
    fun getBuildInfo(): List<String> {
        return listOf(
            Build.MODEL,
            Build.BRAND,
            Build.DEVICE,
            Build.MANUFACTURER,
            Build.PRODUCT,
            Build.FINGERPRINT
        )
    }

    //检测ro.kernel.qemu是否为1，内核qemu
    private fun hasQEmuProps(): String {
        val propertyValue = getRoProperty("ro.kernel.qemu")
        return "ro.kernel.qemu:$propertyValue"
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

    /**
     * 读取驱动文件, 检查是否包含已知的qemu驱动
     *
     * @return `true` if any known drivers where found to exist or `false` if not.
     */
    fun checkForQEMU(): Boolean {
        val knownQEMUFiles = listOf("goldfish", "pipe", "qemud", "bochs", "ttvm", "nox", "vbox")

        for (qemuFile in knownQEMUFiles) {
            if (File("/system/bin/$qemuFile").exists() || File("/system/xbin/$qemuFile").exists()) {
                return true
            }
        }
        return false
    }

    private fun getUserAppNumber(): String {
        val userApps = Runtime.getRuntime().exec("pm list package -3")
        return userApps.toString()
    }
    override fun isDetected(): Boolean {
        TODO("Not yet implemented")
    }

    fun getResults(context: Context): List<String>{
        return listOf(
            getAllSensors(context).toString(),
            getOperatorName(context),
            getBuildInfo().toString(),
            hasQEmuProps(),
            checkForQEMU().toString(),
            getUserAppNumber()
        )
    }
    override fun getResults(): List<String> {

        return listOf(
            hasQEmuProps(),
            checkForQEMU().toString(),
            getUserAppNumber()
        )
    }

}

