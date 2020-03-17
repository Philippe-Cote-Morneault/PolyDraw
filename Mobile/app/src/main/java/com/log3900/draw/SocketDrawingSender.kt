package com.log3900.draw

import android.util.Log
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
    private lateinit var drawingID: UUID
    var isListening = true
    var receiver: SocketDrawingReceiver? = null

    fun sendStrokeStart(drawingID: UUID) {
        this.drawingID = drawingID
        socketService.sendMessage(Event.DRAW_START_CLIENT, UUIDUtils.uuidToByteArray(drawingID))
    }

    fun sendStrokeDraw(strokeInfo: StrokeInfo) {
        if (!isListening)
            return
        Log.d("DRAW_VIEW", "Send points: ${strokeInfo.points}")
        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                val strokeData = StrokeToBytesConverter.packStrokeInfo(strokeInfo)
//                receiver!!.drawStrokeData(strokeData)   // TODO: Switch back
                socketService.sendMessage(Event.STROKE_DATA_CLIENT, strokeData)
            }
        }
    }

    fun sendStrokeEnd() {
        socketService.sendMessage(Event.DRAW_END_CLIENT, UUIDUtils.uuidToByteArray(drawingID))
    }

    fun sendStrokeRemove(strokeID: UUID) {
//        receiver!!.onStrokeRemove(strokeID)
        socketService.sendMessage(Event.STROKE_ERASE_CLIENT, UUIDUtils.uuidToByteArray((strokeID)))
    }
}