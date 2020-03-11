package com.log3900.draw

import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.log3900.draw.divyanshuwidget.DrawMode
import com.log3900.draw.divyanshuwidget.PaintOptions
import com.log3900.utils.format.UUIDUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

private enum class Offset(val value: Int) {
    STROKE_ID(1),
    USER_ID(17),
    BRUSH_SIZE(33),
    POINTS(34)
}

object BytesToStrokeConverter {
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
        Log.d("DRAW_BYTES", "data size: ${data.size}, points size: ${points.size}")
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
        return if ((this.toInt() and 0x40) shr 6 == 0)
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

@ExperimentalUnsignedTypes
object StrokeToBytesConverter {
    fun packStrokeInfo(strokeInfo: StrokeInfo): ByteArray {
        val (strokeID, userID, paintOptions, points) = strokeInfo
        val (color, strokeWidth, _, drawMode, strokeCap) = paintOptions
        var data = UByteArray(1)

        data[0] = setEraserBit(drawMode)
        data[0] = data[0] or setColorBits(color)
        data[0] = data[0] or setBrushBit(strokeCap)

        data += UUIDUtils.uuidToByteArray(strokeID).toUByteArray()
        data += UUIDUtils.uuidToByteArray(userID).toUByteArray()
        data += ubyteArrayOf(strokeWidth.toInt().toUByte())
        data += pointsToBytes(points)

        return data.toByteArray()
    }

    private fun setEraserBit(drawMode: DrawMode): UByte {
        return when (drawMode) {
            DrawMode.DRAW -> 0
            else -> 1 shl 7
        }.toUByte()
    }

    private fun setColorBits(color: Int): UByte {
        val colorVal = when (color) {
            Color.BLACK     -> 0x0
            Color.WHITE     -> 0x1
            Color.RED       -> 0x2
            Color.GREEN     -> 0x3
            Color.BLUE      -> 0x4
            Color.YELLOW    -> 0x5
            Color.CYAN      -> 0x6
            Color.MAGENTA   -> 0x7
            else            -> 0x0
        }
        return (colorVal and 0x0F).toUByte()
    }

    private fun setBrushBit(strokeCap: Paint.Cap): UByte {
        val capValue = if (strokeCap == Paint.Cap.SQUARE) 1 else 0
        return (capValue shl 6).toUByte()
    }

    private fun pointsToBytes(points: List<DrawPoint>): UByteArray {
        var bytes = ubyteArrayOf()
        for (point in points) {
            bytes += shortToUBytes(point.x.toShort())
            bytes += shortToUBytes(point.y.toShort())
        }
        return bytes
    }

    private fun shortToUBytes(short: Short): UByteArray {
        return ubyteArrayOf(
            (short.toInt() and 0xFF).toUByte(),
            ((short.toInt() shr 8) and 0xFF).toUByte()
        )
    }
}