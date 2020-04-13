package com.log3900.draw

import android.os.Handler
import com.daveanthonythomas.moshipack.MoshiPack
import com.google.gson.JsonParser
import com.log3900.game.match.MatchManager
import com.log3900.game.match.PlayerTurnToDraw
import com.log3900.socket.Event
import com.log3900.socket.Message
import com.log3900.socket.SocketService
import com.log3900.utils.format.UUIDUtils
import com.log3900.utils.format.moshi.MatchAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class SocketDrawingReceiver(private val drawView: DrawViewBase) {
    private val socketService: SocketService = SocketService.instance!!
    private var socketHandler: Handler
    var isListening = true
    var matchManager: MatchManager? = null
    private var drawerIsCpu = false
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
        socketService.subscribeToMessage(Event.PLAYER_TURN_TO_DRAW, socketHandler)
    }

    fun unsubscribe() {
        socketService.unsubscribeFromMessage(Event.STROKE_DATA_SERVER, socketHandler)
        socketService.unsubscribeFromMessage(Event.STROKE_ERASE_SERVER, socketHandler)
        socketService.unsubscribeFromMessage(Event.PLAYER_TURN_TO_DRAW, socketHandler)
    }

    private fun handleSocketMessage(message: android.os.Message) {
//        if (!isListening)
//            return

        val socketMessage = message.obj as Message

        when (socketMessage.type) {
            Event.STROKE_DATA_SERVER -> {
                if (isListening) {
                    drawStrokeData(socketMessage.data)
                }
            }
            Event.STROKE_ERASE_SERVER -> {
                if (isListening) {
                    onStrokeRemove(socketMessage.data)
                }
            }
            Event.PLAYER_TURN_TO_DRAW -> {
                val playerTurnToDraw = socketMessage.data.unpackToPlayerTurnToDraw()
                onDrawerSwitch(playerTurnToDraw)
            }
            else -> return
        }
    }

    private fun ByteArray.unpackToPlayerTurnToDraw(): PlayerTurnToDraw {
        val json = MoshiPack.msgpackToJson(this)
        val jsonObject = JsonParser().parse(json).asJsonObject
        return MatchAdapter.jsonToPlayerTurnToDraw(jsonObject)
    }

    private fun onDrawerSwitch(playerTurnToDraw: PlayerTurnToDraw) {
        val drawerID = playerTurnToDraw.userID

        drawerIsCpu = matchManager?.getCurrentMatch()
            ?.players
            ?.find { it.ID == drawerID }
            ?.isCPU ?: false
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

                    if (drawerIsCpu) {
                        previewDrawStrokes(strokeInfo)
                    } else {
                        drawStrokes(strokeInfo)
                    }
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

    private suspend fun previewDrawStrokes(strokeInfo: StrokeInfo) {
        val points = strokeInfo.points
        if (points.isEmpty())
            return

        val tmpPoints = mutableListOf(points.first())
        for (i in 0 until points.size - 1) {
            if (calculateDistance(points[i], points[i + 1]) < 20) {
                tmpPoints.add(points[i + 1])
            } else {
                drawStrokes(strokeInfo.copy(strokeID = UUID.randomUUID(), points = tmpPoints))
                tmpPoints.clear()
                tmpPoints.add(points[i + 1])
            }
        }

        if (tmpPoints.size > 0) {
            drawStrokes(strokeInfo.copy(strokeID = UUID.randomUUID(), points = tmpPoints))
        }
    }

    private fun calculateDistance(point1: DrawPoint, point2: DrawPoint): Float {
        val xSquare = (point1.x - point2.x).pow(2)
        val ySquare = (point1.y - point2.y).pow(2)

        return sqrt(xSquare + ySquare)
    }

    fun onStrokeRemove(data: ByteArray) {
        val strokeID = UUIDUtils.byteArrayToUUID(data)
        drawView.removeStroke(strokeID)
    }
    fun onStrokeRemove(strokeID: UUID) {
        drawView.removeStroke(strokeID)
    }
}