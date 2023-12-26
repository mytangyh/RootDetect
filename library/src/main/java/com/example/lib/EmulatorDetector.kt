package com.example.lib

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import kotlin.system.measureTimeMillis

class EmulatorDetector : IDetection {
    private val detectedResults = mutableListOf<String>()

    /**
     * 检测网易MuMu模拟器
     * 模拟器版本 12
     * 安卓版本 12
     */
    private val mmAppName = arrayOf(
        "/data/data/com.mumu.launcher",
        "/data/data/com.mumu.store",
        "/data/data/com.netease.mumu.cloner",
        "/data/dalvik-cache/profiles/com.mumu.launcher",
        "/data/dalvik-cache/profiles/com.mumu.store"
    )
    private val mmAppPackage = arrayOf("com.mumu.store", "com.mumu.launcher", "com.mumu.audio")

    private fun isMuMu(context: Context): Int {
        val appPackageCount = hasAppPackage(context, mmAppPackage)
        detectedResults.add("appPackageCount:$appPackageCount\n")
        val markCount = isMark(mmAppName)
        detectedResults.add("markCount:$markCount\n")
        val model = Build.MODEL
        val isMuMuModel = if (model == "MuMu") 1 else 0
        return appPackageCount + markCount + isMuMuModel
    }

    /**
     * 检测夜神模拟器
     * 模拟器版本 7.0.59
     * 安卓版本 7.1.2
     */
    private val ysAppName = arrayOf(
        "/storage/emulated/legacy/BigNoxHD",
        "/lib/libnoxd.so",
        "/lib/libnoxspeedup.so",
        "/data/property/persist.nox.androidid",
        "system/app/Helper/NoxHelp_zh.apk",
        "/data/dalvik-cache/profiles/com.bignox.app.store.hd"
    )
    private val ysAppPackage = arrayOf("com.bignox.google.installer", "com.bignox.app.store.hd")

    private fun isNox(context: Context): Int {
        val appPackageCount = hasAppPackage(context, ysAppPackage)
        detectedResults.add("appPackageCount:$appPackageCount\n")
        val markCount = isMark(ysAppName)
        detectedResults.add("markCount:$markCount\n")
        return appPackageCount + markCount
    }


    /**
     * 检测逍遥模拟器
     * 模拟器版本 9.0.7
     * 安卓版本 9
     */

    private val xyAppName = arrayOf(
        "/data/data/com.microvirt.launcher",
        "/data/data/com.microvirt.download",
        "/data/data/com.microvirt.guide",
        "/data/data/com.microvirt.installer",
        "/data/data/com.microvirt.market",
        "/data/data/com.microvirt.memuime"
    )
    private val xyAppPackage = arrayOf(
        "com.microvirt.launcher",
        "com.microvirt.download",
        "com.microvirt.guide",
        "com.microvirt.installer",
        "com.microvirt.market",
        "com.microvirt.memuime"
    )

    private fun isXiaoYao(context: Context): Int {
        val appPackageCount = hasAppPackage(context, xyAppPackage)
        detectedResults.add("appPackageCount:$appPackageCount\n")
        val markCount = isMark(xyAppName)
        detectedResults.add("markCount:$markCount\n")
        return appPackageCount + markCount
    }


    /**
     * 检测Genymotion模拟器
     * 模拟器版本 3.0.1
     * 安卓版本5.0、9.0
     *
     *
     */
    private val geAppName = arrayOf(
        "/data/data/com.google.android.launcher.layouts.genymotion",
        "/system/app/GenymotionLayout/GenymotionLayout.apk",
        "/dev/socket/baseband_genyd",
        "system/bin/genymotion-vbox-sf"
    )
    private val geAppPackage = arrayOf(
        "com.genymotion.superuser", "com.genymotion.genyd", "com.genymotion.systempatcher"
    )

