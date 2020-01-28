using System;

namespace ClientLourd.Utilities.SocketEventsArguments
{
    public class MessageReceivedEventArgs : EventArgs
    {
        public MessageReceivedEventArgs(dynamic data)
        {
            ChannelId = data["ChannelID"];
            Date = DateTime.Parse(data["timestamp"]);
            ChannelId = data["UserID"];
            UserName = data["UserName"];
            UserId = data["UserId"];
        }
        
        public int ChannelId { get; private set; }
        public DateTime Date { get; private set; }
        public string UserName { get; private set; }
        public int UserId { get; private set; }
    }
}