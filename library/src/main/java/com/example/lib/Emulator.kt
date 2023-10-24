package com.example.lib

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import java.io.*
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * 检测模拟器
 *
 *
 */
class Emulator {
    private val bsAppName = arrayOf(
        FileUtil.getSdCardPath("Android/data/com.bluestacks.home"),
        FileUtil.getSdCardPath("Android") + "/data/com.bluestacks.settings",
        FileUtil.getSdCardPath("windows/BstSharedFolder"),
        "/data/app-lib/com.bluestacks.settings"
    )
    private val bsAppPackage = arrayOf("com.bluestacks.appmart", "com.bluestacks.settings")

    @SuppressLint("SdCardPath")
    private val mmAppName = arrayOf(
        "/data/data/com.mumu.launcher",
        "/data/data/com.mumu.store",
        "/data/data/com.netease.mumu.cloner",
        "/data/dalvik-cache/profiles/com.mumu.launcher",
        "/data/dalvik-cache/profiles/com.mumu.store"
    )
    private val mmAppPackage = arrayOf("com.mumu.store", "com.mumu.launcher", "com.mumu.audio")

    @SuppressLint("SdCardPath")
    private val xyAppName = arrayOf(
        "/data/data/com.microvirt.launcher",
        "/data/data/com.microvirt.download", "/data/data/com.microvirt.guide",
        "/data/data/com.microvirt.installer", "/data/data/com.microvirt" +
                ".market", "/data/data/com.microvirt.memuime"
    )
    private val xyAppPackage = arrayOf(
        "com.microvirt.launcher", "com.microvirt.download", "com" +
                ".microvirt.guide", "com.microvirt.installer", "com.microvirt.market", "com.microvirt"
                + ".memuime"
    )

    @SuppressLint("SdCardPath")
    private val ttAppName = arrayOf(
        "/data/data/com.tiantian.ime",
        "/system/priv-app" +
                "/TianTianLauncher/TianTianLauncher.apk",
        "/init.ttVM_x86.rc",
        "/ueventd.ttVM_x86.rc",
        "/fstab.ttVM_x86",
        "/system/bin/ttVM-prop"
    )
    private val ttAppPackage = arrayOf("com.tiantian.ime")

    @SuppressLint("SdCardPath")
    private val ldAppName = arrayOf(
        "/sdcard/ldAppStore", "/system/priv-app/ldAppStore/ldAppStore.apk", "/system/app/ldAppStore/ldAppStore.apk",
        "/lib/libldutils.so", "/lib/hw/gps.ld.so"
    )
    private val ldAppPackage = arrayOf("com.android.flysilkworm", "com.yqw.llfz.ld")

    @SuppressLint("SdCardPath")
    private val ysAppName = arrayOf(
        "/storage/emulated/legacy/BigNoxHD", "/lib/libnoxd.so",
        "/lib/libnoxspeedup.so", "/data/property/persist.nox.androidid", "system/app" +
                "/Helper/NoxHelp_zh.apk", "/data/dalvik-cache/profiles/com.bignox.app" +
                ".store.hd"
    )
    private val ysAppPackage = arrayOf("com.bignox.google.installer", "com.bignox.app.store.hd")

    @SuppressLint("SdCardPath")
    private val geAppName = arrayOf(
        "/data/data/com.google.android.launcher.layouts.genymotion",
        "/system/app/GenymotionLayout/GenymotionLayout.apk",
        "/dev/socket/baseband_genyd",
        "system/bin/genymotion-vbox-sf"
    )
    private val geAppPackage = arrayOf(
        "com.genymotion.superuser", "com.genymotion.genyd", "com" +
                ".genymotion.systempatcher"
    )

