package lobby

import (
	"log"

	"gitlab.com/jigsawcorp/log3900/internal/services/auth"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"

	service "gitlab.com/jigsawcorp/log3900/internal/services"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"
)

//Lobby service used the manage the groups before the match
type Lobby struct {
	connected cbroadcast.Channel
	close     cbroadcast.Channel

	join       cbroadcast.Channel
	leave      cbroadcast.Channel
	startMatch cbroadcast.Channel
	kick       cbroadcast.Channel
	addbot     cbroadcast.Channel

	groups   *groups
	shutdown chan bool
}

//Init the lobby service
func (l *Lobby) Init() {
	instance = l
	l.shutdown = make(chan bool)
	l.groups = &groups{}
	l.groups.Init()

	l.subscribe()
}

//Start the lobby service
func (l *Lobby) Start() {
	log.Println("[Lobby] -> Starting service")
	go l.listen()
	//TODO include a cleanup for unused groups after x minutes
}

//Shutdown the lobby service
func (l *Lobby) Shutdown() {
	log.Println("[Lobby] -> Closing service")
	l.groups.CleanAllGroups()
	log.Println("[Lobby] -> Remaining groups closed")
	close(l.shutdown)
}

//CreateGroup method used to broadcast a message that a new group was created
func (l *Lobby) CreateGroup(group *model.Group) {
	l.groups.AddGroup(group)
	socketID, err := auth.GetSocketID(group.OwnerID)

	if err == nil {
		l.groups.JoinGroup(socketID, group.ID)
		log.Printf("[Lobby] -> New group created %s", group.Name)
	}
}

func (l *Lobby) listen() {
	defer service.Closed()

	for {
		select {
		case id := <-l.connected:
			log.Printf("[Lobby] -> New session id: %s", id)
			l.groups.RegisterSession(id.(uuid.UUID))

		case id := <-l.close:
			log.Printf("[Lobby] -> Session disconnected id: %s", id)
			socketID := id.(uuid.UUID)
			l.groups.QuitGroup(socketID)
			l.groups.UnRegisterSession(socketID)

		case message := <-l.join:
			rawMessage := message.(socket.RawMessageReceived)
			groupID, err := uuid.FromBytes(rawMessage.Payload.Bytes)
			if err == nil {
				l.groups.JoinGroup(rawMessage.SocketID, groupID)
			} else {
				socket.SendErrorToSocketID(socket.MessageType.RequestJoinGroup, 400, "The uuid is invalid", rawMessage.SocketID)
			}

		case message := <-l.addbot:
			rawMessage := message.(socket.RawMessageReceived)
			groupID, err := uuid.FromBytes(rawMessage.Payload.Bytes)
			if err == nil {
				l.groups.AddBot(rawMessage.SocketID, groupID)
			} else {
				socket.SendErrorToSocketID(socket.MessageType.RequestJoinGroup, 400, "The uuid is invalid", rawMessage.SocketID)
			}

		case message := <-l.leave:
			rawMessage := message.(socket.RawMessageReceived)
			l.groups.QuitGroup(rawMessage.SocketID)

		case message := <-l.kick:
			rawMessage := message.(socket.RawMessageReceived)
			userID, err := uuid.FromBytes(rawMessage.Payload.Bytes)
			if err == nil {
				l.groups.KickUser(rawMessage.SocketID, userID)
			} else {
				socket.SendErrorToSocketID(socket.MessageType.RequestKickUser, 400, "The uuid is invalid", rawMessage.SocketID)
			}

		case message := <-l.startMatch:
			rawMessage := message.(socket.RawMessageReceived)
			l.groups.StartMatch(rawMessage.SocketID)

		case <-l.shutdown:
			return
		}
	}
}

func (l *Lobby) subscribe() {
	l.connected, _, _ = cbroadcast.Subscribe(socket.BSocketAuthConnected)
	l.close, _, _ = cbroadcast.Subscribe(socket.BSocketAuthCloseClient)
	l.join, _, _ = cbroadcast.Subscribe(BJoinGroup)
	l.leave, _, _ = cbroadcast.Subscribe(BLeaveGroup)
	l.startMatch, _, _ = cbroadcast.Subscribe(BStartMatch)
	l.kick, _, _ = cbroadcast.Subscribe(BKickUser)
	l.addbot, _, _ = cbroadcast.Subscribe(BAddBot)
}
