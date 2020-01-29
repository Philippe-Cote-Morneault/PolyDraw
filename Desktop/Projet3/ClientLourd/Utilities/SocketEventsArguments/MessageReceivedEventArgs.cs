using System;

namespace ClientLourd.Utilities.SocketEventsArguments
{
    public class MessageReceivedEventArgs : EventArgs
    {
        public MessageReceivedEventArgs(dynamic data)
        {
            ChannelId = data["CanalID"];
            Date = new DateTime(1970, 1, 1, 0, 0, 0, 0).
                AddSeconds(Math.Round(1372061224000 / 1000d)).ToLocalTime();
            Message = data["Message"];
            UserName = data["SenderName"];
            UserId = data["SenderID"];
        }
        
        public string ChannelId { get; private set; }
        public DateTime Date { get; private set; }
        public string UserName { get; private set; }
        public string Message { get; private set; }
        public string UserId { get; private set; }
    }
}