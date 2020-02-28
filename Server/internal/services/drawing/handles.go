package drawing

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

func (d *Drawing) handlePreview(message socket.RawMessageReceived) {
	drawingID, err := uuid.FromBytes(message.Payload.Bytes)
	if err != nil {
		sendPreviewResponse(message.SocketID, false)
		return
	}
	//Check if drawing exists
	game := model.Game{}
	model.DB().Preload("Image").Where("id = ?", drawingID).First(&game)
	if game.ID == uuid.Nil || game.Image.SVGFile == "" {
		sendPreviewResponse(message.SocketID, false)
		return
	}
	sendPreviewResponse(message.SocketID, true)
}

func sendPreviewResponse(socketID uuid.UUID, response bool) {
	byteResponse := byte(0x00)
	if response {
		byteResponse = byte(0x01)
	}

	bytesResponse := []byte{byteResponse}
	packet := socket.RawMessage{
		MessageType: byte(socket.MessageType.PreviewDrawingResponse),
		Length:      1,
		Bytes:       bytesResponse,
	}
	socket.SendRawMessageToSocketID(packet, socketID)
}
