package main

import (
	"fmt"
	"log"
	"sync"
	"time"
)

var out = make(chan int)
var wg sync.WaitGroup

func f(num int) {
	defer wg.Done()
	time.Sleep(time.Second)
	out <- num * 2
}

func main() {
	routines := 10000000
	defer duration(track("Real time"))
	for i := 0; i < routines; i++ {
		wg.Add(1)
		go f(i)
	}
	//wg.Wait()
	count := 0
	for i := 0; i < routines; i++ {
		<-out
		count++
	}
	fmt.Printf("Result count: %v\n", count)
}

func track(msg string) (string, time.Time) {
	return msg, time.Now()
}

func duration(msg string, start time.Time) {
	log.Printf("%v: %v\n", msg, time.Since(start))
}
