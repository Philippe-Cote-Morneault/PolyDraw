using System;
using ClientLourd.Services.DateService;

namespace ClientLourd.Models.Bindable
{
    public class Message : ModelBase
    {
        public Message()
        {
        }

        public Message(DateTime date, User user, string content)
        {
            Date = date;
            User = user;
            Content = content;
        }

        public Message(int timestamp, User user, string content)
        {
            Date = Timestamp.UnixTimeStampToDateTime(timestamp);
            User = user;
            Content = content;
        }

        private DateTime _date;

        public DateTime Date
        {
            get { return _date; }
            set
            {
                if (value != _date)
                {
                    _date = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private User _user;

        public User User
        {
            get { return _user; }
            set
            {
                if (value != _user)
                {
                    _user = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _content;

        public string Content
        {
            get { return _content; }
            set
            {
                if (value != _content)
                {
                    _content = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }
}