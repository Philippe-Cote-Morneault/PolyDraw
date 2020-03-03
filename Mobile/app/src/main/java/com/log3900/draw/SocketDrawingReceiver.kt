package com.log3900.draw

import android.os.Handler
import android.util.Log
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.utils.format.UUIDUtils
import java.util.*

class SocketDrawingReceiver(private val drawView: DrawViewBase) {
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

        socketService.subscribeToMessage(Event.DRAW_PREVIEW_RESPONSE, Handler {
            Log.d("DRAW", (it.obj as Message).toString())
            val message = (it.obj as Message).data.toString()
            Log.d("DRAW", "Drawing starting: $message")
            true
        })

        socketService.subscribeToMessage(Event.STROKE_DATA_SERVER, Handler {
            Log.d("DRAW", "Stroke data server")
            val message = it.obj as Message
            parseMessageToStroke(message.data)
            true
        })

        val gameUUID = UUID.fromString("61db7e41-1cb2-4d88-a834-29c59dbcd389")
        Log.d("DRAW", gameUUID.toString())
        socketService.sendMessage(Event.DRAW_PREVIEW_REQUEST, UUIDUtils.uuidToByteArray(gameUUID))
    }

    private fun parseMessageToStroke(data: ByteArray) {
        val strokeInfo = DrawingMessageParser.unpackStrokeInfo(data)
        Log.d("DRAW", "Stroke info: $strokeInfo")
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