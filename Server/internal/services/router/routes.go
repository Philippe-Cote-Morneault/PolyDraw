package router

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/healthcheck"
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
)

func (r *Router) routing() {
	r.handle(socket.MessageType.MessageSent, messenger.BMessageSent)
	r.handle(socket.MessageType.HealthCheckResponse, healthcheck.BReceived)
}
