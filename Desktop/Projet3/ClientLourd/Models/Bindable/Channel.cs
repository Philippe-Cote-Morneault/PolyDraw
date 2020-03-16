using System.Collections.ObjectModel;
using System.Collections.Specialized;
using System.Linq;
using System.Timers;

namespace ClientLourd.Models.Bindable
{
    public class Channel : ModelBase
    {
        public Channel()
        {
            InitTimer();
        }
        public Channel(string name, string id)
        {
            InitTimer();
            Name = name;
            ID = id;
            IsFullyLoaded = true;
            Users = new ObservableCollection<User>();
            Messages = new ObservableCollection<Message>();
        }

        private Timer _clearMessageTimer;
        private void InitTimer()
        {
            IsFullyLoaded = false;
            _clearMessageTimer = new Timer(10000);
            _clearMessageTimer.AutoReset = false;
            _clearMessageTimer.Elapsed += (sender, args) =>
            {
                if (Messages.Count > 50)
                {
                    Messages = new ObservableCollection<Message>(Messages.Skip(Messages.Count - 50));
                    IsFullyLoaded = false;
                }
            };
        }
        
        public bool IsFullyLoaded { get; set; }

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


        public int UserMessageCount
        {
            get { return Messages.Count(m => m.User.ID != "-1"); }
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
                    if (_isSelected)
                    {
                        _clearMessageTimer.Stop();
                    }
                    else
                    {
                        _clearMessageTimer.Start();
                    }
                }
            }
        }
        private bool _isSelected;

    }
}