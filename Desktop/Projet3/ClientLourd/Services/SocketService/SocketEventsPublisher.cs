using System;

namespace ClientLourd.Services.SocketService
{
    public class SocketEventsPublisher
    {
        public delegate void SocketEventHandler(object source, EventArgs args);
        
        //General event
        public event SocketEventHandler StartWaiting;
        public event SocketEventHandler StopWaiting;
        public event SocketEventHandler ConnectionResponse;
        public event SocketEventHandler ServerMessage;
        public event SocketEventHandler ServerDisconnected;
        public event SocketEventHandler HealthCheck;
        public event SocketEventHandler ConnectionLost;
        
        protected virtual void OnServerDisconnected(object source)
        {
            ServerDisconnected?.Invoke(source, EventArgs.Empty);
        }
        protected virtual void OnServerMessage(object source, EventArgs e)
        {
            ServerMessage?.Invoke(source, e);
        }
        protected virtual void OnConnectionLost(object source)
        {
            ConnectionLost?.Invoke(source, EventArgs.Empty);
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
        
        //Chat relate event
        public event SocketEventHandler MessageReceived;
        public event SocketEventHandler UserJoinedChannel;
        public event SocketEventHandler UserLeftChannel;
        public event SocketEventHandler UserCreatedChannel;
        public event SocketEventHandler UserDeletedChannel;
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


        protected virtual void OnUserJoinedChannel(object source, EventArgs e)
        {
            UserJoinedChannel?.Invoke(source, e);
        }

        protected virtual void OnMessageReceived(object source, EventArgs e)
        {
            MessageReceived?.Invoke(source, e);
        }
        
        
        //Canvas relate message
        public event SocketEventHandler ServerStrokeSent;
        public event SocketEventHandler ServerStartsDrawing;
        public event SocketEventHandler ServerEndsDrawing;
        public event SocketEventHandler DrawingPreviewResponse;
        



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
        
        
        //Match relate event
        public event SocketEventHandler MatchReadyToStart;
        public event SocketEventHandler PlayerLeftMatch;
        public event SocketEventHandler NewPlayerIsDrawing;
        public event SocketEventHandler YourTurnToDraw;
        public event SocketEventHandler MatchTimesUp;
        public event SocketEventHandler MatchSync;
        public event SocketEventHandler GuessResponse;
        public event SocketEventHandler PlayerGuessedTheWord;
        public event SocketEventHandler MatchCheckPoint;
        public event SocketEventHandler MatchEnded;
        public event SocketEventHandler MatchStarted;


        protected virtual void OnMatchStarted(object source)
        {
            MatchStarted?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnMatchEnded(object source, EventArgs e)
        {
            MatchEnded?.Invoke(source, e);
        }

        protected virtual void OnMatchReadyToStart(object source, EventArgs e)
        {
            MatchReadyToStart?.Invoke(source, e);
        }

        protected virtual void OnMatchCheckPoint(object source, EventArgs e)
        {
            MatchCheckPoint?.Invoke(source, e);
        }

        protected virtual void OnPlayerLeftMatch(object source, EventArgs e)
        {
            PlayerLeftMatch?.Invoke(source, e);
        }

        protected virtual void OnNewPlayerIsDrawing(object source, EventArgs e)
        {
            NewPlayerIsDrawing?.Invoke(source, e);
        }

        protected virtual void OnYourTurnToDraw(object source, EventArgs e)
        {
            YourTurnToDraw?.Invoke(source, e);
        }

        protected virtual void OnMatchTimesUp(object source, EventArgs e)
        {
            MatchTimesUp?.Invoke(source, e);
        }

        protected virtual void OnMatchSync(object source, EventArgs e)
        {
            MatchSync?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnGuessResponse(object source, EventArgs e)
        {
            GuessResponse?.Invoke(source, EventArgs.Empty);
        }

        protected virtual void OnPlayerGuessedTheWord(object source, EventArgs e)
        {
            PlayerGuessedTheWord?.Invoke(source, EventArgs.Empty);
        }
    }
}