package com.example.rootcheck

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.RadioGroup
import android.widget.RelativeLayout

/**
 * 自动换行的RadioGroup
 */
class LineWrapRadioGroup : RadioGroup {
    private var lineCount = 0
    private var totalShowHeight = 0
    private var totalFolderShowHeight = 0
    private var number = 0

    // 控制是否显示所有
    private var showAll = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        measureChildren(widthMeasureSpec, heightMeasureSpec)

        var maxWidth = 0
        var totalHeight = 0
        var lineWidth = 0
        var maxLineHeight = 0
        var oldHeight: Int
        var oldWidth: Int
        val count = childCount

        for (i in 0 until count) {
            val child = getChildAt(i)
            val params = child.layoutParams as MarginLayoutParams
            oldHeight = maxLineHeight
            oldWidth = maxWidth
            val deltaX = child.measuredWidth + params.leftMargin + params.rightMargin
            if (lineWidth + deltaX + paddingLeft + paddingRight > widthSize) {
                maxWidth = maxOf(lineWidth, oldWidth)
                lineWidth = deltaX

                val externMarginTop = params.topMargin

                totalHeight += oldHeight
                maxLineHeight =
                    child.measuredHeight + params.topMargin + params.bottomMargin + externMarginTop

                lineCount++
                if (lineCount <= MAX_SHOW_COUNT) {
                    totalFolderShowHeight = totalHeight
                }
            } else {
                lineWidth += deltaX
                val deltaY = child.measuredHeight + params.topMargin + params.bottomMargin
                maxLineHeight = maxOf(maxLineHeight, deltaY)
            }
            if (i == count - 1) {
                totalHeight += maxLineHeight
                maxWidth = maxOf(lineWidth, oldWidth)
            }
        }

        maxWidth += paddingLeft + paddingRight
        totalHeight += paddingTop + paddingBottom
        totalShowHeight = totalHeight
        if (showAll) {
            setMeasuredDimension(
                if (widthMode == MeasureSpec.EXACTLY) widthSize else maxWidth,
                if (heightMode == MeasureSpec.EXACTLY) heightSize else totalHeight
            )
        } else {
            setMeasuredDimension(
                if (widthMode == MeasureSpec.EXACTLY) widthSize else maxWidth,
                if (heightMode == MeasureSpec.EXACTLY) heightSize else if (totalFolderShowHeight > 0) totalFolderShowHeight else totalShowHeight
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var count = childCount
        var preLeft = paddingLeft
        var preTop = paddingTop
        var maxHeight = 0
        var externMarginTop = 0
        var bFirstLine = true

        for (i in 0 until count) {
            val child = getChildAt(i)
            val params = child.layoutParams as MarginLayoutParams
            var left: Int

            if (preLeft + params.leftMargin + child.measuredWidth + params.rightMargin + paddingRight > r - l) {
                preLeft = paddingLeft
                preTop = preTop + maxHeight
                maxHeight = getChildAt(i).measuredHeight + params.topMargin + params.bottomMargin
                left = preLeft
                externMarginTop = externMarginTop + params.topMargin
                bFirstLine = false
            } else {
                maxHeight = maxOf(
                    maxHeight, child.measuredHeight + params.topMargin + params.bottomMargin
                )
                left = if (!bFirstLine) {
                    preLeft
                } else {
                    preLeft + params.leftMargin
                }
            }

            val top = preTop + externMarginTop
            val right = left + child.measuredWidth
            val bottom = top + child.measuredHeight
            child.layout(left, top, right, bottom)
            preLeft += params.leftMargin + child.measuredWidth + params.rightMargin
        }
    }

    fun requestShowMeasure(showAll: Boolean) {
        this.showAll = showAll
        requestLayout()
    }

    fun getNumber(): Int {
        return lineCount
    }

    fun setMax(i: Int) {
        MAX_SHOW_COUNT = i

    }

    companion object {
        private const val TAG = "RadioGroup"
        var MAX_SHOW_COUNT = 2
    }
}