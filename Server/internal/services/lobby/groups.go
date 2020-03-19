package lobby

import (
	"gitlab.com/jigsawcorp/log3900/internal/services/messenger"
	"sync"

	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/internal/services/auth"
	"gitlab.com/jigsawcorp/log3900/internal/services/match"
	"gitlab.com/jigsawcorp/log3900/internal/socket"
	"gitlab.com/jigsawcorp/log3900/model"
)

type responseGen struct {
	Response bool
	Error    string
}

type responseGroup struct {
	UserID   string
	Username string
	GroupID  string
	IsCPU    bool
	IsKicked bool
}

type responsePlayer struct {
	IsCPU    bool
	ID       string
	Username string
}

type responseNewGroup struct {
	ID         string
	GroupName  string
	OwnerName  string
	OwnerID    string
	PlayersMax int
	Mode       int
	Players    []responsePlayer
	Language   int
	Difficulty int
}

type groups struct {
	mutex      sync.Mutex
	queue      map[uuid.UUID]bool
	assignment map[uuid.UUID]uuid.UUID //socketID -> groupID
	groups     map[uuid.UUID][]uuid.UUID
}

func (g *groups) Init() {
	g.assignment = make(map[uuid.UUID]uuid.UUID)
	g.groups = make(map[uuid.UUID][]uuid.UUID)
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
			messenger.HandleQuitGroup(&groupDB, socketID)
			//If user is owner we delete the group
			if groupDB.OwnerID == userID {
				g.safeDeleteGroup(&groupDB)
			}
		}
	}
}

func (g *groups) KickUser(socketID uuid.UUID, userID uuid.UUID) {
	g.mutex.Lock()
	//Find the group and find the user socket id
	if groupID, ok := g.assignment[socketID]; ok {
		var groupDB model.Group
		model.DB().Where("id = ?", groupID).First(&groupDB)
		if groupDB.ID != uuid.Nil {
			//Make sure that we are the owner
			currentUserID, _ := auth.GetUserID(socketID)
			if groupDB.OwnerID == currentUserID {
				//TODO handle virtual players
				socketKickUser, err := auth.GetSocketID(userID)
				if err == nil {
					g.mutex.Unlock()
					g.QuitGroup(socketKickUser, true)
				} else {
					g.mutex.Unlock()
					go socket.SendErrorToSocketID(socket.MessageType.RequestKickUser, 404, "Cannot find the user", socketID)
				}
			} else {
				g.mutex.Unlock()
				go socket.SendErrorToSocketID(socket.MessageType.RequestKickUser, 400, "Only the group owner can kick people out", socketID)
			}
		} else {
			g.mutex.Unlock()
			go socket.SendErrorToSocketID(socket.MessageType.RequestKickUser, 404, "The group could not be found", socketID)
		}
	} else {
		g.mutex.Unlock()
		go socket.SendErrorToSocketID(socket.MessageType.RequestKickUser, 400, "The user does not belong to a group", socketID)
	}
}

//AddGroup add the group and send the message to all the users in queue
func (g *groups) AddGroup(group *model.Group) {
	defer g.mutex.Unlock()

	message := socket.RawMessage{}
	players := []responsePlayer{
		{
			IsCPU:    false,
			ID:       group.OwnerID.String(),
			Username: group.Owner.Username,
		},
	}
	message.ParseMessagePack(byte(socket.MessageType.ResponseGroupCreated), responseNewGroup{
		ID:         group.ID.String(),
		GroupName:  group.Name,
		OwnerName:  group.Owner.Username,
		OwnerID:    group.OwnerID.String(),
		PlayersMax: group.PlayersMax,
		Mode:       group.GameType,
		Players:    players,
		Language:   group.Language,
		Difficulty: group.Difficulty,
	})
	messenger.RegisterGroup(group)
	g.groups[group.ID] = make([]uuid.UUID, 0, 4)
	//TODO only if not solo
	g.mutex.Lock()
	for k := range g.queue {
		go socket.SendRawMessageToSocketID(message, k)
	}
}

