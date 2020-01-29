using System;
using System.IO;
using System.Linq;
using System.Linq.Expressions;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using ClientLourd.Models;
using ClientLourd.Utilities.Enums;
using MessagePack;
using MessagePack.Resolvers;

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
            _socket.Send(tlv.GetBytes());
        }

        private void InitializeConnection(IPAddress ip, string token)
        {
            //TODO send the token
            _socket.Connect(new IPEndPoint(ip, PORT));
            _stream = new NetworkStream(_socket);
        } 
        private void MessagesListener()
        {
            //TODO correct buffer size
            byte[] bytes = new byte[4096];
            dynamic data = null;
            while (_socket.Connected)
            {
                // Read the type and the length
                _stream.Read(bytes, 0, 3);
                int length = (bytes[1] << 8) + bytes[2];
                if (length > 0)
                {
                    //Read the data
                    _stream.Read(bytes, 3, length);
                    data = MessagePackSerializer.Deserialize<dynamic>(bytes.Skip(3).ToArray(), ContractlessStandardResolver.Options);
                }
                SocketMessageTypes type = (SocketMessageTypes)bytes[0];
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
        }


    }
}
