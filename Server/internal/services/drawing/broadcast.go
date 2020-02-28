package drawing

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BPreview broadcast message when the client sends a new preview
const BPreview = "drawing:preview"

//BSize buffer size for the drawing service
const BSize = 5

//Register the broadcast for drawing
func (m *Drawing) Register() {
	cbroadcast.Register(BPreview, BSize)
}
