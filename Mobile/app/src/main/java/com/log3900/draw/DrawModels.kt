package com.log3900.draw

import com.log3900.draw.divyanshuwidget.PaintOptions
import java.util.*

data class DrawPoint(val x: Float, val y: Float)

data class StrokeInfo(
    val strokeID:       UUID,
    val userID:         UUID,
    val paintOptions:   PaintOptions,
    val points:         List<DrawPoint>
)