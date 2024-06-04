package com.example.rootcheck.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import com.example.rootcheck.R


class CustomSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    private val thumbView: View
    private val thumbTextView: TextView

    init {
        // Inflate the thumb layout
        val inflater = LayoutInflater.from(context)
        thumbView = inflater.inflate(R.layout.thumb, null, false)
        thumbTextView = thumbView.findViewById(R.id.thumb_text)

        // Measure and layout the thumb view
        thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        thumbView.layout(0, 0, thumbView.measuredWidth, thumbView.measuredHeight)

        // Convert the thumb view to a drawable
        updateThumb(progress)
    }

    private fun updateThumb(progress: Int) {
        thumbTextView.text = progress.toString()

        thumbView.isDrawingCacheEnabled = true
        thumbView.buildDrawingCache()
        val bitmap: Bitmap = Bitmap.createBitmap(thumbView.drawingCache)
        thumbView.isDrawingCacheEnabled = false

        val thumbDrawable = BitmapDrawable(resources, bitmap)
        thumbDrawable.setBounds(0, 0, thumbDrawable.intrinsicWidth, thumbDrawable.intrinsicHeight)
        thumb = thumbDrawable
    }

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        updateThumb(progress)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Update the thumb text dynamically
        updateThumb(progress)
    }
}