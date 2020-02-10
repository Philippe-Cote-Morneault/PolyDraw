using System.Collections.ObjectModel;
using System.Linq;

namespace ClientLourd.Models.Bindable
{
    public class Channel : ModelBase
    {
        public Channel()
        {
        }
        public Channel(string name, string id)
        {
            Name = name;
            ID = id;
            Users = new ObservableCollection<User>();
            Messages = new ObservableCollection<Message>();
        }

        public ObservableCollection<User> Users
        {
            get { return _users; }
            set
            {
                if (value != _users)
                {
                    _users = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private ObservableCollection<User> _users;

        public string ID
        {
            get { return _id; }
            set
            {
                if (value != _id)
                {
                    _id = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _id;

        public string Name
        {
            get { return _name; }
            set
            {
                if (value != _name)
                {
                    _name = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private string _name;

        public ObservableCollection<Message> Messages
        {
            get { return _messages; }
            set
            {
                if (value != _messages)
                {
                    _messages = new ObservableCollection<Message>(value.OrderBy(m => m.Date).ToList());
                    NotifyPropertyChanged();
                }
            }
        }

        private ObservableCollection<Message> _messages;
    }
}