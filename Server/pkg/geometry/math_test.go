package geometry

import (
	"fmt"
	"testing"

	"gitlab.com/jigsawcorp/log3900/pkg/geometry/model"
)

func BenchmarkBezierLength(b *testing.B) {
	InitTable()
	start := model.Point{
		X: 0,
		Y: 0,
	}
	c1 := model.Point{
		X: -400,
		Y: -400,
	}
	c2 := model.Point{
		X: 200,
		Y: 200,
	}
	end := model.Point{
		X: 200,
		Y: 200,
	}
	b.ResetTimer()
	var value float64
	for n := 0; n < b.N; n++ {
		value = BezierLength(&start, &c1, &c2, &end)
	}
	b.StopTimer()
	fmt.Println(value)
}
