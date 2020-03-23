package com.log3900.draw

import android.os.Handler
import android.util.Log
import com.log3900.draw.divyanshuwidget.DrawMode
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.utils.format.UUIDUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.Long.min
import java.util.*

class SocketDrawingReceiver(private val drawView: DrawViewBase) {
    private val socketService: SocketService = SocketService.instance!!
    private var socketHandler: Handler
    var isListening = true
    private val strokeMutex = Mutex()

    init {
        socketHandler = Handler {
            handleSocketMessage(it)
            true
        }

        subscribe()
        
//        sendPreviewRequest()
    }

    fun subscribe() {
        socketService.subscribeToMessage(Event.STROKE_DATA_SERVER, socketHandler)
        socketService.subscribeToMessage(Event.STROKE_ERASE_SERVER, socketHandler)
    }

    fun unsubscribe() {
        socketService.unsubscribeFromMessage(Event.STROKE_DATA_SERVER, socketHandler)
        socketService.unsubscribeFromMessage(Event.STROKE_ERASE_SERVER, socketHandler)
    }

    private fun handleSocketMessage(message: android.os.Message) {
        if (!isListening)
            return

        val socketMessage = message.obj as Message

        when (socketMessage.type) {
            Event.STROKE_DATA_SERVER -> drawStrokeData(socketMessage.data)
            Event.STROKE_ERASE_SERVER -> onStrokeRemove(socketMessage.data)
            else -> return
        }
    }

    @Deprecated("Test purposes only")
    fun sendPreviewRequest() {
        val gameUUID = UUID.fromString("61db7e41-1cb2-4d88-a834-29c59dbcd389")  // TODO: Remove
        socketService.sendMessage(Event.DRAW_PREVIEW_REQUEST, UUIDUtils.uuidToByteArray(gameUUID))
    }

    fun onStrokeStart(data: ByteArray) {
        // Drawing id...
    }

    fun drawStrokeData(data: ByteArray) {
        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                strokeMutex.withLock {
                    val strokeInfo = BytesToStrokeConverter.unpackStrokeInfo(data)
                    drawStrokes(strokeInfo)
                }
            }
        }
    }

    private suspend fun drawStrokes(strokeInfo: StrokeInfo) {
        val (strokeID, userID, paintOptions, points) = strokeInfo
        if (points.isEmpty())
            return

        Log.d("DRAW_VIEW", "Received ${points.size} points")
//        Log.d("DRAW_VIEW", "Erase is ${paintOptions.drawMode == DrawMode.ERASE}")
        drawView.setOptions(paintOptions)

//        val time = min((20 / points.size).toLong(), 1)  // TODO: Validate delay
        val time = 0L  // TODO: Validate delay
//        val time = 1000L  // TODO: Validate delay
        drawView.drawStart(points.first(), strokeID)
        delay(time)

        for (point in points.drop(1)) {
            drawView.drawMove(point)
            delay(time/50)
        }
        delay(time)
        drawView.drawEnd()
    }

    fun onStrokeRemove(data: ByteArray) {
        val strokeID = UUIDUtils.byteArrayToUUID(data)
        drawView.removeStroke(strokeID)
    }
    fun onStrokeRemove(strokeID: UUID) {
        drawView.removeStroke(strokeID)
    }
}