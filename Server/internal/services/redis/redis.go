package redisservice

import (
	"github.com/go-redis/redis/v7"
	"github.com/spf13/viper"
	"github.com/tevino/abool"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/model"
	"log"
	"syscall"
	"time"
)

//Logger service used to debug the message received by the server
type RedisService struct {
	shutdown abool.AtomicBool
	wait     chan bool
	client   *redis.Client
}

//Init the messenger service
func (r *RedisService) Init() {
	r.shutdown.SetTo(false)
	r.wait = make(chan bool)
}

//Start the messenger service
func (r *RedisService) Start() {
	log.Println("[Redis] -> Starting service")
	r.client = model.Redis()
	r.client = redis.NewClient(&redis.Options{
		Addr:     viper.GetString("redis.address"),
		Password: viper.GetString("redis.password"),
		DB:       viper.GetInt("redis.database"),
	})
	go r.run()
}

//Shutdown the messenger service
func (r *RedisService) Shutdown() {
	log.Println("[Redis] -> Closing service")
	r.shutdown.SetTo(true)
	r.client.Close()
	<-r.wait

}

//Register register any broadcast not used
func (r *RedisService) Register() {

}

func (r *RedisService) run() {
	defer service.Closed()

	count := 0
	for {
		if r.shutdown.IsSet() {
			r.wait <- true
			return
		}

		//Check for ping
		pong, err := r.client.Ping().Result()
		if (err != nil || pong != "PONG") && !r.shutdown.IsSet() {
			log.Println("[Redis] -> Disconnected possibly trying again.")
			count++
		} else {
			count = 0
			log.Println("[Redis] -> PONG")
		}

		if count > 3 {
			log.Println("[Redis] -> Did not respond 4 times. Closing server")
			syscall.Kill(syscall.Getpid(), syscall.SIGTERM)

			r.wait <- true
			return
		}
		time.Sleep(2 * time.Second)
	}

}
