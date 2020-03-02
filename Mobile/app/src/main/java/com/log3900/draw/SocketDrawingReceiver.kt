package com.log3900.draw

import android.os.Handler
import android.util.Log
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.utils.format.UUIDUtils
import java.util.*

class SocketDrawingReceiver(val drawView: DrawViewBase) {
    private val socketService: SocketService = SocketService.instance!!

    init {
        socketService.subscribeToMessage(Event.DRAW_START_SERVER, Handler {
            Log.d("DRAW", (it.obj as Message).toString())
            true
        })

        socketService.subscribeToMessage(Event.DRAW_END_SERVER, Handler {
            Log.d("DRAW", (it.obj as Message).toString())
            true
        })

//        socketService.subscribeToMessage(Event.DRAW_PREVIEW, Handler {
//            Log.d("DRAW", (it.obj as Message).toString())
//            true
//        })

        socketService.subscribeToMessage(Event.DRAW_PREVIEW_RESPONSE, Handler {
            Log.d("DRAW", (it.obj as Message).toString())
            val message = it.obj as Message
            parseMessageToStroke(message.data)
            true
        })

//        socketService.sendSerializedMessage(Event.DRAW_PREVIEW_REQUEST, UUIDUtils.uuidToByteArray())
    }

    private fun parseMessageToStroke(data: ByteArray) {
        val strokeInfo = DrawingMessageParser.unpackStrokeInfo(data)

        drawStrokes(strokeInfo)
    }

    // Probably a drawer class or something...
    private fun drawStrokes(strokeInfo: StrokeInfo) {
        // TODO: Delai pour le chaque point?
        val (strokeID, userID, paintOptions, points) = strokeInfo

        // ...

        drawView.setOptions(paintOptions)
        drawView.drawStart(points.first())
        for (point in points.drop(1)) {
            drawView.drawMove(point)
        }
        drawView.drawEnd()
    }
}