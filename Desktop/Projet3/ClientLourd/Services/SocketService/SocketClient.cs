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
        
        public SocketClient()
        {
            //TODO catch exception
            var ip = IPAddress.Parse(IP);
            //Create the socket
            _socket = new Socket(ip.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
            //Connect the socket to the end point
            _socket.Connect(new IPEndPoint(ip, PORT));
            _stream = new NetworkStream(_socket);
            InitializeConnection("token");
        }

        public void sendMessage(TLV tlv)
        {
            _socket.Send(tlv.GetBytes());
        }

        public void InitializeConnection(string token)
        {
            //TODO send the token
            //sendMessage(new TLV(SocketMessageTypes.ServerConnection, token));
            //Start a message listener
            _receiver = new Task(MessagesListener);
            _receiver.Start();
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