//JoinGroup used to add a user to the groups can be called in rest that's why we can avoid the db operation
func (g *groups) JoinGroup(socketID uuid.UUID, groupID uuid.UUID) {
	g.mutex.Lock()
	if _, ok := g.queue[socketID]; ok {
		//Check if groups exists
		groupDB := model.Group{}
		model.DB().Where("id = ? and status = 0", groupID).First(&groupDB)
		if groupDB.ID != uuid.Nil {

			//Is the group full ?
			if groupDB.PlayersMax-len(g.groups[groupID]) > 0 {
				delete(g.queue, socketID)
				g.assignment[socketID] = groupID

				g.groups[groupID] = append(g.groups[groupID], socketID)
				g.mutex.Unlock()

				//send response to client
				message := socket.RawMessage{}
				message.ParseMessagePack(byte(socket.MessageType.ResponseJoinGroup), responseGen{
					Response: true,
					Error:    "",
				})

				if socket.SendRawMessageToSocketID(message, socketID) == nil {
					//We only commit the data to the db if the message was sent successfully
					//else we will handle the error in the disconnect message
					user, _ := auth.GetUser(socketID)
					model.DB().Model(&groupDB).Association("Users").Append(&model.User{Base: model.Base{ID: user.ID}})

					//Send a message to all the member of the group to advertise that a new user is in the group
					newUser := socket.RawMessage{}
					newUser.ParseMessagePack(byte(socket.MessageType.UserJoinedGroup), responseGroup{
						UserID:   user.ID.String(),
						Username: user.Username,
						GroupID:  groupID.String(),
						IsCPU:    false,
					})
					g.mutex.Lock()
					for i := range g.groups[groupID] {
						go socket.SendRawMessageToSocketID(newUser, g.groups[groupID][i])
					}
					for k := range g.queue {
						go socket.SendRawMessageToSocketID(newUser, k)
					}
					g.mutex.Unlock()

					messenger.HandleJoinGroup(&groupDB, socketID)

				}
				return
			}
			g.mutex.Unlock()
			message := socket.RawMessage{}
			message.ParseMessagePack(byte(socket.MessageType.ResponseJoinGroup), responseGen{
				Response: false,
				Error:    "The group is full",
			})
			socket.SendRawMessageToSocketID(message, socketID)
			return

		}
		g.mutex.Unlock()

		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseJoinGroup), responseGen{
			Response: false,
			Error:    "The group could not be found.",
		})
		socket.SendRawMessageToSocketID(message, socketID)
	} else {
		g.mutex.Unlock()

		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseJoinGroup), responseGen{
			Response: false,
			Error:    "The user can only join one group",
		})
		socket.SendRawMessageToSocketID(message, socketID)
	}
}

//QuitGroup quits the groups the user is currently in.
func (g *groups) QuitGroup(socketID uuid.UUID, forced bool) {
	g.mutex.Lock()
	if _, ok := g.assignment[socketID]; ok {
		groupID := g.assignment[socketID]

		delete(g.assignment, socketID)
		g.removeSocketGroup(socketID, groupID)
		g.queue[socketID] = true

		//Send a message to the groups member to advertise that the user quit the groups
		user, _ := auth.GetUser(socketID)
		message := socket.RawMessage{}
		message.ParseMessagePack(byte(socket.MessageType.ResponseLeaveGroup), responseGroup{
			UserID:   user.ID.String(),
			Username: user.Username,
			GroupID:  groupID.String(),
			IsKicked: forced,
			IsCPU:    false,
		})
		for i := range g.groups[groupID] {
			go socket.SendRawMessageToSocketID(message, g.groups[groupID][i])
		}
		for k := range g.queue {
			go socket.SendRawMessageToSocketID(message, k)
		}
		g.mutex.Unlock()

		var groupDB model.Group
		model.DB().Where("id = ?", groupID).First(&groupDB)
		model.DB().Model(&groupDB).Association("Users").Delete(&user)

		messenger.HandleQuitGroup(&groupDB, socketID)

		g.mutex.Lock()
		if user.ID == groupDB.OwnerID {
			//The owner has left the group
			g.safeDeleteGroup(&groupDB)
		}
		g.mutex.Unlock()

	} else {
		g.mutex.Unlock()
		go socket.SendErrorToSocketID(44, 404, "The user does not belong to this group", socketID)
	}
}

