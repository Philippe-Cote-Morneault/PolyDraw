namespace ClientLourd.Utilities.Enums 
{
    public enum SocketMessageTypes
    {
        ServerConnection = 0,
        ServerDisconnection = 1,
        UserDisconnected =2 ,
        MessageSent = 20,
        MessageReceived = 21,
        JoinChannel = 22,
        UserJoinedChannel = 23,
        LeaveChannel = 24,
        UserLeftChannel = 25,
        CreateChannel = 26,
    }
}