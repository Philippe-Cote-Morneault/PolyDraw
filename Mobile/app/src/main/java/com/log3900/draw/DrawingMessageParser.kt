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
        BRUSH_SIZE(33),
        POINTS(34)
    }

    fun unpackStrokeInfo(data: ByteArray): StrokeInfo {
        val isEraser = data[0].toIsEraser()
        val color = data[0].toColor()
        val brushType = data[0].toBrushType()
        val brushSize = data[Offset.BRUSH_SIZE.value].toInt()
        val strokeID = data.sliceArray(Offset.STROKE_ID.value until Offset.USER_ID.value).getUUID()
        val userID = data.sliceArray(Offset.USER_ID.value until Offset.BRUSH_SIZE.value).getUUID()
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
        var bytes = this
        if (isLittleEndianOrder())
            bytes = this.reversed().toByteArray()

        val points = mutableListOf<DrawPoint>()
        for (i in bytes.indices step 4) {
            val x = bytesToUnsignedShort(bytes[i], bytes[i + 1])
            val y = bytesToUnsignedShort(bytes[i + 2], bytes[i + 3])

            val point = if (isLittleEndianOrder())
                DrawPoint(y.toFloat(), x.toFloat())
            else
                DrawPoint(x.toFloat(), y.toFloat())
            points.add(point)
        }

        return points
    }

    private fun bytesToUnsignedShort(byte1: Byte, byte2: Byte) : Int {
        if (!isLittleEndianOrder())
            return (((byte1.toInt() and 255) shl 8) or (byte2.toInt() and 255))


        return (((byte2.toInt() and 255) shl 8) or (byte1.toInt() and 255))

    }

    private fun isLittleEndianOrder(): Boolean = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
}