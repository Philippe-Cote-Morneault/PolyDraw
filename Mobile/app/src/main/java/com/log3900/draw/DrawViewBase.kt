/**
 * @author Originally from https://github.com/divyanshub024/AndroidDraw
 * License: Apache 2.0
 * Modifications:   - Added setTip()
 *                  - Added setDrawMode(), removed isEraser
 */

package com.log3900.draw

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.log3900.draw.divyanshuwidget.*
import com.log3900.user.account.AccountRepository
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import kotlin.math.sqrt

fun MyPath.toPoints(): List<DrawPoint> {
    val points = mutableListOf<DrawPoint>()
    for (action in this.actions) {
        when (action) {
            is Move -> points.add(DrawPoint(action.x, action.y))
            is Line -> points.add(DrawPoint(action.x, action.y))
            is Quad -> {
                points.add(DrawPoint(action.x1, action.y1))
                points.add(DrawPoint(action.x2, action.y2))
            }
        }
    }
//    Log.d("DRAW", "$points")

    return points
}

class DrawViewBase @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
    var canDraw: Boolean = true
) : View(context, attrs, defStyleAttr) {
    private val MAX_WIDTH = 1125f
    private val MIN_WIDTH = 0f
    private val MAX_HEIGHT = 750f
    private val MIN_HEIGHT = 0f

    private var mPaths = ConcurrentHashMap<MyPath, PaintOptions>()

    private var mLastPaths = ConcurrentHashMap<MyPath, PaintOptions>()

    private var mPaint = Paint()
    private var nextPathPosition: Int = 0
    private var mPath = MyPath(positionIndex =  getNextPathPosition())
    private var lastPathID: UUID = mPath.id
    private var mPaintOptions = PaintOptions()

    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var mIsSaving = false
    private var mIsStrokeWidthBarEnabled = false

    val userID = AccountRepository.getInstance().getAccount().ID

    var socketDrawingReceiver: SocketDrawingReceiver? = null
    var socketDrawingSender: SocketDrawingSender? = null

    init {
        mPaint.apply {
            color = mPaintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mPaintOptions.strokeWidth
            isAntiAlias = true
        }

        Log.d("TOUCH_EVENT", "w: $width, h: $height")

//        if (canDraw) {
//            socketDrawingSender = SocketDrawingSender()
//        } else {
//        }
        socketDrawingReceiver = SocketDrawingReceiver(this)
    }

    fun enableCanDraw(canDrawOnCanvas: Boolean, drawingID: UUID?) {
        canDraw = canDrawOnCanvas

        if (!canDraw) {
            if (socketDrawingReceiver == null) {
                socketDrawingReceiver = SocketDrawingReceiver(this)
            }
        } else {
            if (socketDrawingSender == null) {
                socketDrawingSender = SocketDrawingSender()
            }
            if (drawingID != null) {
                socketDrawingSender?.sendStrokeStart(drawingID)
            }
        }
        // If we cannot draw, we want to receive strokes from the server
        socketDrawingReceiver?.isListening = !canDraw
//        socketDrawingReceiver?.sendPreviewRequest()

        socketDrawingSender?.isListening = canDraw
    }

    fun drawStart(start: DrawPoint, strokeID: UUID? = null) {
        // If stroke is a continuation of the last one
        if (strokeID != null && lastPathID == strokeID) {
            drawMove(start)
            return
        }

        mPath = strokeID
            ?.let { MyPath(it, getNextPathPosition()) }
            ?: MyPath(positionIndex =  getNextPathPosition())
        mPath.reset()

        mPath.moveTo(start.x, start.y)
        mCurX = start.x
        mCurY = start.y
        invalidate()

        if (canDraw)
            sendStrokeInfo()
    }

    fun drawMove(point: DrawPoint) {
//        interpolateDraw(point.x.toInt(), point.y.toInt())
        mPath.quadTo(mCurX, mCurY, (point.x + mCurX) / 2, (point.y + mCurY) / 2)
        mCurX = point.x
        mCurY = point.y
        invalidate()
        if (canDraw)
            sendStrokeInfo()
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

//        mPath = MyPath()  TODO
        lastPathID = mPath.id

        mPaintOptions = PaintOptions(
            mPaintOptions.color,
            mPaintOptions.strokeWidth,
            mPaintOptions.alpha,
            mPaintOptions.drawMode,
            mPaintOptions.strokeCap
        )
        invalidate()
        if (canDraw)
            sendStrokeInfo()
    }

    private fun sendStrokeInfo() {
        val points = mPath.toPoints()
        val strokeInfo = StrokeInfo(
            mPath.id,
            userID,
            mPaintOptions,
            points
        )
//        socketDrawingSender!!.sendStrokeDraw(strokeInfo)
        socketDrawingSender!!.addStrokeToSend(strokeInfo)
    }

    fun removeStroke(strokeID: UUID) {
        mPaths.keys
            .find { it.id == strokeID }
            ?.let {
                mPaths.remove(it)
                it.reset()
                invalidate()
            }
    }

    fun setOptions(options: PaintOptions) {
        mPaintOptions = options
        changePaint(options)
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

        mPaths
            .toSortedMap(compareBy { it.positionIndex })
            .forEach { (path, paintOptions) ->
                changePaint(paintOptions)
                canvas.drawPath(path, mPaint)
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
        mLastPaths = ConcurrentHashMap(mPaths) // as ConcurrentHashMap<MyPath, PaintOptions>
        mPath.reset()
        mPaths.clear()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canDraw)
            return true

        // TODO: REmove when done testing
        val x = when {
            event.x < MIN_WIDTH -> MIN_WIDTH
            event.x > MAX_WIDTH -> MAX_WIDTH
            else -> event.x
        }
        val y = when {
            event.y < MIN_HEIGHT -> MIN_HEIGHT
            event.y > MAX_HEIGHT -> MAX_HEIGHT
            else -> event.y
        }
        
        Log.d("TOUCH_EVENT", "($x, $y)")

        if (mPaintOptions.drawMode == DrawMode.REMOVE) {
//            if (event.action == MotionEvent.ACTION_DOWN)
            removePathIfIntersection(x, y)
            return true
        }

        Log.d("TOUCH", "($x, $y), CURR($mCurX, $mCurY)")

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                drawStart(DrawPoint(x, y))
            }
            MotionEvent.ACTION_MOVE -> {
//                interpolateDraw(x.toInt(), y.toInt())
                drawMove(DrawPoint(x, y))
            }
            MotionEvent.ACTION_UP -> drawEnd()
        }

        invalidate()
        return true
    }

    private fun interpolateDraw(x: Int, y: Int) {
        val lastX = mCurX
        val lastY = mCurY

        val pointsToInterpolate = 10

        val difX = (x - lastX) / pointsToInterpolate
        val difY = (y - lastY) / pointsToInterpolate

        for (i in 1 until pointsToInterpolate) {
            val point = DrawPoint(lastX + i * difX, lastY + i * difY)
            mPath.quadTo(mCurX, mCurY, (point.x + mCurX) / 2, (point.y + mCurY) / 2)
            mCurX = point.x
            mCurY = point.y
        }

//        mCurX = x.toFloat()
//        mCurY = y.toFloat()
    }

    private fun removePathIfIntersection(x: Float, y: Float) {
        val sortedMap = mPaths
        var keyToRemove: MyPath? = null
        for ((key, value) in sortedMap) {
            if (value.drawMode == DrawMode.ERASE) continue
            for (action in key.actions) {
                var width = 30
                if (value.strokeWidth > 30)
                    width = value.strokeWidth.toInt()
                if (action is Quad) {
                    val q: Quad = action
                    val distance1 = sqrt((q.x1.toDouble() - x.toDouble()).pow(2.0) + (q.y1.toDouble() - y.toDouble()).pow(2.0))
                    val distance2 = sqrt((q.x2.toDouble() - x.toDouble()).pow(2.0) + (q.y2.toDouble() - y.toDouble()).pow(2.0))
                    if (value.color != 0xFFFFFFFF.toInt() && (distance1 <= width || distance2 <= width)) {
                        keyToRemove = key
                    }
                } else if (action is Line) {
                    val q: Line = action
                    val distance = sqrt((q.x.toDouble() - x.toDouble()).pow(2.0) + (q.y.toDouble() - y.toDouble()).pow(2.0))
                    if (distance <= width && value.color != 0xFFFFFFFF.toInt()) {
                        keyToRemove = key
                    }
                }
            }
        }
        if (keyToRemove != null) {
            Log.d("DRAW_CANVAS", "Removing ${keyToRemove.toString()}")
            mPaths.remove(keyToRemove)
            if (canDraw) {
                socketDrawingSender?.sendStrokeRemove(keyToRemove.id)
            }
            keyToRemove.reset()
        }

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
                mPaintOptions.color = Color.WHITE
            }
        }
    }

    private fun getNextPathPosition(): Int = nextPathPosition++
}