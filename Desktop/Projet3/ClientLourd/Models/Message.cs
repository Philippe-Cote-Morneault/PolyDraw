using System;

namespace ClientLourd.Models
{
    public class Message : ModelBase
    {
        public Message(DateTime date, User user, string content)
        {
            Date = date;
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