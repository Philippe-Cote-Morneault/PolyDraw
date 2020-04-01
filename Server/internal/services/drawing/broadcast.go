package drawing

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BPreview broadcast message when the client sends a new preview
const BPreview = "drawing:preview"

//BStopPreview broadcast message when the client sends a new preview
const BStopPreview = "drawing:stoppreview"

//BStrokeChunk message for the strokes received by the server
const BStrokeChunk = "drawing:strokeChunk"

//BDrawStart message to indicate that strokes messages will arrive
const BDrawStart = "drawing:start"

//BDrawEnd message to indicate that strokes messages will stop
const BDrawEnd = "drawing:end"

//BDrawErase message to indicate that a stroke will be erased
const BDrawErase = "drawing:erase"

//BSize buffer size for the drawing service
const BSize = 5

//Register the broadcast for drawing
func (m *Drawing) Register() {
	cbroadcast.Register(BPreview, BSize)
	cbroadcast.Register(BStopPreview, BSize)
	cbroadcast.Register(BStrokeChunk, BSize)
	cbroadcast.Register(BDrawStart, BSize)
	cbroadcast.Register(BDrawEnd, BSize)
	cbroadcast.Register(BDrawErase, BSize)
}
