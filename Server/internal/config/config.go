package config

import (
	"log"

	"github.com/spf13/viper"
)

//Init set the configuration and all the defaults
func Init() {
	setupFile()
	setupDefault()
}

func setupFile() {
	log.Println("Loading the configuration file")

	viper.SetConfigName("config")
	viper.SetConfigType("yaml")
	viper.AddConfigPath("/etc/polydraw/")
	viper.AddConfigPath("$HOME/.polydraw")
	viper.AddConfigPath(".")

	if err := viper.ReadInConfig(); err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); ok {
			panic("Error: The config file was not found. Make sure that a config file is available")
		} else {
			// Config file was found but another error was produced
			panic(err)
		}
	}
}

func setupDefault() {
	viper.SetDefault("database.connection", "nil")
	viper.SetDefault("database.engine", "nil")
	viper.SetDefault("redis.address", "nil")
	viper.SetDefault("redis.password", "")
	viper.SetDefault("redis.database", 0)

	viper.SetDefault("datastore", "/tmp/images")
	viper.SetDefault("rest.port", "3000")
	viper.SetDefault("rest.address", "127.0.0.1")
	viper.SetDefault("socket.port", "3001")
	viper.SetDefault("socket.address", "127.0.0.1")

}
