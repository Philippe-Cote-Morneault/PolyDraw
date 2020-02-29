package geometry

import (
	"fmt"
	"testing"
)

func BenchmarkBezierLength(b *testing.B) {
	InitTable()
	start := Point{
		X: 0,
		Y: 0,
	}
	c1 := Point{
		X: -400,
		Y: -400,
	}
	c2 := Point{
		X: 200,
		Y: 200,
	}
	end := Point{
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
