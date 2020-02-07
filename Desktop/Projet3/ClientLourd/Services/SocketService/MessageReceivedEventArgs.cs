using System;

namespace ClientLourd.Services.SocketService
{
    public class MessageReceivedEventArgs : EventArgs
    {
        public MessageReceivedEventArgs(dynamic data)
        {
            ChannelId = data["ChannelID"];
            Date = UnixTimeStampToDateTime(data["Timestamp"]);
            Message = data["Message"];
            UserName = data["SenderName"];
            UserId = data["SenderID"];
        }

        public string ChannelId { get; private set; }
        public DateTime Date { get; private set; }
        public string UserName { get; private set; }
        public string Message { get; private set; }
        public string UserId { get; private set; }


        private static DateTime UnixTimeStampToDateTime(double unixTimeStamp)
        {
            DateTime dtDateTime = new DateTime(1970, 1, 1, 0, 0, 0, 0, System.DateTimeKind.Utc);
            dtDateTime = dtDateTime.AddSeconds(unixTimeStamp).ToLocalTime();
            return dtDateTime;
        }
    }
}