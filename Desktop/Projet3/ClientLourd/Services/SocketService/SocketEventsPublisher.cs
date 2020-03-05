using System;

namespace ClientLourd.Services.SocketService
{
    public class SocketEventsPublisher
    {
        public delegate void SocketEventHandler(object source, EventArgs args);

        public event SocketEventHandler ConnectionResponse;
        public event SocketEventHandler ServerMessage;
        public event SocketEventHandler ServerDisconnected;
        public event SocketEventHandler MessageReceived;
        public event SocketEventHandler UserJoinedChannel;
        public event SocketEventHandler UserLeftChannel;
        public event SocketEventHandler UserCreatedChannel;
        public event SocketEventHandler UserDeletedChannel;
        public event SocketEventHandler HealthCheck;
        public event SocketEventHandler ConnectionLost;
        public event SocketEventHandler StartWaiting;
        public event SocketEventHandler StopWaiting;
        public event SocketEventHandler ServerStrokeSent;
        public event SocketEventHandler ServerStartsDrawing;
        public event SocketEventHandler ServerEndsDrawing;
        public event SocketEventHandler DrawingPreviewResponse;
        public event SocketEventHandler LobbyCreated;
        public event SocketEventHandler JoinLobbyResponse;
        public event SocketEventHandler UserJoinedLobby;
        public event SocketEventHandler UserLeftLobby;

        protected virtual void OnServerMessage(object source, EventArgs e)
        {
            ServerMessage?.Invoke(source, e);
        }
        protected virtual void OnConnectionLost(object source)
        {
            ConnectionLost?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnUserDeletedChannel(object source, EventArgs e)
        {
            UserDeletedChannel?.Invoke(source, e);
        }
        protected virtual void OnUserCreatedChannel(object source, EventArgs e)
        {
            UserCreatedChannel?.Invoke(source, e);
        }

        protected virtual void OnUserLeftChannel(object source, EventArgs e)
        {
            UserLeftChannel?.Invoke(source, e);
        }

        protected virtual void OnServerDisconnected(object source)
        {
            ServerDisconnected?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnUserJoinedChannel(object source, EventArgs e)
        {
            UserJoinedChannel?.Invoke(source, e);
        }

        protected virtual void OnMessageReceived(object source, EventArgs e)
        {
            MessageReceived?.Invoke(source, e);
        }

        protected virtual void OnConnectionResponse(object source)
        {
            ConnectionResponse?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnHealthCheck(object source)
        {
            HealthCheck?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnStartWaiting(object source)
        {
            StartWaiting?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnStopWaiting(object source)
        {
            StopWaiting?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnServerStrokeSent(object source, EventArgs e)
        {
            ServerStrokeSent?.Invoke(source, e);
        }

        protected virtual void OnServerStartsDrawing(object source, EventArgs e)
        {
            ServerStartsDrawing?.Invoke(source, e);
        }

        protected virtual void OnServerEndsDrawing(object source, EventArgs e)
        {
            ServerEndsDrawing?.Invoke(source, e);
        }


        protected virtual void OnDrawingPreviewResponse(object source, EventArgs e)
        {
            DrawingPreviewResponse?.Invoke(source, e);
        }

        protected virtual void OnLobbyCreated(object source, EventArgs e)
        {
            LobbyCreated?.Invoke(source, e);
        }

        protected virtual void OnJoinLobbyResponse(object source, EventArgs e)
        {
            JoinLobbyResponse?.Invoke(source, e);
        }

        protected virtual void OnUserJoinedLobby(object source, EventArgs e)
        {
            UserJoinedLobby?.Invoke(source, e);
        }

        protected virtual void OnLeftLobby(object source, EventArgs e)
        {
            UserLeftLobby?.Invoke(source, e);
        }
    }
}