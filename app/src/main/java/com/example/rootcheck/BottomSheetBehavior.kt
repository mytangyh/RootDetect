package com.example.rootcheck

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet) : BottomSheetBehavior<V>(context, attrs) {

    private var isTouchOnHandle = false

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 检查触摸事件是否在顶部的白条区域
                isTouchOnHandle = isTouchOnHandleView(event, child)
            }
        }
        return isTouchOnHandle && super.onInterceptTouchEvent(parent, child, event)
    }

    private fun isTouchOnHandleView(event: MotionEvent, child: View): Boolean {
        val handleView = child.findViewById<View>(R.id.dragHandle)
        val handleLocation = IntArray(2)
        handleView.getLocationOnScreen(handleLocation)
        val handleLeft = handleLocation[0]
        val handleRight = handleLeft + handleView.width
        val handleTop = handleLocation[1]
        val handleBottom = handleTop + handleView.height

        val touchX = event.rawX
        val touchY = event.rawY

        return touchX >= handleLeft && touchX <= handleRight && touchY >= handleTop && touchY <= handleBottom
    }
}