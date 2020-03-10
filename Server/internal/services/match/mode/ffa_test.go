package mode

import (
	"github.com/docker/docker/pkg/testutil/assert"
	"github.com/google/uuid"
	"gitlab.com/jigsawcorp/log3900/model"
	"testing"
)

func TestFFA_SetOrder(t *testing.T) {
	for i := 0; i < 5000; i++ {
		ffa := FFA{}
		group := model.Group{}
		connections := make([]uuid.UUID, 0)
		for i := 0; i < 10; i++ {
			connections = append(connections, uuid.New())
		}
		ffa.Init(connections, group)
		ffa.SetOrder()
	}
}

func TestFFA_GetConnections(t *testing.T) {
	ffa := FFA{}
	group := model.Group{}
	connections := make([]uuid.UUID, 0)
	for i := 0; i < 10; i++ {
		connections = append(connections, uuid.New())
	}
	ffa.Init(connections, group)
	connectionsReturned := ffa.GetConnections()
	for i := range connections {
		assert.Equal(t, connections[i], connectionsReturned[i])
	}
}
