package drawing

import (
	"encoding/binary"
	"encoding/xml"
	"io/ioutil"
	"log"
	"sync"
	"time"

	"gitlab.com/jigsawcorp/log3900/internal/services/potrace"

	"github.com/google/uuid"
	"github.com/tevino/abool"
	"gitlab.com/jigsawcorp/log3900/internal/datastore"
	svgmodel "gitlab.com/jigsawcorp/log3900/internal/services/potrace/model"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/internal/strokegenerator"
	"gitlab.com/jigsawcorp/log3900/internal/svgparser"
	"gitlab.com/jigsawcorp/log3900/model"
	geometry "gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

//MaxUint16 represents the maximum value of a uint16
const MaxUint16 = ^uint16(0)
const maxPointsperPacket = 16000
const delayDrawSending = 20  //in Milliseconds
const drawingTimePreview = 5 //in Seconds

var pendingPreviews map[uuid.UUID]*abool.AtomicBool

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
	pendingPreviews[message.SocketID] = abool.New()
	StartDrawing([]uuid.UUID{message.SocketID}, uuidBytes, &Draw{SVGFile: game.Image.SVGFile, DrawingTimeFactor: 1, Mode: game.Image.Mode}, &DrawState{StopDrawing: pendingPreviews[message.SocketID], Time: drawingTimePreview})
	removePreview(message.SocketID)
}

// StartDrawing starts the drawing procedure
func StartDrawing(socketsID []uuid.UUID, uuidBytes []byte, draw *Draw, drawState *DrawState) {
	payloads := generateDrawing(draw, drawState.Time)
	var wg sync.WaitGroup
	wg.Add(len(socketsID))
	for _, id := range socketsID {
		go func(socketID uuid.UUID) {
			defer wg.Done()
			socket.SendQueueMessageSocketID(socket.RawMessage{
				MessageType: byte(socket.MessageType.StartDrawingServer),
				Length:      uint16(len(uuidBytes)),
				Bytes:       uuidBytes,
			}, socketID)

			sendDrawing(socketID, payloads, drawState.StopDrawing)

			socket.SendQueueMessageSocketID(socket.RawMessage{
				MessageType: byte(socket.MessageType.EndDrawingServer),
				Length:      uint16(len(uuidBytes)),
				Bytes:       uuidBytes,
			}, socketID)
		}(id)
	}
	wg.Wait()
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
	socket.SendQueueMessageSocketID(packet, socketID)
}

func sendDrawing(socketID uuid.UUID, payloads [][]byte, stopDrawing *abool.AtomicBool) {
	for _, payload := range payloads {
		if stopDrawing.IsSet() {
			return
		}
		packet := socket.RawMessage{
			MessageType: byte(socket.MessageType.StrokeChunkServer),
			Length:      uint16(len(payload)),
			Bytes:       payload,
		}

		socket.SendQueueMessageSocketID(packet, socketID)
		//Wait 20ms between strokes
		time.Sleep(delayDrawSending * time.Millisecond)
	}
}

func generateDrawing(draw *Draw, drawingTime float64) [][]byte {
	file, err := datastore.GetFile(draw.SVGFile)
	if err != nil {
		log.Println(err)
	}
	byteValue, _ := ioutil.ReadAll(file)
	var xmlSvg svgmodel.XMLSvg

	err = xml.Unmarshal(byteValue, &xmlSvg)
	if err != nil {
		log.Println(err)
	}
	//If random we shuffle it
	if draw.Mode == 1 {
		potrace.Random(&xmlSvg.G.XMLPaths)
	}

	var commands []svgparser.Command
	var payloads [][]byte
	timePerStroke := ((drawingTime * 1000) / float64(len(xmlSvg.G.XMLPaths))) * draw.DrawingTimeFactor
	for _, path := range xmlSvg.G.XMLPaths {
		stroke := Stroke{
			ID:        uuid.New(),
			color:     path.Color,
			isEraser:  path.Eraser,
			isSquared: path.Brush != "circle",
			brushSize: byte(path.BrushSize),
		}
		commands = svgparser.ParseD(path.D, nil)
		stroke.points = strokegenerator.ExtractPointsStrokes(&commands)
		s := stroke.clone()
		splitPointsIntoPayloads(&payloads, &stroke.points, &s, int(timePerStroke))
	}
	return payloads
}

func splitPointsIntoPayloads(payloads *[][]byte, points *[]geometry.Point, stroke *Stroke, time int) {

	if time == 0 {
		return
	}

	nbPointsperPacket := int((float64(delayDrawSending) / float64(time)) * float64(len(*points)))

	if nbPointsperPacket == 0 {
		nbPointsperPacket = len(*points)
	}

	if nbPointsperPacket >= maxPointsperPacket {
		for nbPointsperPacket >= maxPointsperPacket {
			nbPointsperPacket /= 2
		}
	}

	index := 0
	iterations := len(*points)/nbPointsperPacket + 1

	for i := 0; i < iterations; i++ {
		if nbPointsperPacket+index >= len(*points) {
			stroke.points = (*points)[index:]
			*payloads = append(*payloads, stroke.Marshall())
			break
		}

		stroke.points = (*points)[index : index+nbPointsperPacket]

		*payloads = append(*payloads, stroke.Marshall())
		index += nbPointsperPacket
		stroke.points = nil
	}
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

	response := make([]byte, 0, 40)
	response = append(response, firstByte)
	response = append(response, strokeID...)
	response = append(response, nulID...)
	response = append(response, s.brushSize)
	for i := range s.points {
		response = append(response, marshallPoint(s.points[i])...)
	}
	return response
}

func marshallPoint(p geometry.Point) []byte {
	//Round the number up if larger than uint16 max
	round(&p)
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

//Marshall encode the stroke to binary
func (s *Stroke) clone() Stroke {
	return Stroke{
		ID:        s.ID,
		color:     s.color,
		isEraser:  s.isEraser,
		isSquared: s.isSquared,
		brushSize: s.brushSize,
	}
}

func round(p *geometry.Point) {
	if p.X > float32(MaxUint16) {
		p.X = float32(MaxUint16)
	}

	if p.Y > float32(MaxUint16) {
		p.Y = float32(MaxUint16)
	}
}

//Stops the preview when stopPreview socket message is sent
func stopPreview(message socket.RawMessageReceived) {
	stopDrawing, ok := pendingPreviews[message.SocketID]

	if !ok {
		log.Printf("[Drawing] -> [Error] Cannot stop preview. Cannot find preview with socketID")
		return
	}

	stopDrawing.Set()
	removePreview(message.SocketID)
}

//Remove the preview in managerInstance stored in cache
func removePreview(socketID uuid.UUID) {

	if _, ok := pendingPreviews[socketID]; !ok {
		log.Printf("[Drawing] -> [Error] Cannot remove preview from cache. Cannot find preview with socketID")
		return
	}
	delete(pendingPreviews, socketID)
}
