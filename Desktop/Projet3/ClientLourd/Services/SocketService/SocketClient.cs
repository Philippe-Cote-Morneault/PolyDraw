using System;
using System.IO;
using System.Linq.Expressions;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using ClientLourd.Models;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.Services.SocketService
{
    public class SocketClient : SocketEventsPublisher
    {
        
        private const int PORT = 3001;
        private const string IP = "127.0.0.1";
        private Socket _socket;
        private NetworkStream _stream;
        private Task _receiver;
        
        public SocketClient(string token)
        {
            //TODO catch exception
            var ip = IPAddress.Parse(IP);
            _socket = new Socket(ip.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            InitializeConnection(ip,token);
            _receiver = new Task(MessagesListener);
            _receiver.Start();
        }

        public void sendMessage(TLV tlv)
        {
            _socket.Send(Serializer.Serializer.ToByteArray(tlv));
        }

        private void InitializeConnection(IPAddress ip, string token)
        {
            //TODO send the token
            _socket.Connect(new IPEndPoint(ip, PORT));
            _socket.Send(Encoding.ASCII.GetBytes($"token: {token}"));
            _stream = new NetworkStream(_socket);
        } 
        private void MessagesListener()
        {
            byte[] bytes = new byte[4096];
            while (_socket.Connected)
            {
                // Read the type and the length
                _stream.Read(bytes, 0, 3);
                int length = bytes[1] << 8 + bytes[2];
                if (length > 0)
                {
                    //Read the data
                    _stream.Read(bytes, 3, length);
                }
                SocketMessageTypes type = (SocketMessageTypes)bytes[0];
                switch (type)
                {
                    case SocketMessageTypes.ServerConnectionResponse:
                        OnConnectionResponse(null);
                        break;
                    case SocketMessageTypes.ServerDisconnection:
                        OnServerDisconnected(null);
                        break;
                    case SocketMessageTypes.HealthCheck:
                        OnHealthCheck(null);
                        break;
                    case SocketMessageTypes.MessageReceived:
                        OnMessageReceived(null);
                        break;
                    case SocketMessageTypes.UserJoinedChannel:
                        OnUserJoinedChannel(null);
                        break;
                    case SocketMessageTypes.UserLeftChannel:
                        OnUserLeftChannel(null);
                        break;
                    case SocketMessageTypes.UserCreatedChannel:
                        OnUserCreatedChannel(null);
                        break;
                    default:
                        throw new InvalidDataException();
                }

            }            
        }


    }
}
