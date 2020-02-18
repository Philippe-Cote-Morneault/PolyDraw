package model

import (
	"github.com/go-redis/redis/v7"
	"github.com/spf13/viper"
)

var redisClient *redis.Client

//RedisInit starts the redis client
func RedisInit() {
	redisClient = redis.NewClient(&redis.Options{
		Addr:     viper.GetString("redis.address"),
		Password: viper.GetString("redis.password"),
		DB:       viper.GetInt("redis.database"),
	})
}

//Redis returns the Redis client object
func Redis() *redis.Client {
	return redisClient
}
