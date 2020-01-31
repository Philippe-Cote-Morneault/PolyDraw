using System;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using ClientLourd.Models;
using ClientLourd.Utilities.Enums;
using MessagePack;
using System.Timers;
using MessagePack.Resolvers;

namespace ClientLourd.Services.SocketService
{
    public class SocketClient : SocketEventsPublisher
    {

        //private const int PORT = 5001;
        private const int PORT = 3001;
        //private const string HostName = "log3900.fsae.polymtl.ca";
        private const string HostName = "127.0.0.1";
        private Socket _socket;
        private NetworkStream _stream;
        private Task _receiver;
        private System.Timers.Timer _timer;

        public SocketClient()
        {
            //TODO catch exception
            //var ip = Dns.GetHostAddresses(HostName)[0];
            var ip = IPAddress.Parse(HostName);
            //Create the socket
            _socket = new Socket(ip.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            //Connect the socket to the end point
            _socket.Connect(new IPEndPoint(ip, PORT));
            _stream = new NetworkStream(_socket);

            _timer = new System.Timers.Timer(4000);
            _timer.Elapsed += ElapsedTimer;
            _timer.Start();
        }

        private void ElapsedTimer(object sender, ElapsedEventArgs e)
        {
            _timer.Stop();
            if (!IsConnected())
            {
                // TODO: Return to Login page
                // TODO: Show error message
                MessageBox.Show("Socket connection interrupted");

                return;
            }

            _timer.Start();
        }


        void Reconnect()
        {
            _socket.Close();
            bool isConnected = false;

            while (!isConnected)
            {
                Thread.Sleep(1000);
                try
                {
                    //TODO Change to DNS...
                    _socket = new Socket(IPAddress.Parse(HostName).AddressFamily, SocketType.Stream, ProtocolType.Tcp);
                    _socket.Connect(new IPEndPoint(IPAddress.Parse(HostName), PORT)); _stream = new NetworkStream(_socket);
                    _stream = new NetworkStream(_socket);
                    isConnected = true;
                    MessageBox.Show("Has successfully been reconnected");
                }
                catch (Exception e)
                {
                    
                }
            } 
            

        }

        bool IsConnected()
        {
            return !((_socket.Poll(500, SelectMode.SelectRead) && (_socket.Available == 0)) || !_socket.Connected);
        }


        public void sendMessage(TLV tlv)
        {
            try
            {
                _socket.Send(tlv.GetBytes());
            }
            catch (Exception e)
            {
                MessageBox.Show("Error while sending data from socket");
            }
            
        }

        public void InitializeConnection(string token)
        {
            //TODO send the token
            //Start a message listener
            _receiver = new Task(MessagesListener);
            _receiver.Start();

            sendMessage(new TLV(SocketMessageTypes.ServerConnection, token));
        } 
        private void MessagesListener()
        {
            //TODO correct buffer size
            byte[] bytes = new byte[4096];  
            dynamic data = null;


           while (IsConnected())
            {
                try
                {
                    // Read the type and the length
                    _stream.Read(bytes, 0, 3);

                    SocketMessageTypes type = (SocketMessageTypes)bytes[0];
                    int length = (bytes[1] << 8) + bytes[2];
                    if (length > 0)
                    {
                        //Read the data
                        _stream.Read(bytes, 3, length);
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
                    MessageBox.Show("Error while reading socket!");
                    _receiver.Dispose();
                }
            }
        }

        private dynamic RetreiveData(SocketMessageTypes type, byte[] bytes)
        {
            switch (type)
            {
                //Raw bytes
                case SocketMessageTypes.ServerConnectionResponse:
                    return bytes.Skip(3).ToArray();
                //Message pack
                default:
                    return MessagePackSerializer.Deserialize<dynamic>(bytes.Skip(3).ToArray(), ContractlessStandardResolver.Options);
            }
        }
        

    }
}
