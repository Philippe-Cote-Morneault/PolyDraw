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
                    _messages = new ObservableCollection<Message>(value.OrderBy(m => m.Date).ToList());
                    _messages.CollectionChanged += MessagesOnCollectionChanged;
                    NotifyPropertyChanged();
                }
            }
        }
        private ObservableCollection<Message> _messages;

        private void MessagesOnCollectionChanged(object sender, NotifyCollectionChangedEventArgs e)
        {
            App.Current.Dispatcher.InvokeAsync(() =>
            {
                if (e.Action == NotifyCollectionChangedAction.Add)
                {
                    if (!_isSelected)
                    {
                        Notification++;
                    }

                    Message tmp;
                    for (int i = Messages.Count - 1; i > 0; i--)
                    {
                        if (Messages[i - 1].Date > Messages[i].Date)
                        {
                            tmp = Messages[i];
                            Messages[i] = Messages[i - 1];
                            Messages[i - 1] = tmp;
                        }
                        else
                        {
                            return;
                        }
                    }
                }
            });
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