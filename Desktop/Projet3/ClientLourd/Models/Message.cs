using System;

namespace ClientLourd.Models
{
    public class Message: ModelBase
    {
        public Message()
        {
                 
        }

        private DateTime _date;
        public DateTime Date
        {
            get
            {
                return _date;
            }
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
            get
            {
                return _user;
            }
            set
            {
                if (value != _user)
                {
                    _user = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }
}