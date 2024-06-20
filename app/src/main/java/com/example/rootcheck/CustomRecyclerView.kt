package com.example.rootcheck

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class CustomRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        val view = findChildViewUnder(e.x, e.y)
        return view != null && getChildAdapterPosition(view) != 0 || super.onInterceptTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val view = findChildViewUnder(e.x, e.y)
        return view != null && getChildAdapterPosition(view) != 0 || super.onTouchEvent(e)
    }
}