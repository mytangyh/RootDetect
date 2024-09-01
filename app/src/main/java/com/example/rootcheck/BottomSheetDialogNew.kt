package com.example.rootcheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetDialogOne : BottomSheetDialogFragment() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var recyclerView: RecyclerView
    private lateinit var dragHandle: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.custom_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置RecyclerView
        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = BottomSheetDialogAdapter(getData())
        recyclerView.isNestedScrollingEnabled = true

        dragHandle = view.findViewById(R.id.dragHandle)
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

            // 设置初始高度为屏幕高度的 60%
            val initialHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheetBehavior.peekHeight = initialHeight

            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // 状态变化时的回调
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        recyclerView.stopNestedScroll()
                    } else if (newState == BottomSheetBehavior.STATE_SETTLING) {
                        recyclerView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // 滑动时的回调
                }
            })

            // 强制触发一次布局重新测量和调整
            bottomSheet.viewTreeObserver.addOnGlobalLayoutListener {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    val totalHeight = bottomSheet.height
                    val buttonHeight = dialog.findViewById<View>(R.id.buttonFinish).height
                    val availableHeight = totalHeight - buttonHeight
                    recyclerView.layoutParams.height = availableHeight
                }
            }
        }
    }



    private fun getData(): List<Item> {
    val items = mutableListOf<Item>()
    repeat(30) {
        items.add(Item(14, "浙商证券", 300, "提交成功"))
    }
    items[0] = Item(15, "恒生电子", 300, "提交成功") // 修改第一个元素
    return items
}
}



