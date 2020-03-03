package lobby

import (
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
	"sync"
)

type responseJoinGroup struct {
	Response bool
	Error    string
}

type responseQuitGroup struct {
	UserID   string
	Username string
	GroupID  string
}

type groups struct {
	mutex      sync.Mutex
	queue      map[uuid.UUID]bool
	assignment map[uuid.UUID]uuid.UUID //socketID -> groupID
	groups     map[uuid.UUID][]uuid.UUID
}

func (g *groups) Init() {
	g.assignment = make(map[uuid.UUID]uuid.UUID)
	g.queue = make(map[uuid.UUID]bool)
}

//RegisterSession used to register a session in the queue to be added to a groups
func (g *groups) RegisterSession(socketID uuid.UUID) {
	defer g.mutex.Unlock()

	g.mutex.Lock()
	g.queue[socketID] = true
}

//UnRegisterSession used to remove the user from the groups or the waiting list
func (g *groups) UnRegisterSession(socketID uuid.UUID) {
	defer g.mutex.Unlock()
	g.mutex.Lock()

	delete(g.queue, socketID)
	if groupID, ok := g.assignment[socketID]; ok {
		delete(g.assignment, socketID)
		g.removeSocketGroup(socketID, groupID)

		userID, err := auth.GetUserID(socketID)

		var groupDB model.Group
		model.DB().Where("id = ?", groupID).First(&groupDB)
		if groupDB.ID != uuid.Nil && err == nil {
			model.DB().Model(&groupDB).Association("Users").Delete(&model.User{Base: model.Base{ID: userID}})
		}
	}
}

//AddGroup used to add a user to the groups can be called in rest that's why we can avoid the db operation
func (g *groups) AddGroup(socketID uuid.UUID, groupID uuid.UUID, addToBD bool) {
	g.mutex.Lock()
	if _, ok := g.queue[socketID]; ok {
		//Check if groups exists
		groupDB := model.Group{}
		model.DB().Where("id = ? and status = 0", groupID).First(&groupDB)
		if groupDB.ID != uuid.Nil {
			delete(g.queue, socketID)
			g.assignment[socketID] = groupID
			g.groups[groupID] = append(g.groups[groupID], socketID)
			g.mutex.Unlock()

			//send response to client
			message := socket.RawMessage{}
			message.ParseMessagePack(byte(socket.MessageType.ResponseJoinGroup), responseJoinGroup{
				Response: true,
				Error:    "",
			})

			if socket.SendRawMessageToSocketID(message, socketID) == nil && addToBD {
				//We only commit the data to the db if the message was sent successfully
				//else we will handle the error in the disconnect message
				userID, _ := auth.GetUserID(socketID)
				model.DB().Model(&groupDB).Association("Users").Append(&model.User{Base: model.Base{ID: userID}})
			}
			return

		}
		g.mutex.Unlock()

		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseJoinGroup), responseJoinGroup{
			Response: false,
			Error:    "The groups could not be found.",
		})
		socket.SendRawMessageToSocketID(message, socketID)
	} else {
		g.mutex.Unlock()

		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseJoinGroup), responseJoinGroup{
			Response: false,
			Error:    "The user can only join one groups",
		})
		socket.SendRawMessageToSocketID(message, socketID)
	}
}

//QuitGroup quits the groups the user is currently in.
func (g *groups) QuitGroup(socketID uuid.UUID) {
	defer g.mutex.Unlock()

	g.mutex.Lock()
	if _, ok := g.assignment[socketID]; ok {
		groupID := g.assignment[socketID]

		delete(g.assignment, socketID)
		g.removeSocketGroup(socketID, groupID)
		g.queue[socketID] = true

		//Send a message to the groups member to advertise that the user quit the groups
		user, _ := auth.GetUser(socketID)
		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseLeaveGroup), responseQuitGroup{
			UserID:   user.ID.String(),
			Username: user.Username,
			GroupID:  groupID.String(),
		})
		socket.SendRawMessageToSocketID(message, socketID) //We inform the user that the request was received.
		for i := range g.groups[groupID] {
			go socket.SendRawMessageToSocketID(message, g.groups[groupID][i])
		}
		g.mutex.Unlock()

		var groupDB model.Group
		model.DB().Where("id = ?", groupID).First(&groupDB)
		model.DB().Model(&groupDB).Association("Users").Delete(&user)
	} else {
		g.mutex.Unlock()
		go socket.SendErrorToSocketID(44, 404, "The user does not belong to this groups", socketID)
	}
}

func (g *groups) removeSocketGroup(socketID uuid.UUID, groupID uuid.UUID) {
	for i := range g.groups[groupID] {
		if g.groups[groupID][i] == socketID {
			last := len(g.groups[groupID]) - 1

			g.groups[groupID][i] = g.groups[groupID][last]
			g.groups[groupID][last] = uuid.Nil
			g.groups[groupID] = g.groups[groupID][:last]
			return
		}
	}
}
