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
using ClientLourd.Models.Enums;
using ClientLourd.Models.NonBindable;
using MessagePack.Resolvers;

namespace ClientLourd.Services.SocketService
{
    public class SocketClient : SocketEventsPublisher
    {
        // If running on a local server, comment out these lines
        //private const int PORT = 3001;
        //private const string HostName = "127.0.0.1";

        private const int PORT = 5011;
        private const string HostName = "log3900.fsae.polymtl.ca";
        
        private Socket _socket;
        private NetworkStream _stream;
        private Task _receiver;
        private Timer _pollTimer;
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
                //We send the healthCheck response to the server
                sendMessage(new Tlv(SocketMessageTypes.HealthCheckResponse));
                //Restart the timer
                _healthCheckTimer.Start();
            });
        }


        public void sendMessage(Tlv tlv)
        {
            try
            {
                _socket.Send(tlv.GetBytes());
            }
            catch (Exception e)
            {
            }
        }

        public void Close()
        {
            sendMessage(new Tlv(SocketMessageTypes.ServerDisconnection));
            _pollTimer.Close();
            _healthCheckTimer.Close();
            _stream.Close();
            _socket.Close();
        }

        public void InitializeConnection(string token)
        {
            try
            {
                var ip = Dns.GetHostAddresses(HostName)[0];

                // If connected on a local server, use the line below
                //var ip = IPAddress.Parse(HostName);
                
                //Create the socket
                _socket = new Socket(ip.AddressFamily, SocketType.Stream, ProtocolType.Tcp);

                //Connect the socket to the end point
                _socket.Connect(new IPEndPoint(ip, PORT));
                _stream = new NetworkStream(_socket);
            }
            catch (Exception e)
            {
                return;
            }

            InitializeTimers();

            //Start a message listener
            _receiver = new Task(MessagesListener);
            _receiver.Start();

            sendMessage(new Tlv(SocketMessageTypes.ServerConnection, token));
        }

        private void MessagesListener()
        {
            //TODO correct buffer size
            byte[] typeAndLength = new byte[3];
            dynamic data = null;

            while (IsConnected())
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
                catch (Exception e)
                {
                    // Here, an exception can be thrown on a logout.
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

        private void InitializeTimers()
        {
            _pollTimer = new Timer(4000);
            _pollTimer.Elapsed += CheckConnection;
            _pollTimer.Start();
            _healthCheckTimer = new Timer(60000);
            _healthCheckTimer.Elapsed += TriggerConnectionLost;
            _healthCheckTimer.Start();
        }

        private void TriggerConnectionLost(object sender, ElapsedEventArgs e)
        {
            OnConnectionLost(this);
        }
        

        private void CheckConnection(object sender, ElapsedEventArgs e)
        {
            _pollTimer.Stop();

            if (!IsConnected())
            {
                OnConnectionLost(this);
                return;
            }

            _pollTimer.Start();
        }

        public bool IsConnected()
        {
            try
            {
                return !(!_socket.Connected || (_socket.Poll(500, SelectMode.SelectRead) && (_socket.Available == 0)));
            }
            catch (Exception e)
            {
                return false;
            }
        }
    }
}