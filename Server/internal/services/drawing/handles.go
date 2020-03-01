package drawing

import (
	"encoding/binary"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	geometry "gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

//MaxUint16 represents the maximum value of a uint16
const MaxUint16 = ^uint16(0)

type point geometry.Point

//Stroke represent a stroke to be drawn on the client canvas
type Stroke struct {
	ID        uuid.UUID
	color     byte
	isEraser  bool
	isSquared bool
	width     uint16
	height    uint16
	brushSize byte
	points    []point
}

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
	uuidBytes, _ := drawingID.MarshalBinary()

	socket.SendRawMessageToSocketID(socket.RawMessage{
		MessageType: byte(socket.MessageType.StartDrawingServer),
		Length:      uint16(len(uuidBytes)),
		Bytes:       uuidBytes,
	}, message.SocketID)
	//Call method to start the drawing here
	//TODO Allan your code goes here!
	sendDummyDrawing(message.SocketID)
	socket.SendRawMessageToSocketID(socket.RawMessage{
		MessageType: byte(socket.MessageType.EndDrawingServer),
		Length:      uint16(len(uuidBytes)),
		Bytes:       uuidBytes,
	}, message.SocketID)

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

func sendDummyDrawing(socketID uuid.UUID) {
	fakeStroke := Stroke{
		ID:        uuid.New(),
		color:     0x02,
		isEraser:  false,
		isSquared: true,
		width:     1920,
		height:    1080,
		brushSize: 25,
	}
	points := []point{
		{0, 0},
		{5, 5},
		{10, 10},
		{15.5, 15.5},
	}
	fakeStroke.points = points

	payload := fakeStroke.Marshall()
	packet := socket.RawMessage{
		MessageType: byte(socket.MessageType.StrokeChunkServer),
		Length:      uint16(len(payload)),
		Bytes:       payload,
	}
	socket.SendRawMessageToSocketID(packet, socketID)
}

//Marshall encode the stroke to binary
func (s *Stroke) Marshall() []byte {
	if s.ID == uuid.Nil {
		s.ID = uuid.New()
	}
	if s.isEraser {
		s.color = 0x01
		s.isSquared = false
	}
	var firstByte byte
	firstByte = 0x00
	firstByte |= s.color

	if s.isSquared {
		firstByte |= 1 << 6
	}
	if s.isEraser {
		firstByte |= 1 << 7
	}
	strokeID, _ := s.ID.MarshalBinary()
	nulID, _ := uuid.Nil.MarshalBinary()
	width := make([]byte, 2)
	height := make([]byte, 2)

	binary.BigEndian.PutUint16(width, s.width)
	binary.BigEndian.PutUint16(height, s.height)

	response := make([]byte, 0, 40)
	response = append(response, firstByte)
	response = append(response, strokeID...)
	response = append(response, nulID...)
	response = append(response, width...)
	response = append(response, height...)
	response = append(response, s.brushSize)
	for i := range s.points {
		response = append(response, s.points[i].Marshall()...)
	}
	return response
}

func (p *point) Marshall() []byte {
	//Round the number up if larger than uint16 max
	p.round()
	x := uint16(p.X)
	y := uint16(p.Y)
	xArray := make([]byte, 2)
	yArray := make([]byte, 2)

	binary.BigEndian.PutUint16(xArray, x)
	binary.BigEndian.PutUint16(yArray, y)

	response := make([]byte, 0, 4)

	response = append(response, xArray...)
	response = append(response, yArray...)

	return response
}

func (p *point) round() {
	if p.X > float32(MaxUint16) {
		p.X = float32(MaxUint16)
	}

	if p.Y > float32(MaxUint16) {
		p.Y = float32(MaxUint16)
	}
}
