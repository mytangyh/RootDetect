package com.example.rootcheck

import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.charset.StandardCharsets

/**
 * Author : Administrator
 * Time : 2024/01/22
 * Desc :
 */
class Block(private val mFile: File) {
    private val value: MutableMap<String?, Any?>

    //版本id
    private var mId: Int? = null
    private var mAccessFile: RandomAccessFile? = null
    private var mChannel: FileChannel? = null

    init {
        if (!mFile.exists() || !mFile.isFile) {
            val dir = mFile.parentFile
            if (!dir.exists()) {
                dir.mkdirs()
            }
            mFile.createNewFile()
        }
        value = HashMap()
    }

    fun getValue(): MutableMap<String?, Any?> {
        sync()
        return value
    }

    val size: Long
        get() = mFile.length()

    fun write(): Boolean {
        return doMap2File()
    }

    fun sync() {
        var buffer: ByteBuffer? = null
        var lock: FileLock? = null
        try {
            //读mid
            lock = lock(0, 4, true)
            buffer = ByteBuffer.allocate(4)
            val size = mChannel!!.read(buffer, 0)
            unLock(lock)
            if (size == 4) {
                buffer.flip()
                //比较mid
                val mid = buffer.getInt()
                //当前mid为空，没同步过，同步，mid不一致，同步
                if (mId == null || mId != mid) {
                    doFile2Map()
                    //同步完成，更新mid
                    mId = mid
                }
            }
        } catch (e: Throwable) {
            //读取mid出io异常
            unLock(lock)
            e.printStackTrace()
        } finally {
            buffer?.clear()
        }
    }

    private fun lock(position: Long, size: Long, shared: Boolean): FileLock? {
        var size = size
        try {
            if (mAccessFile == null || mChannel == null || !mChannel!!.isOpen) {
                mAccessFile = RandomAccessFile(mFile, "rw")
                mChannel = mAccessFile!!.channel
            }
            if (mChannel != null && mChannel!!.isOpen) {
                size = Math.min(size, mAccessFile!!.length())
                return mChannel!!.lock(position, size, shared)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun unLock(lock: FileLock?) {
        var lock = lock
        if (lock != null) {
            try {
                lock.release()
                release()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            lock = null
        }
    }

    private fun release() {
        if (mChannel != null) {
            try {
                mChannel!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mChannel = null
        }
        if (mAccessFile != null) {
            try {
                mAccessFile!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mAccessFile = null
        }
    }

    private fun doFile2Map() {
        val lock = lock(5, Long.MAX_VALUE, true)
        try {
            //前4位是mid,跳过
            mChannel!!.position(4)
            val buffer = ByteBuffer.allocate((mChannel!!.size() - 4).toInt())
            val len = mChannel!!.read(buffer)
            if (len == -1) {
                return
            }
            buffer.flip()
            value.clear()
            val `object` = JSONObject(StandardCharsets.UTF_8.decode(buffer).toString())
            val it = `object`.keys()
            while (it.hasNext()) {
                val k = it.next()
                value[k] = `object`[k]
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            unLock(lock)
            try {
                mFile.delete()
            } catch (e1: Exception) {
                e1.printStackTrace()
            }
            e.printStackTrace()
        } finally {
            unLock(lock)
        }
    }

    private fun doMap2File(): Boolean {
        var result = false
        val lock = lock(0, Long.MAX_VALUE, false)
        try {
            val `object` = JSONObject(value)
            val bt = `object`.toString(0).toByteArray(StandardCharsets.UTF_8)
            val buf = ByteBuffer.allocate(bt.size + 4)
            mId = if (mId == null) {
                Int.MIN_VALUE
            } else {
                (mId!! + 1) % (Int.MAX_VALUE - 10)
            }
            buf.putInt(mId!!)
            buf.put(bt)
            buf.flip()
            mChannel!!.position(0)
            while (buf.hasRemaining()) {
                mChannel!!.write(buf)
            }
            mChannel!!.truncate((4 + bt.size).toLong())
            mChannel!!.force(true)
            result = true
        } catch (e: IOException) {
            //todo 写入文件失败,用备份文件方式处理
            e.printStackTrace()
        } catch (e: JSONException) {
            //map转json串会出异常?先不处理,最多就是数据存不进去
            //可能map存储了含有特殊字符串的value会有这个异常.
            e.printStackTrace()
        } finally {
            unLock(lock)
        }
        return result
    }
}
