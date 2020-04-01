package main

import (
	"fmt"
	"time"

	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

func main() {
	cbroadcast.Register("test:a", 10)
	cbroadcast.Register("test:b", 10)

	go func() {
		//Slow thread
		a, _ := cbroadcast.Subscribe("test:a")
		time.Sleep(time.Second * 1)
		for {
			text := <-a
			fmt.Printf("Slow thread %s\n", text)
			time.Sleep(time.Millisecond * 200)

			cbroadcast.Broadcast("test:b", "Done slow")
		}
	}()

	for i := 0; i < 2; i++ {
		go func(i int) {
			a, c1 := cbroadcast.Subscribe("test:a")
			b, _ := cbroadcast.Subscribe("test:b")
			fmt.Printf("Started %d \n", i)
			for {
				select {
				case text := <-a:
					fmt.Printf("Thread %d channel a %s\n", i, text)
				case text := <-b:
					fmt.Printf("Thread %d channel b %s\n", i, text)
				case <-c1:
					fmt.Println("Closing received for a!")
				}
			}
		}(i)
	}
	go func() {
		time.Sleep(time.Millisecond * 100)
		for i := 0; i < 50; i++ {
			cbroadcast.Broadcast("test:a", fmt.Sprintf("%d", i))
		}

	}()

	go func() {
		time.Sleep(time.Millisecond * 100)
		for i := 0; i < 50; i++ {
			cbroadcast.Broadcast("test:b", fmt.Sprintf("%d", i))
		}

	}()

	time.Sleep(time.Second * 200)

}
