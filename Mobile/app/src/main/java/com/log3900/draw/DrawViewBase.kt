/**
 * @author Originally from https://github.com/divyanshub024/AndroidDraw
 * License: Apache 2.0
 * Modifications:   - Added setTip()
 *                  - Added setDrawMode(), removed isEraser
 */

package com.log3900.draw

import android.content.Context
import android.graphics.*
import android.os.Handler
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.log3900.draw.divyanshuwidget.*
import com.log3900.socket.Event
import com.log3900.socket.SocketService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap
import kotlin.math.pow
import kotlin.math.sqrt

class DrawViewBase @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
    var canDraw: Boolean = true
) : View(context, attrs, defStyleAttr) {
    private var mPaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mLastPaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mPaint = Paint()
    private var mPath = MyPath()
    private var mPaintOptions = PaintOptions()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var mIsSaving = false
    private var mIsStrokeWidthBarEnabled = false

    private var socketDrawingReceiver: SocketDrawingReceiver? = null

    init {
        mPaint.apply {
            color = mPaintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mPaintOptions.strokeWidth
            isAntiAlias = true
        }

        if (canDraw) {
            socketDrawingReceiver = SocketDrawingReceiver(this)
        }
//        GlobalScope.launch {
//            mStartX = 200f
//            mStartY = 200f
//
//            drawStart(DrawPoint(200f, 200f))
//            for (i in 1..300) {
//                drawMove(DrawPoint(200f + i.toFloat(), 200f + i.toFloat()))
//                delay(10)
//            }
//            drawEnd()
//            invalidate()
//        }
    }

    fun enableCanDraw(canDrawOnCanvas: Boolean) {
        canDraw = canDrawOnCanvas

        if (!canDraw) {
            if (socketDrawingReceiver == null) {
                socketDrawingReceiver = SocketDrawingReceiver(this)
            }
        } else {
            // TODO: Set socketDrawingSender
        }
        // If we cannot draw, we want to receive strokes from the server
        socketDrawingReceiver?.isListening = !canDraw
        socketDrawingReceiver?.sendPreviewRequest()
    }

    fun drawStart(start: DrawPoint) {
        mPath.reset()
        mPath.moveTo(start.x, start.y)
        mCurX = start.x
        mCurY = start.y
        invalidate()
    }

    fun drawMove(point: DrawPoint) {
        mPath.quadTo(mCurX, mCurY, (point.x + mCurX) / 2, (point.y + mCurY) / 2)
        mCurX = point.x
        mCurY = point.y
        invalidate()
    }

    fun drawEnd() {
        mPath.lineTo(mCurX, mCurY)

        // draw a dot on click
        if (mPaintOptions.strokeCap == Paint.Cap.ROUND
            && mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY)
        }

        mPaths[mPath] = mPaintOptions
        mPath = MyPath()
        mPaintOptions = PaintOptions(
            mPaintOptions.color,
            mPaintOptions.strokeWidth,
            mPaintOptions.alpha,
            mPaintOptions.drawMode,
            mPaintOptions.strokeCap
        )
        invalidate()
    }

    fun setOptions(options: PaintOptions) {
        mPaintOptions = options
    }

    fun setColor(newColor: Int) {
        @ColorInt
        val alphaColor = ColorUtils.setAlphaComponent(newColor, mPaintOptions.alpha)
        mPaintOptions.color = alphaColor
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        mPaintOptions.strokeWidth = newStrokeWidth
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        mIsSaving = true
        draw(canvas)
        mIsSaving = false
        return bitmap
    }

    fun addPath(path: MyPath, options: PaintOptions) {
        mPaths[path] = options
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for ((key, value) in mPaths) {
            changePaint(value)
            canvas.drawPath(key, mPaint)
        }

        changePaint(mPaintOptions)
        canvas.drawPath(mPath, mPaint)
    }

    private fun changePaint(paintOptions: PaintOptions) {
        mPaint.color = if (paintOptions.drawMode == DrawMode.ERASE) Color.WHITE else paintOptions.color
        mPaint.strokeWidth = paintOptions.strokeWidth
        mPaint.strokeCap = paintOptions.strokeCap
    }

    fun clearCanvas() {
        mLastPaths = mPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
        mPath.reset()
        mPaths.clear()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canDraw)
            return true

        // TODO: REmove when done testing
        val x = event.x
        val y = event.y

        if (mPaintOptions.drawMode == DrawMode.REMOVE) {
//            if (event.action == MotionEvent.ACTION_DOWN)
            removePathIfIntersection(x, y)
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                drawStart(DrawPoint(x, y))
            }
            MotionEvent.ACTION_MOVE -> drawMove(DrawPoint(x, y))
            MotionEvent.ACTION_UP -> drawEnd()
        }

        invalidate()
        return true
    }

    private fun removePathIfIntersection(x: Float, y: Float) {
        val sortedMap = mPaths
        var keyToRemove = MyPath()
        var found = false
        for ((key, value) in sortedMap) {
            for (action in key.actions) {
                var width = 30
                if (value.strokeWidth > 30)
                    width = value.strokeWidth.toInt()
                if (action is Quad) {
                    val q: Quad = action
                    val distance1 = sqrt((q.x1.toDouble() - x.toDouble()).pow(2.0) + (q.y1.toDouble() - y.toDouble()).pow(2.0))
                    val distance2 = sqrt((q.x2.toDouble() - x.toDouble()).pow(2.0) + (q.y2.toDouble() - y.toDouble()).pow(2.0))
                    if (value.color != 0xFFFFFFFF.toInt() && (distance1 <= width || distance2 <= width)) {
                        found = true
                        keyToRemove = key
                    }
                } else if (action is Line) {
                    val q: Line = action
                    val distance = sqrt((q.x.toDouble() - x.toDouble()).pow(2.0) + (q.y.toDouble() - y.toDouble()).pow(2.0))
                    if (distance <= width && value.color != 0xFFFFFFFF.toInt()) {
                        found = true
                        keyToRemove = key
                    }
                }
            }
        }
        if(found) {
            mPaths.remove(keyToRemove)
            invalidate()
        }
//        val point = FloatPoint(x, y)
//        println("Checking point: $point")
//        mPaths.forEach { (myPath, paintOptions) ->
//
//            println(myPath.points)
//            if (myPath.points.any{ it.isInBounds(point) }) {
//                println("removing path!")
//                mPaths.remove(myPath)
//            }
//        }
        invalidate()
    }

    fun setCap(cap: Paint.Cap) {
        mPaintOptions.strokeCap = cap
    }

    fun setDrawMode(mode: DrawMode) {
        mPaintOptions.drawMode = mode
        when (mode) {
            DrawMode.DRAW -> {}
            DrawMode.REMOVE -> {}
            DrawMode.ERASE -> {
                invalidate()
            }
        }
    }
}