package com.example.rootcheck

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.view.ViewCompat
import androidx.core.view.setPadding
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

    fun getStatusBarHeight(): Int {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val height= (resources.displayMetrics.heightPixels * 0.6).toInt()
            val remainHeight= (resources.displayMetrics.heightPixels)- getStatusBarHeight()-height+getStatusBarHeight()
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_COLLAPSED
//                isHideable = true
//                isDraggable = true
                peekHeight = height
            }

            // 设置初始高度为屏幕高度的 60%
//            val initialHeight = (resources.displayMetrics.heightPixels * 0.6).toInt()
//            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//            bottomSheetBehavior.peekHeight = initialHeight

            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    // 状态变化时的回调
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                        recyclerView.stopNestedScroll()
                        bottomSheet.setPadding(bottomSheet.paddingLeft, bottomSheet.paddingTop, bottomSheet.paddingRight, remainHeight)
                    } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
//                        recyclerView.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL)
                        bottomSheet.setPadding(bottomSheet.paddingLeft, bottomSheet.paddingTop, bottomSheet.paddingRight, 0)

                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // 滑动时的回调
                    if (slideOffset>0){
                        bottomSheet.setPadding(bottomSheet.paddingLeft, bottomSheet.paddingTop, bottomSheet.paddingRight, remainHeight.times(1-slideOffset).toInt())

                    }
                }
            })

            bottomSheet.setPadding(bottomSheet.paddingLeft, bottomSheet.paddingTop, bottomSheet.paddingRight, remainHeight)

        }
    }


    private fun getData(): List<Item> {
        val items = mutableListOf<Item>()
        repeat(3) { index ->
            items.add(Item(index + 1, "浙商证券", 300 + index * 10, "提交成功"))
        }
        return items
    }

}



