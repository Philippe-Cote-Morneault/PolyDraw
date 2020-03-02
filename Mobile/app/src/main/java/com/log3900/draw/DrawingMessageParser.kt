package com.log3900.draw

import android.graphics.Color
import android.graphics.Paint
import com.log3900.draw.divyanshuwidget.DrawMode
import com.log3900.draw.divyanshuwidget.PaintOptions
import java.nio.ByteOrder
import java.util.*

object DrawingMessageParser {
    private enum class Offset(val value: Int) {
        STROKE_ID(1),
        USER_ID(17),
        MAX_X(33),
        MAX_Y(35),
        BRUSH_SIZE(37),
        POINTS(38)
    }

    fun unpackStrokeInfo(data: ByteArray): StrokeInfo {
        val isEraser = data[0].toIsEraser()
        val color = data[0].toColor()
        val brushType = data[0].toBrushType()
        val brushSize = data[Offset.BRUSH_SIZE.value].toInt()
        val strokeID = data.sliceArray(Offset.STROKE_ID.value until Offset.USER_ID.value).getUUID()
        val userID = data.sliceArray(Offset.USER_ID.value until Offset.MAX_X.value).getUUID()
        val points = data.sliceArray(Offset.POINTS.value until data.size).getPoints()

        val paintOptions = PaintOptions(
            color = color,
            strokeCap = brushType,
            strokeWidth = brushSize.toFloat(),
            drawMode = if (isEraser) DrawMode.ERASE else DrawMode.DRAW
        )

        return StrokeInfo(
            strokeID,
            userID,
            paintOptions,
            points
        )
    }

    private fun Byte.toIsEraser() = (this.toInt() shr 7) == 1

    private fun Byte.toColor(): Int {
        return when (this.toInt() and 0x0F) {
            0 -> Color.BLACK
            1 -> Color.WHITE
            2 -> Color.RED
            3 -> Color.GREEN
            4 -> Color.BLUE
            5 -> Color.YELLOW
            6 -> Color.CYAN
            7 -> Color.MAGENTA
            else -> Color.BLACK
        }
    }

    private fun Byte.toBrushType(): Paint.Cap {
        return if ((this.toInt() and 0x40) == 0)
            Paint.Cap.ROUND
        else
            Paint.Cap.SQUARE
    }

    private fun ByteArray.getUUID(): UUID {
        if (isLittleEndianOrder())
            this.reverse()
        return UUID.nameUUIDFromBytes(this)
    }

    private fun ByteArray.getPoints(): List<DrawPoint> {
        if (isLittleEndianOrder())
            this.reverse()

        val points = mutableListOf<DrawPoint>()
        for (i in this.indices step 4) {
            val x = this.sliceArray(i..i+1).toInt()
            val y = this.sliceArray(i+2..i+3).toInt()
            points.add(DrawPoint(x.toFloat(), y.toFloat()))
        }

        return points
    }

    // https://stackoverflow.com/questions/56872782/convert-byte-array-to-int-odd-result-java-and-kotlin
    private fun ByteArray.toInt(): Int {
        var result = 0
        for (i in this.indices) {
            result = result or (this[i].toInt() shl 8 * i)
        }
        return result
    }

    private fun isLittleEndianOrder(): Boolean = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
}