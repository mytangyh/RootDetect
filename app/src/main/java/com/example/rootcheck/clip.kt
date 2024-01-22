package com.example.rootcheck

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object clip {
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
}