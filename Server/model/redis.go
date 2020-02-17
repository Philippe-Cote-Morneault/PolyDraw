package model

import "github.com/go-redis/redis/v7"

var redisClient redis.Client

func Redis() *redis.Client {
	return &redisClient
}
