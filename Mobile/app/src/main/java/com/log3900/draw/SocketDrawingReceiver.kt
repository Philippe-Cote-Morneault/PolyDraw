package com.log3900.draw

import android.os.Handler
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.utils.format.UUIDUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
        val (strokeID, _, paintOptions, points) = strokeInfo
        if (points.isEmpty())
            return

        drawView.setOptions(paintOptions)

        drawView.drawStart(points.first(), strokeID)
        for (point in points.drop(1)) {
            drawView.drawMove(point)
        }
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