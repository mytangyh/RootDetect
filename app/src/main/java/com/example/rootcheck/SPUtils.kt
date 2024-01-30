package com.example.rootcheck

import android.content.Context


class SPUtils{

    companion object{

        fun saveString(context: Context,spName:String,key: String, value: String) {

            val editor = MyApplication.applicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE).edit()
            editor.putString(key, value)
            editor.commit()
        }

        fun saveString(context:Context,key: String, value: String){
            saveString(context,"sptest",key,value)
        }
        fun getString(context: Context, spName: String, key: String, defaultValue: String?): String? {
            val sp = MyApplication.applicationContext().getSharedPreferences(spName, Context.MODE_PRIVATE)
            return sp.getString(key, defaultValue)
        }
        fun getString(context: Context,key: String):String?{
            return getString(context,"sptest",key,null)
        }

    }


    // 同样的方式，你可以添加更多的方法来保存和获取其他类型的数据，例如Boolean，Float，Long等。
}