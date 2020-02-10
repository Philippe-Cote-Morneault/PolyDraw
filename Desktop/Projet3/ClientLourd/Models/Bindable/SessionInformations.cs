using ClientLourd.Models.NonBindable;

namespace ClientLourd.Models.Bindable
{
    public class SessionInformations: ModelBase
    {
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