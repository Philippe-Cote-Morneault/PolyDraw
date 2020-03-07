﻿namespace ClientLourd.Utilities.Enums
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
        UserStrokeSent = 30,
        ServerStrokeSent = 31,
        StartDrawing = 32,
        ServerStartsDrawing = 33,
        EndDrawing = 34,
        ServerEndsDrawing = 35,
        DrawingPreviewRequest = 36,
        DrawingPreviewResponse = 37,
        DeleteStroke = 38, 
        UserDeletedStroke = 39,
        JoinLobbyRequest = 40,
        JoinLobbyResponse = 41,
        UserJoinedLobby = 43,
        QuitLobbyRequest = 44,
        QuitLobbyResponse = 45,
        StartGameRequest = 48,
        StartGameResponse = 49,
        LobbyCreated = 51,
        LobbyDeleted = 53,
        ReadyToStart = 62,
        MatchStarted = 63,
        LeaveMatch = 64,
        PlayerLeftMatch = 65,
        NewDrawer = 67,
        YourTurnToDraw = 69,
        TimesUp = 71,
        MatchSync = 73,
        GuessTheWord = 74,
        GuessResponse = 75,
        PlayerGuessed = 77,
        MatchCheckPoint = 79,
        MatchEnd = 81,
        ServerMessage = 255,
    }
}