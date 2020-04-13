using System;
using ClientLourd.Models.NonBindable;

namespace ClientLourd.Models.Bindable
{
    public class SessionInformations : ModelBase
    {
        public SessionInformations()
        {
            User = new User("", Guid.Empty.ToString(), false);
        }

        private User _user;

        public User User
        {
            get { return _user; }
            set
            {
                if (_user != value)
                {
                    _user = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public TokenPair Tokens { get; set; }
    }
}