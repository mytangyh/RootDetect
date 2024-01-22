package com.example.rootcheck

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import com.example.lib.LogUtil
import org.json.JSONArray
import java.io.File
import java.util.LinkedList
import java.util.Queue

/**
 * Author : Administrator
 * Time : 2024/01/22
 * Desc :
 */
class SysnKV @JvmOverloads constructor(context: Context, name: String = DEF_NAME) :
    SharedPreferences {
    /**
     * 默认200kb
     *
     *
     * 分块存储文件最大值,超过这个值就加一块
     */
    private val mMaxBlockSize = 1024 * 10
    private val context: Context
    private var name = "def_sysnkv"
    private val mBlockList: ArrayList<Block>
    private var mEditorQueue: Queue<SharedPreferences.Editor>? = null
    private var mHandler: Handler? = null

    init {
        this.name = name
        this.context = context
        mBlockList = ArrayList()
        try {
            var i = 0
            while (true) {
                val path = getBlockFile(context, name, i)
                val blockFile = File(path)
                if (blockFile.exists() && blockFile.isFile) {
                    val block = Block(blockFile)
                    mBlockList.add(block)
                } else {
                    break
                }
                i++
            }
            if (mBlockList.isEmpty()) {
                val path = getBlockFile(context, name, mBlockList.size)
                val block = Block(File(path))
                mBlockList.add(block)
            }
            mEditorQueue = LinkedList()
            val thread = HandlerThread("SysnKV")
            thread.start()
            mHandler = Handler(thread.looper, Work())
        } catch (e: Throwable) {
            //1.文件禁止访问
            //2.无法创建文件
            e.printStackTrace()
        }
    }

    private fun getBlockFile(context: Context, name: String, num: Int): String {
        val dir = context.getExternalFilesDir(null)!!.absolutePath + File.separator + "testSysnP/"
        return dir + name + num.toString() + if (name.indexOf('.') != -1) "" else SUFFIX
    }

    override fun getAll(): Map<String?, *> {
        val mValue: MutableMap<String?, Any?> = HashMap()
        for (block in mBlockList) {
            mValue.putAll(block.getValue())
        }
        return mValue
    }

    override fun getString(key: String, defValue: String?): String? {
        try {
            for (block in mBlockList) {
                val o = block.getValue()[key] as String?
                if (o != null) {
                    return o
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return defValue
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        try {
            for (block in mBlockList) {
                val array = block.getValue()[key]
                //hashmap 存完了json解析出来是jsonarray
                if (array is Set<*>) {
                    return array as Set<String>?
                } else if (array is JSONArray) {
                    if (array == null) {
                        return defValues
                    }
                    val jsonArray = array
                    val strings: MutableSet<String>
                    strings = HashSet()
                    for (i in 0 until jsonArray.length()) {
                        strings.add(jsonArray.opt(i) as String)
                    }
                    return strings
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return defValues
    }

    override fun getInt(key: String, defValue: Int): Int {
        try {
            for (block in mBlockList) {
                val value = block.getValue()[key]
                if (value != null) {
                    return value as Int
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return defValue
    }

    override fun getLong(key: String, defValue: Long): Long {
        try {
            for (block in mBlockList) {
                val value = block.getValue()[key]
                if (value != null) {
                    return if (value is Int) {
                        value.toLong()
                    } else {
                        value as Long
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return defValue
    }

    override fun getFloat(key: String, defValue: Float): Float {
        try {
            for (block in mBlockList) {
                val value = block.getValue()[key]
                if (value != null) {
                    return if (value is Double) {
                        value.toFloat()
                    } else {
                        value as Float
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        try {
            for (block in mBlockList) {
                val value = block.getValue()[key]
                if (value != null) {
                    return value as Boolean
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return defValue
    }

    override fun contains(key: String): Boolean {
        for (block in mBlockList) {
            val o = block.getValue()[key]
            if (o != null) {
                return true
            }
        }
        return false
    }

    override fun edit(): SharedPreferences.Editor {
        return EditorImpl()
    }

    override fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener) {}
    internal class Work : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when (msg.what) {
                WHAT_APPLY -> {
                    var queue: Queue<SharedPreferences.Editor>? = null
                    if (msg.obj is Queue<*>) {
                        queue = msg.obj as? Queue<SharedPreferences.Editor>
                    }
                    queue?.forEach { it?.commit() }
                }

                WHAT_INIT_SYSN -> {}
                else -> {}
            }
            return true
        }

        companion object {
            const val WHAT_APPLY = 1
            const val WHAT_INIT_SYSN = 2
        }
    }

    internal inner class EditorImpl : SharedPreferences.Editor {
        var addMap: MutableMap<String, Any?> = HashMap()
        var deleteKey: MutableSet<String> = HashSet()
        var isClear = false
        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            addMap[key] = value
            return this
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            addMap[key] = values
            return this
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            addMap[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            addMap[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            addMap[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            addMap[key] = value
            return this
        }

        override fun remove(key: String): SharedPreferences.Editor {
            deleteKey.add(key)
            addMap.remove(key)
            return this
        }

        override fun clear(): SharedPreferences.Editor {
            isClear = true
            deleteKey.clear()
            addMap.clear()
            return this
        }

        override fun commit(): Boolean {
            if (Thread.currentThread() === Looper.getMainLooper().thread) {
                //在主线程操作可能会因为等待文件锁anr
                Log.w(TAG, "在主线程操作,最好使用apply防止ANR")
            }
            var result = false
            try {
                for (i in mBlockList.indices) {
                    var isMdf = false
                    val block = mBlockList[i]
                    if (isClear) {
                        block.getValue().clear()
                        isMdf = true
                    } else {
                        for (key in deleteKey) {
                            block.sync()
                            val value: Any? = block.getValue().remove(key)
                            if (value != null) {
                                deleteKey.remove(key)
                                isMdf = true
                            }
                        }
                        if (block.size > mMaxBlockSize) {
                            continue
                        }
                    }
                    if (addMap.isNotEmpty() && block.size < mMaxBlockSize) {
                        block.getValue().putAll(addMap)
                        addMap.clear()
                        isMdf = true
                    }
                    if (isMdf) {
                        result = block.write()
                    }
                }
                if (addMap.isNotEmpty()) {
                    val path = getBlockFile(context, name, mBlockList.size)
                    val block = Block(File(path))
                    mBlockList.add(block)
                    block.getValue().putAll(addMap)
                    result = block.write()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            return result
        }

        override fun apply() {
            mEditorQueue?.add(this)
            Message.obtain(mHandler, Work.WHAT_APPLY, mEditorQueue)
        }
    }

    companion object {
        private const val TAG = "SysnKV"
        private const val DEF_NAME = "sysn_kv"
        private const val SUFFIX = ".skv"
    }
}