    private fun isGenymotion(context: Context): Int {
        val appPackageCount = hasAppPackage(context, geAppPackage)
        detectedResults.add("appPackageCount:$appPackageCount\n")
        val markCount = isMark(geAppName)
        detectedResults.add("markCount:$markCount\n")
//        var systemApp = CommonUtils.isSystemApp(context, geAppPackage)
//        detectedResults.add("systemApp:$systemApp\n")

        return appPackageCount + markCount
    }

    /**
     * 检测原生模拟器（AS自带的）
     * 针对QEMU, KVM, QEMU-KVM 和 Goldfish的理解
     * https://blog.csdn.net/span76/article/details/19165345
     */

    private val asAppName = arrayOf("/sys/module/goldfish_audio", "/sys/module/goldfish_sync")

    private fun isAS(): Int {
        var value = isMark(asAppName)
        value += if (Build.HARDWARE == "ranchu") 1 else 0
        return value
    }

    /**
     * 检测 ro.kernel.qemu 是否为1，内核 qemu
     *
     * @return 1 为内核 qemu，0 不是内核 qemu
     */
    private fun hasQEmuProps(): Int {
        val propertyValue = System.getProperty("ro.kernel.qemu")
        detectedResults.add("hasQEmuProps:$propertyValue\n")
        return if (propertyValue == "1") 1 else 0
    }

    /**
     * 检查是否存在知名的 QEMU 文件或驱动
     *
     * @return 1 如果找到任何已知的 QEMU 文件或驱动，0 如果未找到。
     */
    private fun checkForQEMU(): Int {
        val knownFiles = arrayOf(
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu.props",
            "/system/bin/qemud",
            "/dev/socket/qemud",
            "/dev/qemu_pipe",
            "/dev/qemu_trace"
        )
        var result = 0
        for (fileName in knownFiles) {
            if (fileExists(fileName)) {
                result += 1
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
                    result += 1
                    break
                }
            }
        }