    @SuppressLint("SdCardPath")
    private val itoolsAppName = arrayOf(
        "/data/data/cn.itools.vm.launcher",
        "/data/data/cn.itools.avdmarket", "/data/data/cn.itools.vm.proxy"
    )
    private val itoolsAppPackage = arrayOf(
        "cn.itools.vm.launcher", "Emulator",
        "cn.itools.vm.proxy"
    )
    private val w1AppName = arrayOf(
        "/mnt/prebundledapps/downloads/51service.apk",
        FileUtil.getSdCardPath("") + ".51service"
    )
    private val hmwAppName = arrayOf(
        "/system/bin/droid4x", "/system/bin/droid4x-prop",
        "/system/lib/libdroid4x.so", "/system/etc/init.droid4x.sh"
    )
    private val hmwAppPackage = arrayOf(
        "com.haimawan.push", "com.haima.helpcenter", "me.haima" +
                ".androidassist"
    )
    private val asAppName = arrayOf("/sys/module/goldfish_audio", "/sys/module/goldfish_sync")
    private fun isMark(appName: Array<String>): String {
        val builder = StringBuilder()
        for (s in appName) {
            if (FileUtil.checkPath(s) == "1") {
                builder.append("1")
            } else {
                builder.append("0")
            }
        }
        return builder.toString()
    }

