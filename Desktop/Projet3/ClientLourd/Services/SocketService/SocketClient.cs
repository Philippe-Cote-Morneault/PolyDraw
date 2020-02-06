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
using ClientLourd.Models.Constants;
using ClientLourd.Models.Enums;
using ClientLourd.Models.NonBindable;
using MessagePack.Resolvers;

namespace ClientLourd.Services.SocketService
{
    public class SocketClient : SocketEventsPublisher
    {
        // For local server usage
        //private const int PORT = 3001;
        //private const string HostName = "127.0.0.1";

        
        private Socket _socket;
        private NetworkStream _stream;
        private Task _receiver;
        private Timer _healthCheckTimer;

        public SocketClient()
        {
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
            });
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
            }
            catch
            {
                //The connection will be close 
            }
            _healthCheckTimer.Close();
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
                    var ip = Dns.GetHostAddresses(Networks.HOST_NAME)[0];

                    // If connected on a local server, use the line below
                    //var ip = IPAddress.Parse(HostName);

                    //Create the socket
                    _socket = new Socket(ip.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

                    //Connect the socket to the end point
                    _socket.Connect(new IPEndPoint(ip, Networks.SOCKET_PORT));
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
                    _stream.Read(typeAndLength, 0, 3);

                    SocketMessageTypes type = (SocketMessageTypes) typeAndLength[0];
                    int length = (typeAndLength[1] << 8) + typeAndLength[2];
                    if (length > 0)
                    {
                        //Read the data
                        byte[] bytes = new byte[length];
                        _stream.Read(bytes, 0, length);
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
                            OnMessageReceived(this, data);
                            break;
                        case SocketMessageTypes.UserJoinedChannel:
                            OnUserJoinedChannel(this, data);
                            break;
                        case SocketMessageTypes.UserLeftChannel:
                            OnUserLeftChannel(this, data);
                            break;
                        case SocketMessageTypes.UserCreatedChannel:
                            OnUserCreatedChannel(this, data);
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
                //Message pack
                default:
                    return MessagePackSerializer.Deserialize<dynamic>(bytes, ContractlessStandardResolver.Options);
            }
        }

        private void InitializeTimer()
        {
            _healthCheckTimer = new Timer(6000);
            _healthCheckTimer.Elapsed += TriggerConnectionLost;
            _healthCheckTimer.Start();
        }

        private void TriggerConnectionLost(object sender, ElapsedEventArgs e)
        {
            OnConnectionLost(this);
        }
    }
}