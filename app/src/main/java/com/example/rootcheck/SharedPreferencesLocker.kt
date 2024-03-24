package com.example.rootcheck

import android.content.Context
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

class SharedPreferencesLocker(private val context: Context) {

    private val lockFilePath: String = context.filesDir.absolutePath + "/shared_prefs_lock.lock"
    private val lockFile: File = File(lockFilePath)

    fun <T> executeWithLock(prefName: String, action: () -> T): T? {
        try {
            // 创建锁文件
            if (!lockFile.exists()) {
                lockFile.createNewFile()
            }

            // 获取文件通道
            val fileChannel = RandomAccessFile(lockFile, "rw").channel

            // 尝试获取文件锁
            val fileLock: FileLock? = fileChannel.tryLock()

            if (fileLock != null) {
                // 锁定成功，执行需要同步的代码
                val result = action.invoke()

                // 释放文件锁
                fileLock.release()

                // 关闭文件通道
                fileChannel.close()

                return result
            } else {
                // 锁定失败，可能文件已被其他进程锁定
                return null
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // 示例：在锁定的情况下写入 SharedPreferences
    fun writeToSharedPreferences(prefName: String, key: String, value: String) {
        executeWithLock(prefName) {
            val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(key, value)
            editor.apply()
        }
    }
    fun writeToSharedPreferences(key: String,value: String){
        writeToSharedPreferences("lock",key, value)
    }

    // 示例：在锁定的情况下读取 SharedPreferences
    fun readFromSharedPreferences(prefName: String, key: String): String? {
        return executeWithLock(prefName) {
            val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            sharedPreferences.getString(key, null)
        }
    }
    fun readFromSharedPreferences(key: String):String?{
        return readFromSharedPreferences("lock",key)
    }
}
