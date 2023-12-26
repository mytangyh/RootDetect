package com.example.rootcheck

import com.tencent.mmkv.MMKV

object MMKVUtils {
    private val mmkv: MMKV = MMKV.mmkvWithID("mmkv",MMKV.MULTI_PROCESS_MODE)

    /**
     * 存储数据
     */
    fun <T> put(key: String, value: T) {
        try {
            when (value) {
                is String -> mmkv.encode(key, value)
                is Float -> mmkv.encode(key, value)
                is Boolean -> mmkv.encode(key, value)
                is Int -> mmkv.encode(key, value)
                is Long -> mmkv.encode(key, value)
                is Double -> mmkv.encode(key, value)
                else -> throw IllegalArgumentException("This type can't be saved into MMKV")
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to put data into MMKV", e)
        }
    }

    fun saveString(key: String,value:String){
        mmkv.encode(key, value)
    }
    fun getString(key: String,defaultValue:String):String{
        return mmkv.decodeString(key, defaultValue)?:defaultValue
    }
    fun getString(key: String):String{
        return getString(key,"")
    }

    /**
     * 获取数据
     */
    fun <T> get(key: String, defaultValue: T? = null): T? {
        return when (defaultValue) {
            is String -> mmkv.decodeString(key, defaultValue)
            is Float -> mmkv.decodeFloat(key, defaultValue)
            is Boolean -> mmkv.decodeBool(key, defaultValue)
            is Int -> mmkv.decodeInt(key, defaultValue)
            is Long -> mmkv.decodeLong(key, defaultValue)
            is Double -> mmkv.decodeDouble(key, defaultValue)
            null -> null
            else -> throw IllegalArgumentException("This type can be gotten from MMKV")
        } as T?
    }

    /**
     * 删除数据
     */
    fun remove(key: String) {
        try {
            mmkv.removeValueForKey(key)
        } catch (e: Exception) {
            throw RuntimeException("Failed to remove data from MMKV", e)
        }
    }

    /**
     * 清空数据
     */
    fun clear() {
        try {
            mmkv.clearAll()
        } catch (e: Exception) {
            throw RuntimeException("Failed to clear data in MMKV", e)
        }
    }
}
