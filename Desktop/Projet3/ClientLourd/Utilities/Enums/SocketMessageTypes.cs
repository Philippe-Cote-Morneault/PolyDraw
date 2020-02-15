namespace ClientLourd.Utilities.Enums
{
    public enum SocketMessageTypes
    {
        ServerConnection = 0,
        ServerConnectionResponse = 1,
        ServerDisconnection = 2,
        UserDisconnected = 3,
        HealthCheck = 9,
        HealthCheckResponse = 10,
        MessageSent = 20,
        MessageReceived = 21,
        JoinChannel = 22,
        UserJoinedChannel = 23,
        LeaveChannel = 24,
        UserLeftChannel = 25,
        CreateChannel = 26,
        UserCreatedChannel = 27,
        DeleteChannel = 28,
        UserDeletedChannel = 29,
        ServerMessage = 255,
    }
}