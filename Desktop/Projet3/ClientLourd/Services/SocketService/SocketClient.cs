using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Services.SocketService
{
    public class SocketClient
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

        public void sendMessage(string message)
        {
            var bytes = Encoding.ASCII.GetBytes(message);
            _stream.Write(bytes, 0, bytes.Length);
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
            Byte[] bytes = new Byte[1024];
            while (_socket.Connected)
            {
                //TODO read the messages
                //receive the message
                int bytesCount = 3;
                _stream.Read(bytes, 0, 1);
                _stream.Read(bytes, 1, 2);
                _stream.Read(bytes, 1, BitConverter.ToInt16(bytes, 1));
                
                //Encode the message
                Console.WriteLine(Encoding.ASCII.GetString(bytes, 0, bytesCount));
            }            
        }
        
        
        
    }
}
