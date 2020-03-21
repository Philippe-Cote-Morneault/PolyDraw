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
import kotlin.concurrent.scheduleAtFixedRate

class SocketDrawingSender() {
    private val SEND_INTERVAL_MS = 40L
    private val SEND_DELAY_MS = 0L

    private val socketService = SocketService.instance!!
    private lateinit var drawingID: UUID
    var isListening = true
    private val timer: Timer = Timer()
    private var strokeToSend: StrokeInfo? = null
    private var lastSentPointsIndex: Int = 0    // Sent points from the current stroke
    var receiver: SocketDrawingReceiver? = null

    init {
        startTimerSend()
    }

    private fun startTimerSend() {
        timer.scheduleAtFixedRate(SEND_DELAY_MS, SEND_INTERVAL_MS) {
            if (strokeToSend == null || strokeToSend?.points?.size == 0)
                return@scheduleAtFixedRate

            sendStrokeDraw(strokeToSend!!)
            lastSentPointsIndex += strokeToSend?.points?.size ?: 0
            strokeToSend = strokeToSend?.copy(points = listOf())
        }
    }

    fun addStrokeToSend(stroke: StrokeInfo) {
        when {
            strokeToSend == null -> {
                strokeToSend = stroke
            }
            strokeToSend?.strokeID == stroke.strokeID -> {
                val newPoints = stroke.points.drop(lastSentPointsIndex)
                strokeToSend = stroke.copy(points = newPoints)
            }
            else -> {
                sendStrokeDraw(strokeToSend ?: return)
                lastSentPointsIndex = 0
                strokeToSend = stroke
            }
        }
    }

    fun sendStrokeStart(drawingID: UUID) {
        this.drawingID = drawingID
        socketService.sendMessage(Event.DRAW_START_CLIENT, UUIDUtils.uuidToByteArray(drawingID))
    }

    fun sendStrokeDraw(strokeInfo: StrokeInfo) {
        if (!isListening)
            return
        Log.d("DRAW_VIEW", "Send points: \n")
        strokeInfo.points.forEach {
            Log.d("DRAW_VIEW", "(${it.x}, ${it.y})")
        }
        Log.d("DRAW_VIEW", "Sent ${strokeInfo.points.size} points")

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
//        receiver!!.onStrokeRemove(strokeID) // TODO: Switch back
        socketService.sendMessage(Event.STROKE_ERASE_CLIENT, UUIDUtils.uuidToByteArray((strokeID)))
    }

    fun stopListening(isListening: Boolean) {
        this.isListening = isListening
        if (isListening) {
            startTimerSend()
        } else {
            timer.cancel()
        }
    }
}