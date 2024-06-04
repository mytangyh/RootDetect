package com.example.rootcheck.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rootcheck.R

class CustomAdapter(private val context: Context, private val items: List<Item>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(context).inflate(R.layout.list_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.item_stock, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            // 处理表头的绑定
        } else if (holder is ItemViewHolder) {
            val item = items[position - 1] // 因为第一个位置是表头
            holder.name.text = item.name
            holder.quantity.text = item.quantity.toString()
            holder.status.text = item.status

            if (item.status == "提交失败") {
                holder.status.setTextColor(Color.RED)
            } else {
                holder.status.setTextColor(Color.GREEN)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size + 1 // 加1是因为表头
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 表头的视图持有者
    }

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        val quantity: TextView = view.findViewById(R.id.item_quantity)
        val status: TextView = view.findViewById(R.id.item_status)
    }
}

data class Item(val name: String, val quantity: Int, val status: String)
