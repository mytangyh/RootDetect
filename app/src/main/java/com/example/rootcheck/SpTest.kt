package com.example.rootcheck

import com.example.lib.LogUtil

object SpTest {

    private var num : String? = null

    private var i = 1

    init{
        LogUtil.d("init")
        readLocal()
        if (num.isNullOrEmpty()){
            readSystem()
        }
    }
    fun readLocal(){

        num = SPUtils.getString(MyApplication.applicationContext(),"test")
    }
    fun readSystem(){
    }
    fun random():String?{
        i++
        num =i.toString()
        SPUtils.saveString(MyApplication.applicationContext(),"test", num.toString())
        return num
    }
    fun testSp(): String? {
        if (!num.isNullOrEmpty()){
            return num
        }
        return random()
    }
}