    fun distinguishVM(context: Context, MAX_INDEX: Int): EmulatorInfo {
        val xiaoYaoTag = isXiaoYao(context)
        val noxTag = isNox(context)
        val blueStacksTag = isBlueStacks(context)
        val fiveOneTag = is51()
        val muMuTag = isMuMu(context)
        val genymotionTag = isGenymotion(context)
        val iToolsTag = isITools(context)
        val tianTianTag = isTianTian(context)
        val droid4xTag = isDroid4x(context)
        val leiDianTag = isLeiDian(context)
        val asTag = isAS(context)
        val otherTag = isOther(context)
        return if (StringUtil.getNumber(xiaoYaoTag) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_XIAOYAO, xiaoYaoTag, "", "")
        } else if (StringUtil.getNumber(noxTag) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_NOX, noxTag, "", "")
        } else if (StringUtil.getNumber(blueStacksTag) >= MAX_INDEX) {
            if (StringUtil.getNumber(fiveOneTag) > 0) {
                EmulatorInfo(true, STRING_TYPE_51, fiveOneTag, "", "")
            } else {
                EmulatorInfo(true, STRING_TYPE_BLUESTACKS, blueStacksTag, "", "")
            }
        } else if (StringUtil.getNumber(isMuMu(context)) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_MUMU, muMuTag, "", "")
        } else if (StringUtil.getNumber(genymotionTag) >= MAX_INDEX) {
            if (StringUtil.getNumber(iToolsTag) > 0) {
                EmulatorInfo(true, STRING_TYPE_ITOOLS, iToolsTag, "", "")
            } else {
                EmulatorInfo(true, STRING_TYPE_GENY, genymotionTag, "", "")
            }
        } else if (StringUtil.getNumber(tianTianTag) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_TIANTIAN, tianTianTag, "", "")
        } else if (StringUtil.getNumber(droid4xTag) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_Drod4X, droid4xTag, "", "")
        } else if (StringUtil.getNumber(leiDianTag) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_LEIDIAN, leiDianTag, "", "")
        } else if (StringUtil.getNumber(asTag) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_AS, asTag, "", "")
        } else if (StringUtil.getNumber(otherTag) >= MAX_INDEX) {
            EmulatorInfo(true, STRING_TYPE_UNKNOWN, "", otherTag, "")
        } else {
            val tag =
                xiaoYaoTag + noxTag + blueStacksTag + fiveOneTag + muMuTag + genymotionTag + iToolsTag + tianTianTag + droid4xTag + leiDianTag + asTag + otherTag
            if (StringUtil.getNumber(tag) >= MAX_INDEX) {
                return EmulatorInfo(true, STRING_TYPE_UNKNOWN, "", tag, "")
            } else {
                if (tag.contains("1")) {
                    return EmulatorInfo(
                        false,
                        STRING_TYPE_UNKNOWN,
                        "",
                        tag,
                        "当前阈值高于检测的点,疑似模拟器"
                    )
                }
            }
            EmulatorInfo(false, "", "", "", "")
        }
    }

    /**
     * 检测逍遥模拟器
     * 模拟器版本6.1.0
     * 安卓版本5.1.1
     */
    private fun isXiaoYao(context: Context): String {
        return isMark(xyAppName) + "-" + CommonUtils.isAppPackage(context, xyAppPackage)
    }

    /**
     * 检测夜神模拟器
     * 模拟器版本 6.2.8.0003
     * 安卓版本5.1.1
     *
     *
     *
     *
     * 可能误判为52新星模拟器（待改进）都是Nox团队开发，都属于夜神范围
     *
     * @param context
     */
    private fun isNox(context: Context): String {
        return isMark(ysAppName) + "-" + CommonUtils.isAppPackage(context, ysAppPackage)
    }

    /**
     * 检测蓝叠模拟器
     * 旧版本:
     * 平台版本号 3.1.20.643
     * 引擎版本号 2.60.88.3420
     * 安卓版本  4.4.2
     *
     *
     * 新版本:
     * 平台版本号 3.1.20.643
     * 引擎版本号 4.50.6.2003
     * 安卓版本  7.1.0
     *
     *
     *
     *
     * 可能误判为51模拟器（修复）
     *
     * @param context
     */
    private fun isBlueStacks(context: Context): String {
        var value = isMark(bsAppName) + "-" + CommonUtils.isBluePackageName(context, bsAppPackage)
        //通过读取以下两个文件夹来进行apk匹配，com.bluestacks.BstCommandProcessor.apk，com.bluestacks.settings.apk
        //com.bluestacks.bstfolder.apk
        val file = FileUtil.getFile("/system/priv-app/")
        value = if (file.contains("com.bluestacks.settings.apk")) {
            "$value-1"
        } else {
            "$value-0"
        }
        //com.bluestacks.home.apk，com.bluestacks.searchapp，com.bluestacks.setup.apk，com
        // .bluestacks.appfinder.apk
        //com.bluestacks.keymappingtool.apk，suppressed.dbWindowsFileManager.apk，com.bluestacks
        // .appmart.apk
        val file1 = FileUtil.getFile("/data/downloads/")
        value = if (file1.contains("com.bluestacks.home.apk")) {
            "$value-1"
        } else {
            "$value-0"
        }
        return value
    }

    /**
     * 模拟器版本  3.1.3.9
     * 烈风版
     * 安卓版本    4.4.2
     *
     * @return
     */
    private fun is51(): String {
        return isMark(w1AppName)
    }

    /**
     * 检测网易MuMu模拟器
     * 模拟器版本 2.1.7
     * 桌面启动器版本 2.3.1
     * 安卓版本6.0.1
     */
    private fun isMuMu(context: Context): String {
        var value = isMark(mmAppName) + "-" + CommonUtils.isAppPackage(context, mmAppPackage)
        val model = Build.MODEL
        value = if (model == "MuMu") {
            "$value-1"
        } else {
            "$value-0"
        }
        return value
    }

    /**
     * 检测Genymotion模拟器
     * 模拟器版本 3.0.1
     * 安卓版本5.0、9.0
     *
     *
     * 误判为畅玩模拟器或者iTools模拟器（需要进一步判断）
     */
    private fun isGenymotion(context: Context): String {
        var value = isMark(geAppName) + "-" + CommonUtils.isSystemApp(context, geAppPackage)
        val model = Build.MODEL
        value = if (model == "Genymotion") {
            "$value-1"
        } else {
            "$value-0"
        }
        val user = Build.USER
        value = if (user == "genymotion") {
            "$value-1"
        } else {
            "$value-0"
        }
        val sensor = getSensorName(context)
        value = if (sensor.contains("Genymotion")) {
            "$value-1"
        } else {
            "$value-0"
        }
        return value
    }

    /**
     * iTools模拟器和畅玩模拟器一样的
     * iTools模拟器版本:2.0.8.9
     * 安卓版本:4.4.4
     */
    private fun isITools(context: Context): String {
        return isMark(itoolsAppName) + "-" + CommonUtils.isSystemApp(
            context,
            itoolsAppPackage
        )
    }

    /**
     * 检测天天模拟器
     * 模拟器版本 3.2.5
     * 安卓版本6.0.1
     *
     * @param context
     */
    private fun isTianTian(context: Context): String {
        var value = isMark(ttAppName) + "-" + CommonUtils.isAppPackage(context, ttAppPackage)
        val sensor = getSensorName(context)
        value = if (sensor.contains("TiantianVM")) {
            "$value-1"
        } else {
            "$value-0"
        }
        value = if (Build.HARDWARE == "ttVM_x86") {
            "$value-1"
        } else {
            "$value-0"
        }
        return value
    }

    /**
     * 检测海马玩模拟器（Droid4x）
     * 模拟器版本 0.10.6.Beta
     * 安卓版本 4.2.2
     */
    private fun isDroid4x(context: Context): String {
        val value = isMark(hmwAppName) + "-" + CommonUtils.isAppPackage(context, hmwAppPackage)
        Log.d("ggg", "海马玩:$value")
        return value
    }

    /**
     * 检测雷电模拟器
     * 模拟器版本 3.42
     * 安卓版本 5.1.1
     */
    private fun isLeiDian(context: Context): String {
        //return value;
        return isMark(ldAppName) + "-" + CommonUtils.isSystemApp(context, ldAppPackage)
    }

    /**
     * 检测原生模拟器（AS自带的）
     * 针对QEMU, KVM, QEMU-KVM 和 Goldfish的理解
     * https://blog.csdn.net/span76/article/details/19165345
     */
    private fun isAS(context: Context): String {
        var value = isMark(asAppName)
        value = if (getSensorName(context).startsWith("Goldfish")) {
            "$value-1"
        } else {
            "$value-0"
        }
        value = if (Build.HARDWARE == "ranchu") {
            "$value-1"
        } else {
            "$value-0"
        }
        return value
    }

    /**
     * 检测不属于以上名字的模拟器
     */
    private fun isOther(context: Context): String {
        val value =
            checkcpu() + "-" + buildInfo + "-" + isLight(context) + "-" + existQemu() + hasQEmuProps() + existQemuDrivers() + "-" + vBox()
        LogUtil.d(value)
        return value
    }

    /**
     * 获取所有传感器名字
     *
     * @param context
     * @return
     */
    private fun getSensorName(context: Context): String {
        val sb = StringBuilder()
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getSensorList(Sensor.TYPE_ALL)
        for (sensor1 in sensor) {
            sb.append(sensor1.name)
        }
        return sb.toString()
    }
    //其他检测模拟器方案
    /**
     * 判断cpu是否为电脑来判断 模拟器
     * 注意安卓平板可能为intel
     *
     * @return true 为模拟器
     */
    private fun checkcpu(): String {
        var cpuinfo = ""
        var result = ""
        try {
            val args = arrayOf("/system/bin/cat", "/proc/cpuinfo")
            val cmd = ProcessBuilder(*args)
            val process = cmd.start()
            val sb = StringBuffer()
            var readLine: String? = ""
            val responseReader = BufferedReader(InputStreamReader(process.inputStream, "utf-8"))
            while (responseReader.readLine().also { readLine = it } != null) {
                sb.append(readLine)
            }
            responseReader.close()
            cpuinfo = sb.toString().lowercase(Locale.getDefault())
        } catch (ex: IOException) {
        }
        result = if (cpuinfo.contains("intel")) {
            result + "1"
        } else {
            result + "0"
        }
        result = if (result.contains("amd")) {
            result + "1"
        } else {
            result + "0"
        }
        return result
    }

    private val buildInfo: String
        /**
         * 根据部分特征参数设备信息来判断是否为模拟器
         *
         * @return true 为模拟器
         */
        private get() {
            var result = ""

            //检测cpu架构
            result = if (Build.CPU_ABI.contains("x86") || Build.CPU_ABI2.contains("x86")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.FINGERPRINT.startsWith("generic")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.FINGERPRINT.startsWith("generic_x86")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.FINGERPRINT.lowercase(Locale.getDefault())
                    .contains("test-keys") || Build.FINGERPRINT.lowercase(Locale.getDefault()).contains("dev-keys")
            ) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.MODEL.contains("Emulator")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.MODEL.contains("google_sdk")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.MODEL.contains("Android SDK built for x86")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.MODEL.contains("Android SDK built for x86_64")) {
                result + "1"
            } else {
                result + "0"
            }


            //厂商
            result = if (Build.MANUFACTURER.contains("Genymotion")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.MANUFACTURER.contains("unknown")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.MANUFACTURER.contains("Google")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.BRAND.startsWith("generic") || Build.BRAND.startsWith("generic_x86")) {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.HARDWARE == "goldfish") {
                result + "1"
            } else {
                result + "0"
            }
            result = if (Build.DEVICE == "vbox86p") {
                result + "1"
            } else {
                result + "0"
            }
            result =
                if (Build.DEVICE.startsWith("generic") || Build.DEVICE.startsWith("generic_x86") || Build.DEVICE.startsWith(
                        "generic_x86_64"
                    )
                ) {
                    result + "1"
                } else {
                    result + "0"
                }


            //product
            result = if ("google_sdk" == Build.PRODUCT) {
                result + "1"
            } else {
                result + "0"
            }
            result = if ("sdk" == Build.PRODUCT) {
                result + "1"
            } else {
                result + "0"
            }
            result = if ("sdk_google" == Build.PRODUCT) {
                result + "1"
            } else {
                result + "0"
            }
            result = if ("sdk_x86" == Build.PRODUCT) {
                result + "1"
            } else {
                result + "0"
            }
            result = if ("vbox86p" == Build.PRODUCT) {
                result + "1"
            } else {
                result + "0"
            }
            result = if ("sdk_google_phone_x86" == Build.PRODUCT) {
                result + "1"
            } else {
                result + "0"
            }
            return result
        }

    /**
     * 光传感器检测
     *
     * @param context
     * @return
     */
    private fun isLight(context: Context): String {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor8 = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        return if (null == sensor8) {
            "1"
        } else {
            "0"
        }
    }

    //检测ro.kernel.qemu是否为1，内核qemu
    private fun hasQEmuProps(): String {
        val property_value = CommandUtils.getInstance().getProperty("ro.kernel.qemu")
        return if (property_value == "1") {
            "1"
        } else {
            "0"
        }
    }

    /**
     * 基于qumu的模拟器特定属性
     */
    private val known_files = arrayOf(
        "/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace",
        "/system/bin/qemu.props", "/system/bin/qemud"
    )

    private fun existQemu(): String {
        var result = ""
        for (i in known_files.indices) {
            val file_name = known_files[i]
            val qemu_file = File(file_name)
            result = if (qemu_file.exists()) {
                result + "1"
            } else {
                result + "0"
            }
        }
        return result
    }

    /**
     * 读取驱动文件, 检查是否包含已知的qemu驱动
     *
     * @return `true` if any known drivers where found to exist or `false` if not.
     */
    private fun existQemuDrivers(): String {
        var result = ""
        for (drivers_file in arrayOf<File>(
            File("/proc/tty/drivers"), File(
                "/proc" +
                        "/cpuinfo"
            )
        )) {
            if (drivers_file.exists() && drivers_file.canRead()) {
                // We don't care to read much past things since info we care about should be
                // inside here
                val data = ByteArray(1024)
                try {
                    val `is`: InputStream = FileInputStream(drivers_file)
                    `is`.read(data)
                    `is`.close()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                val driver_data = String(data)

                /**
                 * 基于qemu的驱动文件判断
                 */
                val known_qemu_drivers = "goldfish"
                result = if (driver_data.contains(known_qemu_drivers)) {
                    result + "1"
                } else {
                    result + "0"
                }
            }
        }
        return result
    }



    private fun vBox(): String {
        return isMark(vBoxFile)
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

    companion object {
        private var emulator: Emulator? = null
        val instance: Emulator?
            get() {
                if (emulator == null) {
                    emulator = Emulator()
                }
                return emulator
            }
        private const val STRING_TYPE_BLUESTACKS = "蓝叠模拟器"

        //MuMu模拟器
        private const val STRING_TYPE_MUMU = "MUMU模拟器"

        //逍遥模拟器
        private const val STRING_TYPE_XIAOYAO = "逍遥模拟器"

        //天天模拟器
        private const val STRING_TYPE_TIANTIAN = "天天模拟器"

        //雷电模拟器
        private const val STRING_TYPE_LEIDIAN = "雷电模拟器"

        //夜神模拟器(/system/app/NoxHelp_zh.apk 该路径会卡死)
        private const val STRING_TYPE_NOX = "夜神模拟器"

        //Genymontion模拟器
        private const val STRING_TYPE_GENY = "Genymontion模拟器"

        //itools模拟器(附属于genymontion)
        private const val STRING_TYPE_ITOOLS = "itools模拟器"

        //51模拟器
        private const val STRING_TYPE_51 = "51模拟器"

        //海马玩模拟器
        private const val STRING_TYPE_Drod4X = "海马玩模拟器"

        //原生模拟器
        private const val STRING_TYPE_AS = "AndroidStudio原生模拟器"

        //未知（未能识别的模拟器）
        private const val STRING_TYPE_UNKNOWN = "unknown"
    }
}
