package com.example.lib

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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


    // 获取Build信息
    private fun getBuildInfo(): List<String> {
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
        val propertyValue = System.getProperty("ro.kernel.qemu") ?: "null"
        return "ro.kernel.qemu:$propertyValue"
    }


    /**
     * 读取驱动文件, 检查是否包含已知的qemu驱动
     *
     * @return `true` if any known drivers where found to exist or `false` if not.
     */
    private fun checkForQEMU(): Boolean {
        val knownQEMUFiles = listOf("goldfish", "pipe", "qemud", "bochs", "ttvm", "nox", "vbox")

        for (qemuFile in knownQEMUFiles) {
            if (File("/system/bin/$qemuFile").exists() || File("/system/xbin/$qemuFile").exists()) {
                return true
            }
        }
        return false
    }


    override fun isDetected(): Boolean {
        TODO("Not yet implemented")
    }

    fun getResults(context: Context): List<String>{
        return listOf(
            "getAllSensors:  "+getAllSensors(context).toString()+"\n",
            "getBuildInfo:"+getBuildInfo().toString()+"\n",
            "hasQEmuProps:"+hasQEmuProps()+"\n",
            "checkForQEMU:"+checkForQEMU().toString()+"\n",
        )
    }
    override fun getResults(): List<String> {

        return listOf(
        )
    }

}

