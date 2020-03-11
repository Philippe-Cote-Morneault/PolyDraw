package com.log3900.draw

import com.log3900.socket.Event
import com.log3900.socket.SocketService
import com.log3900.utils.format.UUIDUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class SocketDrawingSender() {
    private val socketService = SocketService.instance!!
    var isListening = true
    var drawingID = UUID.randomUUID()
    var receiver: SocketDrawingReceiver? = null

    fun sendStrokeStart() {
//        socketService.sendMessage(Event.DRAW_START_CLIENT, UUIDUtils.uuidToByteArray(drawingID))
    }

    fun sendStrokeDraw(strokeInfo: StrokeInfo) {
        if (!isListening)
            return

        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                val strokeData = StrokeToBytesConverter.packStrokeInfo(strokeInfo)
//                socketService.sendMessage(Event.STROKE_DATA_CLIENT, strokeData)
                receiver!!.parseMessageToStroke(strokeData)
            }
        }
    }

    fun sendStrokeEnd() {
//        socketService.sendMessage(Event.DRAW_END_CLIENT, UUIDUtils.uuidToByteArray(drawingID))
    }
}