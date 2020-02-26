package geometry

import "testing"
import "fmt"

func BenchmarkBezierLength(b *testing.B) {
	InitTable()
	start := Point{
		X: 0,
		Y: 0,
	}
	c1 := Point{
		X: 0,
		Y: 5,
	}
	c2 := Point{
		X: 0,
		Y: 10,
	}
	end := Point{
		X: 5,
		Y: 5,
	}
	b.ResetTimer()
	var value float64
	for n := 0; n < b.N; n++ {
		value = BezierLength(&start, &c1, &c2, &end)
	}
	b.StopTimer()
	fmt.Println(value)
}
