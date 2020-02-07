package healthcheck

import "gitlab.com/jigsawcorp/log3900/pkg/cbroadcast"

//BReceived broadcast message when the client responds to the healthcheck
const BReceived = "healthcheck:received"

//BSize buffer size for the healthcheck service
const BSize = 5

//Register the broadcast for healthcheck
func (m *HealthCheck) Register() {
	cbroadcast.Register(BReceived, BSize)
}
