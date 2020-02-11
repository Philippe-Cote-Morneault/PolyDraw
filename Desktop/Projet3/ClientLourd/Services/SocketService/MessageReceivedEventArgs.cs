using System;

namespace ClientLourd.Services.SocketService
{
    public class MessageReceivedEventArgs : EventArgs
    {
        public MessageReceivedEventArgs(dynamic data)
        {
            _data = data;
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
            get { return UnixTimeStampToDateTime(_data["Timestamp"]); }
        }

        public string SenderName
        {
            get { return _data["SenderName"]; }
        }

        public string Message
        {
            get { return _data["Message"]; }
        }

        public string SenderID
        {
            get { return _data["SenderID"]; }
        }
        
        public string UserID
        {
            get { return _data["UserID"]; }
        }
        public string Username
        {
            get { return _data["Username"]; }
        }


        private static DateTime UnixTimeStampToDateTime(double unixTimeStamp)
        {
            DateTime dtDateTime = new DateTime(1970, 1, 1, 0, 0, 0, 0, System.DateTimeKind.Utc);
            dtDateTime = dtDateTime.AddSeconds(unixTimeStamp).ToLocalTime();
            return dtDateTime;
        }
    }
}