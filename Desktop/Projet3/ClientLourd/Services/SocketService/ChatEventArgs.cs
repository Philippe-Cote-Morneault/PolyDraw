using System;
using ClientLourd.Services.DateService;

namespace ClientLourd.Services.SocketService
{
    public class ChatEventArgs : EventArgs
    {
        public ChatEventArgs(dynamic data)
        {
            _data = data;
        }

        public bool IsGame
        {
            get => _data["IsGame"];
        }

        private dynamic _data;

        public string ChannelId
        {
            get { return _data["ChannelID"]; }
        }

        public string ChannelName
        {
            get { return _data["ChannelName"]; }
        }

        public DateTime Date
        {
            get { return Timestamp.UnixTimeStampToDateTime(_data["Timestamp"]); }
        }

        public string Message
        {
            get { return _data["Message"]; }
        }

        public string UserID
        {
            get { return _data["UserID"]; }
        }

        public string Username
        {
            get { return _data["Username"]; }
        }

        public string NewName
        {
            get { return _data["NewName"]; }
        }

        public int PictureID
        {
            get => (int)_data["PictureID"];
        }

        public string OldName
        {
            get => _data["OldName"];
        }
    }
}