using System.Collections.ObjectModel;
using System.Collections.Specialized;
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
                    _messages = value;
                    Messages.CollectionChanged += MessagesOnCollectionChanged;
                    NotifyPropertyChanged();
                }
            }
        }
        private ObservableCollection<Message> _messages;

        private void MessagesOnCollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            if (e.Action == NotifyCollectionChangedAction.Add)
            {
                if (!_isSelected)
                {
                    Notification++;
                }
                if (Messages.Count > 1 && Messages[Messages.Count - 1].Date < Messages[Messages.Count - 2].Date)
                {
                    Messages = new ObservableCollection<Message>(Messages.OrderBy(m => m.Date));
                }
            }
        }


        public int Notification
        {
            get { return _notification; }
            set
            {
                if (_notification != value)
                {
                    _notification = value;
                    NotifyPropertyChanged();
                }
            }
        }
        private int _notification;

        public bool IsSelected
        {
            get { return _isSelected; }
            set
            {
                if (value != _isSelected)
                {
                    _isSelected = value;
                    Notification = 0;
                    NotifyPropertyChanged();
                }
            }
        }
        private bool _isSelected;

    }
}