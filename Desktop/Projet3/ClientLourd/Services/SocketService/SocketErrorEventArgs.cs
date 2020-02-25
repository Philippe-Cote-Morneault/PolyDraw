using System;
using ClientLourd.Services.DateService;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.Services.SocketService
{
    public class SocketErrorEventArgs : EventArgs
    {
        public SocketErrorEventArgs(dynamic data)
        {
            _data = data;
        }

        private dynamic _data;

        public SocketMessageTypes Type
        {
            get { return (SocketMessageTypes)_data["Type"]; }
        }
        
        public SocketMessageTypes Code
        {
            get { return _data["ErrorCode"]; }
        }
        public string Message
        {
            get { return _data["Message"]; }
        }


    }
}