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
    }
}