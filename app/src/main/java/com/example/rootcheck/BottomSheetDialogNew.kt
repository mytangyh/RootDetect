package com.example.rootcheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custom_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = MyAdapter(getData())

        // 设置进度条
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        progressBar.progress = 42 // 设置实际进度

        // 底部按钮
        val buttonFinish = view.findViewById<Button>(R.id.buttonFinish)
        buttonFinish.setOnClickListener {
            dismiss()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.peekHeight = (resources.displayMetrics.heightPixels * 0.5).toInt() // 半屏高度
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            bottomSheet.findViewById<View>(R.id.dragHandle).setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                false
            }
        }
    }

    private fun getData(): List<Item> {
        // 返回数据列表
        return listOf(
            Item(15, "恒生电子", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            Item(14, "浙商证券", 300, "提交成功"),
            // ... 添加更多数据项
        )
    }
}

data class Item(val id: Int, val name: String, val quantity: Int, val status: String)

class MyAdapter(private val items: List<Item>) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val id: TextView = view.findViewById(R.id.item_id)
        val name: TextView = view.findViewById(R.id.item_name)
        val quantity: TextView = view.findViewById(R.id.item_quantity)
        val status: TextView = view.findViewById(R.id.item_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stock, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.id.text = item.id.toString()
        holder.name.text = item.name
        holder.quantity.text = item.quantity.toString()
        holder.status.text = item.status
    }

    override fun getItemCount() = items.size
}
