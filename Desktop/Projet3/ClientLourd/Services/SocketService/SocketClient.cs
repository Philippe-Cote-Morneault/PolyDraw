using System;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using ClientLourd.Models;
using MessagePack;
using System.Timers;
using System.Windows.Threading;
using ClientLourd.Models.NonBindable;
using ClientLourd.Utilities.Constants;
using ClientLourd.Utilities.Enums;
using MessagePack.Resolvers;
using System.Threading;

namespace ClientLourd.Services.SocketService
{
    public class SocketClient : SocketEventsPublisher
    {
        // For local server usage
        /*private const int PORT = 3001;
        private const string HostName = "127.0.0.1";*/


        private Socket _socket;
        private NetworkStream _stream;
        private Task _receiver;
        private System.Timers.Timer _healthCheckTimer;
        private NetworkInformations _networkInformations;

        public SocketClient(NetworkInformations informations)
        {
            _networkInformations = informations;
            HealthCheck += OnHealthCheck;
        }

        private void OnHealthCheck(object source, EventArgs args)
        {
            Application.Current.Dispatcher.InvokeAsync(() =>
            {
                //We stop the timer 
                _healthCheckTimer.Stop();
                try
                {
                    //We send the healthCheck response to the server
                    SendMessage(new Tlv(SocketMessageTypes.HealthCheckResponse));
                }
                catch
                {
                    //If an error occured the health check Timer will handle it
                }

                //Restart the timer
                _healthCheckTimer.Start();
            }, DispatcherPriority.Send);
        }

        /// <summary>
        /// Send a message to the server. Should be in a try catch block
        /// </summary>
        /// <param name="tlv"></param>
        public void SendMessage(Tlv tlv)
        {
            _socket.Send(tlv.GetBytes());
        }


        public void Close()
        {
            try
            {
                SendMessage(new Tlv(SocketMessageTypes.ServerDisconnection));
                _healthCheckTimer.Close();
            }
            catch
            {
                //The connection will be close 
            }

            _stream.Close();
            _socket.Close();
        }

        public Task InitializeConnection(string token)
        {
            return Task.Factory.StartNew(() =>
            {
                OnStartWaiting(this);
                try
                {
                    //Create the socket
                    _socket = new Socket(_networkInformations.IP.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                    //Connect the socket to the end point
                    _socket.Connect(new IPEndPoint(_networkInformations.IP, _networkInformations.SocketPort));
                    //_socket.Connect(new IPEndPoint(ip, 3001));
                    _stream = new NetworkStream(_socket);

                    InitializeTimer();

                    //Start a message listener
                    _receiver = new Task(MessagesListener);
                    _receiver.Start();

                    //send the session token
                    SendMessage(new Tlv(SocketMessageTypes.ServerConnection, token));
                    OnStopWaiting(this);
                }
                catch
                {
                    OnStopWaiting(this);
                    throw;
                }
            });
        }

        private void MessagesListener()
        {
            byte[] typeAndLength = new byte[3];
            dynamic data = null;
            //TODO cancel the Task using a token
            while (_socket.Connected)
            {
                try
                {
                    // Read the type and the length
                    int count = 0;
                    while (count < 3)
                    {
                        count += _stream.Read(typeAndLength, count, 3 - count);
                    }

                    SocketMessageTypes type = (SocketMessageTypes) typeAndLength[0];
                    int length = (typeAndLength[1] << 8) + typeAndLength[2];
                    if (length > 0)
                    {
                        //Read the data
                        byte[] bytes = new byte[length];
                        count = 0;
                        while (count < length)
                        {
                            count += _stream.Read(bytes, count, length - count);
                        }
                        data = RetreiveData(type, bytes);
                    }

                    switch (type)
                    {
                        case SocketMessageTypes.ServerConnectionResponse:
                            OnConnectionResponse(this);
                            break;
                        case SocketMessageTypes.ServerDisconnection:
                            OnServerDisconnected(this);
                            break;
                        case SocketMessageTypes.HealthCheck:
                            OnHealthCheck(this);
                            break;
                        case SocketMessageTypes.MessageReceived:
                            OnMessageReceived(this, new MessageReceivedEventArgs(data));
                            break;
                        case SocketMessageTypes.UserJoinedChannel:
                            OnUserJoinedChannel(this, new MessageReceivedEventArgs(data));
                            break;
                        case SocketMessageTypes.UserLeftChannel:
                            OnUserLeftChannel(this, new MessageReceivedEventArgs(data));
                            break;
                        case SocketMessageTypes.UserCreatedChannel:
                            OnUserCreatedChannel(this, new MessageReceivedEventArgs(data));
                            break;
                        case SocketMessageTypes.UserDeletedChannel:
                            OnUserDeletedChannel(this, new MessageReceivedEventArgs(data));
                            break;
                        case SocketMessageTypes.ServerStrokeSent:
                            OnServerStrokeSent(this, new StrokeSentEventArgs(data));
                            break;
                        case SocketMessageTypes.ServerStartsDrawing:
                            OnServerStartsDrawing(this, new DrawingEventArgs(data));
                            break;
                        case SocketMessageTypes.ServerEndsDrawing:
                            OnServerEndsDrawing(this, new DrawingEventArgs(data));
                            break;
                        case SocketMessageTypes.DrawingPreviewResponse:
                            OnDrawingPreviewResponse(this, new DrawingEventArgs(data));
                            break;
                        case SocketMessageTypes.ServerMessage:
                            OnServerMessage(this, new SocketErrorEventArgs(data));
                            break;
                        case SocketMessageTypes.JoinLobbyResponse:
                            OnJoinLobbyResponse(this, new LobbyEventArgs(data));
                            break;
                        case SocketMessageTypes.QuitLobbyResponse:
                            // TODO
                            break;
                        case SocketMessageTypes.LobbyCreated:
                            OnLobbyCreated(this, new LobbyEventArgs(data));
                            break;
                        case SocketMessageTypes.LobbyErased:
                            // TODO
                            break;
                        default:
                            throw new InvalidDataException();
                    }
                }
                catch
                {
                    // Here, an exception can be thrown on a logout or if the read timeout.
                }
            }
        }

        private dynamic RetreiveData(SocketMessageTypes type, byte[] bytes)
        {
            switch (type)
            {
                //Raw bytes
                case SocketMessageTypes.ServerConnectionResponse:
                    return bytes;
                case SocketMessageTypes.ServerStrokeSent:
                    return bytes;
                case SocketMessageTypes.ServerStartsDrawing:
                    return bytes;
                case SocketMessageTypes.ServerEndsDrawing:
                    return bytes;

                //Message pack
                default:
                     return MessagePackSerializer.Deserialize<dynamic>(bytes, ContractlessStandardResolver.Options);
            }
        }

        private void InitializeTimer()
        {
            _healthCheckTimer = new System.Timers.Timer(6000);
            _healthCheckTimer.AutoReset = false;
            _healthCheckTimer.Elapsed += TriggerConnectionLost;
            _healthCheckTimer.Start();
        }

        private void TriggerConnectionLost(object sender, ElapsedEventArgs e)
        {
            OnConnectionLost(this);
        }
    }
}