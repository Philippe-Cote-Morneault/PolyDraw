package main

import (
	"fmt"
	"gitlab.com/jigsawcorp/log3900/internal/services/potrace"
	redisservice "gitlab.com/jigsawcorp/log3900/internal/services/redis"
	"gitlab.com/jigsawcorp/log3900/pkg/geometry"
	"log"

	"github.com/spf13/viper"
	"gitlab.com/jigsawcorp/log3900/internal/config"
	"gitlab.com/jigsawcorp/log3900/internal/rest"
	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/services/healthcheck"
	"gitlab.com/jigsawcorp/log3900/internal/services/logger"
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/internal/services/router"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"gitlab.com/jigsawcorp/log3900/pkg/graceful"
)

func main() {
	config.Init()
	model.DBConnect()
	geometry.InitTable()

	restServer := &rest.Server{}
	socketServer := &socket.Server{}
	socket.RegisterBroadcast()

	graceful.Register(restServer.Shutdown, "REST server")
	graceful.Register(socketServer.Shutdown, "Socket server")
	graceful.Register(service.ShutdownAll, "Services") //Shutdown all services
	graceful.Register(model.DBClose, "Database")

	handleGraceful := graceful.ListenSIG()

	registerServices()

	log.Printf("Server is starting jobs!")
	handleRest := make(chan bool)
	go func() {
		restServer.Initialize()
		restServer.Run(fmt.Sprintf("%s:%s", viper.GetString("rest.address"), viper.GetString("rest.port")))
		handleRest <- true
	}()

	handleSocket := make(chan bool)
	go func() {
		socketServer.StartListening(fmt.Sprintf("%s:%s", viper.GetString("socket.address"), viper.GetString("socket.port")))
		handleSocket <- true
	}()

	service.StartAll()

	<-handleRest
	<-handleSocket
	<-handleGraceful

}

func registerServices() {
	service.Add(&messenger.Messenger{})
	service.Add(&router.Router{})
	service.Add(&logger.Logger{})
	service.Add(&auth.Auth{})
	service.Add(&healthcheck.HealthCheck{})
	service.Add(&redisservice.RedisService{})
	service.Add(&potrace.Potrace{})
}
