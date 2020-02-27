/**
 * @author Originally from https://github.com/divyanshub024/AndroidDraw
 * License: Apache 2.0
 * Modifications: Added DrawMode
 */

package com.log3900.draw.divyanshuwidget

import android.graphics.Color
import android.graphics.Paint

enum class DrawMode { DRAW, REMOVE, ERASE }

data class PaintOptions(
    var color: Int = Color.BLACK,
    var strokeWidth: Float = 8f,
    var alpha: Int = 255,
    var drawMode: DrawMode = DrawMode.DRAW,
    var strokeCap: Paint.Cap = Paint.Cap.ROUND
)
