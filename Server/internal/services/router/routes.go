package router

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
)

func (r *Router) routing() {
	r.handle(socket.MessageType.MessageSent, messenger.BMessageSent)
	//TODO include the authentification middleware
}