        return result
    }


    private fun checkBuildInfo(): Int {
        var result = 0

        // 检测CPU架构
        val primaryABI = Build.SUPPORTED_ABIS.joinToString(",")
        if (primaryABI.contains("x86")) result++

        // 检测唯一识别码FINGERPRINT
        val isGeneric = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("generic_x86")
        val hasTestKeys =
            Build.FINGERPRINT.toLowerCase(Locale.getDefault()).contains("test-keys")
                    || Build.FINGERPRINT.toLowerCase(
                Locale.getDefault()
            ).contains("dev-keys")
        if (isGeneric || hasTestKeys) result += 1

        // 检测MODEL
        val isEmulator = Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains(
            "Android SDK built for x86"
        ) || Build.MODEL.contains(
            "Android SDK built for x86_64"
        )
        if (isEmulator) result++

        // 检测厂商信息
        val isGenymotion = Build.MANUFACTURER.contains("Genymotion") || Build.MANUFACTURER.contains("unknown")
        if (isGenymotion) result++

        // 检测BRAND、HARDWARE、DEVICE信息
        val isGenericBrand = Build.BRAND.startsWith("generic") || Build.BRAND.startsWith("generic_x86")
        if (isGenericBrand) result++
        val isGoldfishHardware = Build.HARDWARE == "goldfish"
        if (isGoldfishHardware) result++
        val isVbox86pDevice =
            Build.DEVICE == "vbox86p" || Build.DEVICE.startsWith("generic") || Build.DEVICE.startsWith(
                "generic_x86"
            ) || Build.DEVICE.startsWith(
                "generic_x86_64"
            )
        if (isVbox86pDevice) result++

        // 检测PRODUCT信息
        val isGoogleProduct = setOf(
            "google_sdk", "sdk", "sdk_google", "sdk_x86", "vbox86p", "sdk_google_phone_x86"
        ).contains(Build.PRODUCT)
        if (isGoogleProduct) result++
        detectedResults.add(
            "checkBuildInfo:\n " + Build.SUPPORTED_ABIS.joinToString(",") + "\n" + Build.FINGERPRINT + "\n" + Build.MODEL + "\n" + Build.MANUFACTURER + "\n" + Build.BRAND + "\n" + Build.HARDWARE + "\n" + Build.DEVICE + "\n"
        )

        return result
    }


    /**
     * 判断CPU是否为电脑来判断模拟器
     * 注意安卓平板可能为Intel
     *
     * @return 1 为模拟器，0 不是模拟器
     */
    private fun checkCpu(): Int {
        try {
            val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
            val reader = BufferedReader(InputStreamReader(process.inputStream, "utf-8"))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                if (line?.toLowerCase(Locale.getDefault())
                        ?.contains("intel") == true
                    || line?.toLowerCase(Locale.getDefault())?.contains("amd") == true
                ) {
                    reader.close()
                    process.waitFor()
                    detectedResults.add("checkCpu:$line\n")
                    return 1
                }
            }
            reader.close()
            process.waitFor()
        } catch (ex: Exception) {
            Log.e("checkCpu", ex.toString())
        }

        return 0
    }


    /**
     * 特征参数-基带信息
     * 空或者1.0.0.0为模拟器
     * 存在误判
     */
    private fun checkBaseBandValue(): Int {
        val baseBandVersion = Build.getRadioVersion()
        detectedResults.add("checkBaseBandValue:$baseBandVersion\n")
        return if (baseBandVersion.isNullOrEmpty()) {
            1
        } else if (baseBandVersion.contains("1.0.0.0")) {
            1
        } else {
            0
        }
    }


    /**
     * 获取所有传感器
     *
     * @param context
     * @return
     */
    private fun checkSensors(context: Context): Int {
        var result = 0
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (light == null) {
            detectedResults.add("checkLightSensors:light\n")
            result++
        }
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        sensorList.forEach { Log.d("sensorList", it.name) }
        detectedResults.add("checkSensors:${sensorList.size}\n")
        if (sensorList.size < 10) result++
        return result
    }

    private fun normalDetect(context: Context): Int {
        var result = 0
        result += hasQEmuProps() + checkForQEMU() + checkBuildInfo() + checkCpu() + checkBaseBandValue() + checkSensors(
            context
        ) + isMark(vBoxFile)
        return result
    }


    private fun fileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }

    private fun isMark(filePaths: Array<String>): Int {
        for (s in filePaths) {
            if (fileExists(s)) {
                Log.d("filePaths", s)
                detectedResults.add("isMark:$s\n")
                return 1
            }
        }
        return 0
    }

    private fun hasAppPackage(context: Context, app: Array<String>): Int {
        val packageManager = context.packageManager
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PackageManager.MATCH_UNINSTALLED_PACKAGES
        } else {
            PackageManager.GET_UNINSTALLED_PACKAGES
        }

        val installedPackages = packageManager.getInstalledPackages(flag)
        val count = installedPackages.count { installedPackage ->
            app.any { it == installedPackage.packageName }
        }
        return count
    }


    override fun isDetected(context: Context): Boolean {
        var result = 0
        val timeMillis = measureTimeMillis {
            result =
                isAS() + isGenymotion(context) + isMuMu(context) + isNox(context) + isXiaoYao(context) + normalDetect(
                    context
                )
        }
        detectedResults.add("timeMillis:$timeMillis \n result: $result\n")
        Log.d("result", result.toString())
        return result > 0
    }


    override fun getResults(): List<String> {
        return detectedResults
    }

    private val vBoxFile = arrayOf(
        "/data/youwave_id",
        "/dev/vboxguest",
        "/dev/vboxuser",
        "/mnt/prebundledapps/bluestacks.prop.orig",
        "/mnt/prebundledapps/propfiles/ics.bluestacks.prop.note",
        "/mnt/prebundledapps/propfiles/ics.bluestacks.prop.s2",
        "/mnt/prebundledapps/propfiles/ics.bluestacks.prop.s3",
        "/proc/irq/9/vboxguest",
        "/sys/bus/pci/drivers/vboxguest",
        "/sys/bus/pci/drivers/vboxguest/0000:00:04.0",
        "/sys/bus/pci/drivers/vboxguest/bind",
        "/sys/bus/pci/drivers/vboxguest/module",
        "/sys/bus/pci/drivers/vboxguest/new_id",
        "/sys/bus/pci/drivers/vboxguest/remove_id",
        "/sys/bus/pci/drivers/vboxguest/uevent",
        "/sys/bus/pci/drivers/vboxguest/unbind",
        "/sys/bus/platform/drivers/qemu_pipe",
        "/sys/bus/platform/drivers/qemu_trace",
        "/sys/class/bdi/vboxsf-c",
        "/sys/class/misc/vboxguest",
        "/sys/class/misc/vboxuser",
        "/sys/devices/virtual/bdi/vboxsf-c",
        "/sys/devices/virtual/misc/vboxguest",
        "/sys/devices/virtual/misc/vboxguest/dev",
        "/sys/devices/virtual/misc/vboxguest/power",
        "/sys/devices/virtual/misc/vboxguest/subsystem",
        "/sys/devices/virtual/misc/vboxguest/uevent",
        "/sys/devices/virtual/misc/vboxuser",
        "/sys/devices/virtual/misc/vboxuser/dev",
        "/sys/devices/virtual/misc/vboxuser/power",
        "/sys/devices/virtual/misc/vboxuser/subsystem",
        "/sys/devices/virtual/misc/vboxuser/uevent",
        "/sys/module/vboxguest",
        "/sys/module/vboxguest/coresize",
        "/sys/module/vboxguest/drivers",
        "/sys/module/vboxguest/drivers/pci:vboxguest",
        "/sys/module/vboxguest/holders",
        "/sys/module/vboxguest/holders/vboxsf",
        "/sys/module/vboxguest/initsize",
        "/sys/module/vboxguest/initstate",
        "/sys/module/vboxguest/notes",
        "/sys/module/vboxguest/notes/.note.gnu.build-id",
        "/sys/module/vboxguest/parameters",
        "/sys/module/vboxguest/parameters/log",
        "/sys/module/vboxguest/parameters/log_dest",
        "/sys/module/vboxguest/parameters/log_flags",
        "/sys/module/vboxguest/refcnt",
        "/sys/module/vboxguest/sections",
        "/sys/module/vboxguest/sections/.altinstructions",
        "/sys/module/vboxguest/sections/.altinstr_replacement",
        "/sys/module/vboxguest/sections/.bss",
        "/sys/module/vboxguest/sections/.data",
        "/sys/module/vboxguest/sections/.devinit.data",
        "/sys/module/vboxguest/sections/.exit.text",
        "/sys/module/vboxguest/sections/.fixup",
        "/sys/module/vboxguest/sections/.gnu.linkonce.this_module",
        "/sys/module/vboxguest/sections/.init.text",
        "/sys/module/vboxguest/sections/.note.gnu.build-id",
        "/sys/module/vboxguest/sections/.rodata",
        "/sys/module/vboxguest/sections/.rodata.str1.1",
        "/sys/module/vboxguest/sections/.smp_locks",
        "/sys/module/vboxguest/sections/.strtab",
        "/sys/module/vboxguest/sections/.symtab",
        "/sys/module/vboxguest/sections/.text",
        "/sys/module/vboxguest/sections/__ex_table",
        "/sys/module/vboxguest/sections/__ksymtab",
        "/sys/module/vboxguest/sections/__ksymtab_strings",
        "/sys/module/vboxguest/sections/__param",
        "/sys/module/vboxguest/srcversion",
        "/sys/module/vboxguest/taint",
        "/sys/module/vboxguest/uevent",
        "/sys/module/vboxguest/version",
        "/sys/module/vboxsf",
        "/sys/module/vboxsf/coresize",
        "/sys/module/vboxsf/holders",
        "/sys/module/vboxsf/initsize",
        "/sys/module/vboxsf/initstate",
        "/sys/module/vboxsf/notes",
        "/sys/module/vboxsf/notes/.note.gnu.build-id",
        "/sys/module/vboxsf/refcnt",
        "/sys/module/vboxsf/sections",
        "/sys/module/vboxsf/sections/.bss",
        "/sys/module/vboxsf/sections/.data",
        "/sys/module/vboxsf/sections/.exit.text",
        "/sys/module/vboxsf/sections/.gnu.linkonce.this_module",
        "/sys/module/vboxsf/sections/.init.text",
        "/sys/module/vboxsf/sections/.note.gnu.build-id",
        "/sys/module/vboxsf/sections/.rodata",
        "/sys/module/vboxsf/sections/.rodata.str1.1",
        "/sys/module/vboxsf/sections/.smp_locks",
        "/sys/module/vboxsf/sections/.strtab",
        "/sys/module/vboxsf/sections/.symtab",
        "/sys/module/vboxsf/sections/.text",
        "/sys/module/vboxsf/sections/__bug_table",
        "/sys/module/vboxsf/sections/__param",
        "/sys/module/vboxsf/srcversion",
        "/sys/module/vboxsf/taint",
        "/sys/module/vboxsf/uevent",
        "/sys/module/vboxsf/version",
        "/sys/module/vboxvideo",
        "/sys/module/vboxvideo/coresize",
        "/sys/module/vboxvideo/holders",
        "/sys/module/vboxvideo/initsize",
        "/sys/module/vboxvideo/initstate",
        "/sys/module/vboxvideo/notes",
        "/sys/module/vboxvideo/notes/.note.gnu.build-id",
        "/sys/module/vboxvideo/refcnt",
        "/sys/module/vboxvideo/sections",
        "/sys/module/vboxvideo/sections/.data",
        "/sys/module/vboxvideo/sections/.exit.text",
        "/sys/module/vboxvideo/sections/.gnu.linkonce.this_module",
        "/sys/module/vboxvideo/sections/.init.text",
        "/sys/module/vboxvideo/sections/.note.gnu.build-id",
        "/sys/module/vboxvideo/sections/.rodata.str1.1",
        "/sys/module/vboxvideo/sections/.strtab",
        "/sys/module/vboxvideo/sections/.symtab",
        "/sys/module/vboxvideo/sections/.text",
        "/sys/module/vboxvideo/srcversion",
        "/sys/module/vboxvideo/taint",
        "/sys/module/vboxvideo/uevent",
        "/sys/module/vboxvideo/version",
        "/system/app/bluestacksHome.apk",
        "/system/bin/androVM-prop",
        "/system/bin/androVM-vbox-sf",
        "/system/bin/androVM_setprop",
        "/system/bin/get_androVM_host",
        "/system/bin/mount.vboxsf",
        "/system/etc/init.androVM.sh",
        "/system/etc/init.buildroid.sh",
        "/system/lib/hw/audio.primary.vbox86.so",
        "/system/lib/hw/camera.vbox86.so",
        "/system/lib/hw/gps.vbox86.so",
        "/system/lib/hw/gralloc.vbox86.so",
        "/system/lib/hw/sensors.vbox86.so",
        "/system/lib/modules/3.0.8-android-x86+/extra/vboxguest",
        "/system/lib/modules/3.0.8-android-x86+/extra/vboxguest/vboxguest.ko",
        "/system/lib/modules/3.0.8-android-x86+/extra/vboxsf",
        "/system/lib/modules/3.0.8-android-x86+/extra/vboxsf/vboxsf.ko",
        "/system/lib/vboxguest.ko",
        "/system/lib/vboxsf.ko",
        "/system/lib/vboxvideo.ko",
        "/system/usr/idc/androVM_Virtual_Input.idc",
        "/system/usr/keylayout/androVM_Virtual_Input.kl",
        "/system/xbin/mount.vboxsf",
        "/ueventd.android_x86.rc",
        "/ueventd.vbox86.rc",
        "/ueventd.goldfish.rc",
        "/fstab.vbox86",
        "/init.vbox86.rc",
        "/init.goldfish.rc"
    )
}

