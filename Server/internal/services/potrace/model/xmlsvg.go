package model

import (
	"encoding/xml"
	"github.com/google/uuid"
)

//XMLSvg model XML
type XMLSvg struct {
	XMLName       xml.Name `xml:"svg"`
	G             XMLG     `xml:"g"`
	Version       string   `xml:"version,attr"`
	Width         string   `xml:"width,attr"`
	Height        string   `xml:"height,attr"`
	ViewBox       string   `xml:"viewBox,attr"`
	Xmlns         string   `xml:"xmlns,attr"`
	XmlnsPolydraw string   `xml:"xmlns:polydraw,attr"`
}

//XMLG model G
type XMLG struct {
	Transform string    `xml:"transform,attr"`
	XMLPaths  []XMLPath `xml:"path"`
}

//XMLPath model path
type XMLPath struct {
	D         string    `xml:"d,attr"`
	Time      int       `xml:"polydraw:time,attr"`
	Order     int       `xml:"polydraw:order,attr"`
	Color     int       `xml:"polydraw:color,attr"`
	Eraser    bool      `xml:"polydraw:eraser,attr"`
	Brush     string    `xml:"polydraw:brush,attr"`
	BrushSize int       `xml:"polydraw:brushsize,attr"`
	ID        uuid.UUID `xml:"id,attr"`
}
