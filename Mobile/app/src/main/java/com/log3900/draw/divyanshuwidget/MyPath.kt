/**
 * @author Originally from https://github.com/divyanshub024/AndroidDraw
 * License: Apache 2.0
 * Modifications: None
 */

package com.log3900.draw.divyanshuwidget

import android.graphics.Path
import android.graphics.Point
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.*

class MyPath(
    val id: UUID = UUID.randomUUID(),
    val positionIndex: Int
) : Path(), Serializable {
    val actions = LinkedList<Action>()

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()

        val copiedActions = actions.map { it }
        copiedActions.forEach {
            it.perform(this)
        }
    }

    override fun reset() {
        actions.clear()
        super.reset()
    }

    override fun moveTo(x: Float, y: Float) {
//        println(FloatPoint(200f, 200f).isInBounds(FloatPoint(200.5f, 200.5f)))
//        points.add(FloatPoint(x, y))
        actions.add(Move(x, y))
        super.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
//        points.add(FloatPoint(x, y))
        actions.add(Line(x, y))
        super.lineTo(x, y)
    }

    override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        actions.add(Quad(x1, y1, x2, y2))
        super.quadTo(x1, y1, x2, y2)
    }
}