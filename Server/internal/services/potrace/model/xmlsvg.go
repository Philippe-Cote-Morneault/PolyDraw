package model

import (
	"encoding/xml"
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/svgparser"
)

//XMLSvg model XML
type XMLSvg struct {
	XMLName xml.Name `xml:"svg"`
	G       XMLG     `xml:"g"`
	Version string   `xml:"version,attr"`
	Width   string   `xml:"width,attr"`
	Height  string   `xml:"height,attr"`
	ViewBox string   `xml:"viewBox,attr"`
	Xmlns   string   `xml:"xmlns,attr"`
}

//XMLG model G
type XMLG struct {
	Transform string    `xml:"transform,attr"`
	Style     string    `xml:"style,attr"`
	XMLPaths  []XMLPath `xml:"path"`
}

//XMLPath model path
type XMLPath struct {
	D         string    `xml:"d,attr"`
	Time      int       `xml:"http://example.org/polydraw time,attr"`
	Order     int       `xml:"http://example.org/polydraw order,attr"`
	Color     int       `xml:"http://example.org/polydraw color,attr"`
	Eraser    bool      `xml:"http://example.org/polydraw eraser,attr"`
	Brush     string    `xml:"http://example.org/polydraw brush,attr"`
	BrushSize int       `xml:"http://example.org/polydraw brushsize,attr"`
	ID        uuid.UUID `xml:"id,attr"`

	FirstCommand svgparser.Command `xml:"-"`
	Length       float64           `xml:"-"`
}
