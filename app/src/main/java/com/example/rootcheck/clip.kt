package com.example.rootcheck

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object clip {
    var num = 1;
    fun clip(context: Context){
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // 检查剪切板中是否有内容
        if (clipboardManager.hasPrimaryClip()) {
            // 获取剪切板内容
            val clip = clipboardManager.primaryClip
            val item = clip?.getItemAt(0)
            val clipboardText = item?.text.toString()
            Toast.makeText(context, clipboardText, Toast.LENGTH_SHORT).show()

        }
    }
    fun testSp(context: Context){
        val sysnKV = SysnKV(context,"test")
        val editorImpl = sysnKV.EditorImpl()
        editorImpl.putString("build","buildStr")
        editorImpl.commit()
    }
    fun testgetSp(context: Context):String{
        val sysnKV = SysnKV(context,"test")
        val toString = sysnKV.getString("build", "").toString()
        if (toString.isEmpty()){
            num++
            val sysnKV = SysnKV(context,"test")
            val editorImpl = sysnKV.EditorImpl()
            editorImpl.putString("build", num.toString())
            editorImpl.commit()
        }
        return sysnKV.getString("build", "").toString()
    }
}