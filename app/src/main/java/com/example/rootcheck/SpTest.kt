package com.example.rootcheck

import com.example.lib.LogUtil

object SpTest {

    private var num : String? = null

    private var n =0
    private var i = 1
    private var sharedPreferencesLocker: SharedPreferencesLocker? = null
    init{
        LogUtil.d("init")
        readLocal()
        sharedPreferencesLocker = SharedPreferencesLocker(MyApplication.applicationContext())
        if (num.isNullOrEmpty()){
            readSystem()
        }
    }
    fun readLocal(){

        num = SPUtils.getString(MyApplication.applicationContext(),"test")
    }
    fun readSystem(){
        SPUtils.saveString(MyApplication.applicationContext(),"testP", n.toString())

    }
    fun random():String?{
        i++
        num =i.toString()
        SPUtils.saveString(MyApplication.applicationContext(),"test", num.toString())
        return num
    }
    fun testSp(): String? {
//        if (!num.isNullOrEmpty()){
//            return num
//        }
//        return random()
        return testLockSp()
    }
    fun testGetSp(): String? {
        val string = SPUtils.getString(MyApplication.applicationContext(), "testP")
        if (n.toString() == string){
            n++
        }
        SPUtils.saveString(MyApplication.applicationContext(),"testP", n.toString())
        return string
    }
    fun testLockSp(): String? {
        val string = sharedPreferencesLocker?.readFromSharedPreferences("testP")
        if (n.toString() == string){
            n++
        }
        sharedPreferencesLocker?.writeToSharedPreferences("testP", n.toString())
        return string
    }
}

