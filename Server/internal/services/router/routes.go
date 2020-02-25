package router

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/healthcheck"
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
)

func (r *Router) routing() {
	//Messenger
	r.handle(socket.MessageType.MessageSent, messenger.BMessageSent)
	r.handle(socket.MessageType.CreateChannel, messenger.BCreateChannel)
	r.handle(socket.MessageType.DestroyChannel, messenger.BDestroyChannel)
	r.handle(socket.MessageType.JoinChannel, messenger.BJoinChannel)
	r.handle(socket.MessageType.LeaveChannel, messenger.BLeaveChannel)

	//Healthcheck
	r.handle(socket.MessageType.HealthCheckResponse, healthcheck.BReceived)
}