//Set all groups in the database who are set to status waiting to abandoned
func (g *groups) CleanAllGroups() {
	var groups []model.Group
	model.DB().Model(&groups).Where("status = 0").Find(&groups)
	for i := range groups {
		groups[i].Status = 3
		model.DB().Save(groups[i])
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

func (g *groups) safeDeleteGroup(groupDB *model.Group) {
	//We remove the group
	uuidBytes, _ := groupDB.ID.MarshalBinary()
	message := socket.RawMessage{
		MessageType: byte(socket.MessageType.ResponseGroupRemoved),
		Length:      uint16(len(uuidBytes)),
		Bytes:       uuidBytes,
	}
	//Broadcast a message to all the users in queue
	for k := range g.queue {
		go socket.SendRawMessageToSocketID(message, k)
	}

	//Broadcast a message that the group was deleted and remove them from the group
	for _, v := range g.groups[groupDB.ID] {
		go socket.SendRawMessageToSocketID(message, v)
	}

	messenger.UnRegisterGroup(groupDB, g.groups[groupDB.ID])
	//Remove all the data associated with the groups
	for _, v := range g.groups[groupDB.ID] {
		delete(g.assignment, v)
		g.queue[v] = true
	}
	delete(g.groups, groupDB.ID)

	groupDB.Status = 3
	model.DB().Save(&groupDB)
}

//StartMatch method used to create the match
func (g *groups) StartMatch(socketID uuid.UUID) {

	g.mutex.Lock()
	groupID, hasGroup := g.assignment[socketID]
	g.mutex.Unlock()

	if hasGroup {
		var groupDB model.Group
		model.DB().Model(&groupDB).Related(&model.User{}, "Users")
		model.DB().Preload("Users").Where("id = ?", groupID).First(&groupDB)

		userID, _ := auth.GetUserID(socketID)
		//Start only if the owner
		if groupDB.OwnerID == userID {
			//Check if there are enough people
			g.mutex.Lock()
			count := len(g.groups[groupID])
			g.mutex.Unlock()

			//TODO make a check for solo
			if count > 1 {
				//We send the response and we pass it along to the match service
				rawMessage := socket.RawMessage{}
				rawMessage.ParseMessagePack(byte(socket.MessageType.ResponseGameStart), responseGen{
					Response: true,
				})
				for _, v := range g.groups[groupDB.ID] {
					go socket.SendRawMessageToSocketID(rawMessage, v)
				}
				uuidBytes, _ := groupDB.ID.MarshalBinary()
				message := socket.RawMessage{
					MessageType: byte(socket.MessageType.ResponseGroupRemoved),
					Length:      uint16(len(uuidBytes)),
					Bytes:       uuidBytes,
				}

				g.mutex.Lock()
				//Broadcast a message to all the users in queue
				for k := range g.queue {
					go socket.SendRawMessageToSocketID(message, k)
				}
				connections := g.groups[groupID][:]
				g.mutex.Unlock()

				match.UpgradeGroup(groupID, connections, groupDB)
				groupDB.Status = 1
				model.DB().Save(&groupDB)

				//change status and put all the users in the queue once they quit the game
				//Remove all the data associated with the groups
				g.mutex.Lock()
				for _, v := range g.groups[groupDB.ID] {
					delete(g.assignment, v)
					g.queue[v] = true
				}
				delete(g.groups, groupDB.ID)
				g.mutex.Unlock()
			} else {
				rawMessage := socket.RawMessage{}
				rawMessage.ParseMessagePack(byte(socket.MessageType.ResponseGameStart), responseGen{
					Response: false,
					Error:    "There are not enough users to start the game.",
				})
				socket.SendRawMessageToSocketID(rawMessage, socketID)
			}

		} else {
			rawMessage := socket.RawMessage{}
			rawMessage.ParseMessagePack(byte(socket.MessageType.ResponseGameStart), responseGen{
				Response: false,
				Error:    "Only the owner can request the game to start.",
			})
			socket.SendRawMessageToSocketID(rawMessage, socketID)
		}
	} else {
		rawMessage := socket.RawMessage{}
		rawMessage.ParseMessagePack(byte(socket.MessageType.ResponseGameStart), responseGen{
			Response: false,
			Error:    "The user is not associated with any group.",
		})
		socket.SendRawMessageToSocketID(rawMessage, socketID)
	}
}
