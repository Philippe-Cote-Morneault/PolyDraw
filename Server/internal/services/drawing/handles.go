package drawing

import (
	"encoding/binary"
	"encoding/xml"
	"io/ioutil"
	"log"
	"time"

	"github.com/google/uuid"
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
const delayDrawSending = 20 //in Milliseconds

//Stroke represent a stroke to be drawn on the client canvas
type Stroke struct {
	ID        uuid.UUID
	color     byte
	isEraser  bool
	isSquared bool
	brushSize byte
	points    []geometry.Point
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

	sendDrawing(message.SocketID, game.Image.SVGFile)

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

func sendDrawing(socketID uuid.UUID, svgKey string) {
	file, err := datastore.GetFile(svgKey)
	if err != nil {
		log.Println(err)
	}
	byteValue, _ := ioutil.ReadAll(file)
	var xmlSvg svgmodel.XMLSvg

	err = xml.Unmarshal(byteValue, &xmlSvg)
	if err != nil {
		log.Println(err)
	}

	var commands []svgparser.Command
	var payloads [][]byte
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
		// log.Printf("[Drawing] Number of points in stroke : %v", len(stroke.points))
		s := stroke.clone()
		splitPointsIntoPayloads(&payloads, &stroke.points, &s, path.Time)
		// payloads = append(payloads, stroke.Marshall())
	}
	for _, payload := range payloads {
		packet := socket.RawMessage{
			MessageType: byte(socket.MessageType.StrokeChunkServer),
			Length:      uint16(len(payload)),
			Bytes:       payload,
		}

		socket.SendRawMessageToSocketID(packet, socketID)
		// log.Printf("[Drawing] %v | Length of current paquet : %v.", time.Now(), packet.Length)
		//Wait 20ms between strokes
		time.Sleep(delayDrawSending * time.Millisecond)
	}
}

func splitPointsIntoPayloads(payloads *[][]byte, points *[]geometry.Point, stroke *Stroke, time int) {

	nbPoints := (delayDrawSending * len(*points)) / time

	if nbPoints == 0 {
		nbPoints = 1
	} else if nbPoints >= maxPointsperPacket {
		for nbPoints >= maxPointsperPacket {
			nbPoints /= 2
		}
	}

	// log.Printf("[Drawing] %v points per %v ms is (with total time of %v ms).", nbPoints, delayDrawSending, time)

	index := 0
	// TODO a supprimer et faire boucle infinie apres debug
	iterations := len(*points)/nbPoints + 1
	// log.Printf("[Drawing] iterations : %v", iterations)

	for i := 0; i < iterations; i++ {
		if nbPoints+index >= len(*points) {
			stroke.points = (*points)[index:]
			// log.Printf("[Drawing] nb Points in payload (final) : %v", len(stroke.points))
			*payloads = append(*payloads, stroke.Marshall())
			break
		}

		stroke.points = (*points)[index : index+nbPoints]

		// log.Printf("[Drawing] nb Points in payload : %v", len(stroke.points))
		// log.Println(stroke.points)

		*payloads = append(*payloads, stroke.Marshall())
		index += nbPoints
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
