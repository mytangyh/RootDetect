package com.example.lib

import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Field


class HookDetector {

    companion object {
        fun isDetected(): Boolean {
            return Native.checkFrida()
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
            var flag = false
            if (tasks != null) {
                for (task in tasks) {
                    try {
                        val reader = BufferedReader(FileReader(task.absolutePath + "/status"))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            Log.d("TAG", "$line")
                            if (line?.contains("gmain") == true || line?.contains("pool-frida") == true || line?.contains(
                                    "gdbus"
                                ) == true || (line?.contains("gum-js-loop") == true)||(line?.contains("linjector ") == true)
                            ) {
                                Log.e("TAG", "find: $line")
                                flag = true
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return flag
        }

        private const val XPOSED_HELPERS = "de.robv.android.xposed.XposedHelpers"
        private const val XPOSED_BRIDGE = "de.robv.android.xposed.XposedBridge"

        //手动抛出异常，检查堆栈信息是否有xp框架包
        fun isEposedExistByThrow(): Boolean {
            return try {
                throw java.lang.Exception("gg")
            } catch (e: java.lang.Exception) {
                for (stackTraceElement in e.stackTrace) {
                    if (stackTraceElement.className.contains(XPOSED_BRIDGE)) return true
                }
                false
            }
        }

        //检查xposed包是否存在
        fun isXposedExists(): Boolean {
            try {
                val xpHelperObj = ClassLoader
                    .getSystemClassLoader()
                    .loadClass(XPOSED_HELPERS)
                    .newInstance()
            } catch (e: InstantiationException) {
                e.printStackTrace()
                return true
            } catch (e: IllegalAccessException) {
                //实测debug跑到这里报异常
                e.printStackTrace()
                return true
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return false
            }
            try {
                val xpBridgeObj = ClassLoader
                    .getSystemClassLoader()
                    .loadClass(XPOSED_BRIDGE)
                    .newInstance()
            } catch (e: InstantiationException) {
                e.printStackTrace()
                return true
            } catch (e: IllegalAccessException) {
                //实测debug跑到这里报异常
                e.printStackTrace()
                return true
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return false
            }
            return true
        }

        //尝试关闭xp的全局开关，亲测可用
        fun tryShutdownXposed(): Boolean {
            return if (isEposedExistByThrow()) {
                var xpdisabledHooks: Field? = null
                try {
                    xpdisabledHooks = ClassLoader.getSystemClassLoader()
                        .loadClass(XPOSED_BRIDGE)
                        .getDeclaredField("disableHooks")
                    xpdisabledHooks.setAccessible(true)
                    xpdisabledHooks.set(null, java.lang.Boolean.TRUE)
                    true
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                    false
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    false
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                    false
                }
            } else true
        }

        fun classCheck(): Boolean {
            try {
                Class.forName("de.robv.android.xposed.XC_MethodHook")
                return true
            } catch (e: java.lang.Exception) {
            }
            try {
                Class.forName("de.robv.android.xposed.XposedHelpers")
                return true
            } catch (e: java.lang.Exception) {
            }
            return false
        }

        fun exceptionCheck(): Boolean {
            try {
                throw java.lang.Exception("Deteck hook")
            } catch (e: java.lang.Exception) {
                var zygoteInitCallCount = 0
                for (item in e.stackTrace) {
                    // 检测"com.android.internal.os.ZygoteInit"是否出现两次，如果出现两次，则表明Substrate框架已经安装
                    if ("com.android.internal.os.ZygoteInit" == item.className) {
                        zygoteInitCallCount++
                        if (zygoteInitCallCount == 2) {
                            return true
                        }
                    }
                    if ("com.saurik.substrate.MS$2" == item.className && "invoke" == item.methodName) {
                        return true
                    }
                    if ("de.robv.android.xposed.XposedBridge" == item.className && "main" == item.methodName) {
                        return true
                    }
                    if ("de.robv.android.xposed.XposedBridge" == item.className && "handleHookedMethod" == item.methodName) {
                        return true
                    }
                }
            }
            return false
        }
    }


}