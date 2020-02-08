using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.ViewModels
{
    public class ChatViewModel : ViewModelBase
    {
        private int _newMessages;

        /// <summary>
        /// New message counter
        /// </summary>
        public int NewMessages
        {
            get => _newMessages;
            set
            {
                if (value != _newMessages)
                {
                    _newMessages = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public SocketClient SocketClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }

        private Channel _selectedChannel;

        public Channel SelectedChannel
        {
            get { return _selectedChannel; }
            set
            {
                if (value != _selectedChannel)
                {
                    _selectedChannel = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public ChatViewModel()
        {
            Channels = new List<Channel>()
            {
                new Channel()
                {
                    Messages = new ObservableCollection<Message>(),
                    Name = "Global",
                    Members = new ObservableCollection<User>()
                    {
                        new User("test","2323"),
                    },
                },
                new Channel()
                {
                    Messages = new ObservableCollection<Message>(),
                    Name = "test1",
                    Members = new ObservableCollection<User>()
                    {
                        new User("test","2323"),
                    },
                },
                new Channel()
                {
                    Messages = new ObservableCollection<Message>(),
                    Name = "test2",
                    Members = new ObservableCollection<User>()
                    {
                        new User("jow","2323"),
                    },
                },
            };
            SelectedChannel = Channels[0];
            Init();
            (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).ChatOpen += OnChatOpen;
        }

        private void OnChatOpen(object source, EventArgs args)
        {
            OnChatOpen(source);

        }

        public override void Init()
        {
            SocketClient.MessageReceived += SocketClientOnMessageReceived;
            Channels.ForEach(c => c.Messages.Clear());
            NewMessages = 0;
        }

        private void SocketClientOnMessageReceived(object source, EventArgs e)
        {
            var args = (MessageReceivedEventArgs) e;
            Message m = new Message(args.Date, new User(args.UserName, args.UserId), args.Message);
            App.Current.Dispatcher.Invoke(() => { Channels[0].Messages.Add(m); });
            NewMessages++;
        }
        
        
        RelayCommand<Channel> _changeChannelCommand;

        public ICommand ChangeChannelCommand
        {
            get
            {
                return _changeChannelCommand ??
                       (_changeChannelCommand = new RelayCommand<Channel>(channel => SelectedChannel = channel));
            }
        }
        
        RelayCommand<Channel> _joinChannelCommand;

        public ICommand JoinChannelCommand
        {
            get
            {
                return _joinChannelCommand ??
                       (_joinChannelCommand = new RelayCommand<Channel>(channel => JoinChannel(channel)));
            }
        }

        public void JoinChannel(Channel channel)
        {
            channel.Members.Add(new User("test", "1"));
            UpdateChannels();
        }
        
        
        RelayCommand<Channel> _leaveChannelCommand;

        public ICommand LeaveChannelCommand
        {
            get
            {
                return _leaveChannelCommand ??
                       (_leaveChannelCommand = new RelayCommand<Channel>(channel => LeaveChannel(channel)));
            }
        }

        public void LeaveChannel(Channel channel)
        {
            //TODO change the name
            channel.Members.Remove(channel.Members.First(u => u.Name == "test"));
            UpdateChannels();
        }
        
        
        

        private void UpdateChannels()
        {
            NotifyPropertyChanged("Channels");
            NotifyPropertyChanged("JoinedChannels");
            NotifyPropertyChanged("AvailableChannels");
        }
        
        

        RelayCommand<object> _clearNotificationCommand;

        public ICommand ClearNotificationCommand
        {
            get
            {
                return _clearNotificationCommand ??
                       (_clearNotificationCommand = new RelayCommand<object>(param => NewMessages = 0));
            }
        }

        private RelayCommand<object[]> _openDrawerCommand;

        public ICommand OpenDrawerCommand
        {
            get
            {
                return _openDrawerCommand ?? (_openDrawerCommand = new RelayCommand<object[]>(param => OpenChatDrawer(param), param => (bool) param[0]));
            }
        }

        public void OpenChatDrawer(object[] param)
        {
            ((DrawerHost)param[1]).IsRightDrawerOpen = !((DrawerHost)param[1]).IsRightDrawerOpen;
            OnChatOpen(this);
        }

        RelayCommand<object[]> _sendMessageCommand;

        public ICommand SendMessageCommand
        {
            get
            {
                return _sendMessageCommand ??
                       (_sendMessageCommand = new RelayCommand<object[]>(param => this.SendMessage(param)));
            }
        }

        private void SendMessage(object[] param)
        {
            TextBox tBox = param[0] as TextBox;
            string username = param[1] as string;
            string message = tBox.Text;
            if (!string.IsNullOrWhiteSpace(message))
            {
                var data = new {Message = message, ChannelID = "0"};
                try
                {
                    SocketClient.SendMessage(new Tlv(SocketMessageTypes.MessageSent, data));
                    //Clear the chat textbox
                    tBox.Text = "";
                }
                catch(Exception e)
                {
                    DialogHost.Show(new ClosableErrorDialog(e));
                }
            }
        }


        public ObservableCollection<Channel> JoinedChannels
        {
            get
            {
                //TODO change name
                return new ObservableCollection<Channel>(_channels.Where(c => c.Members.Select(m => m.Name).Contains("test")).ToList());
            }
        }
        public ObservableCollection<Channel> AvailableChannels
        {
            get
            {
                //TODO change name
                var test = new ObservableCollection<Channel>(_channels.Where(c => !c.Members.Select(m => m.Name).Contains("test")).ToList());
                return test;
            }
        }
        public List<Channel> Channels
        {
            get
            {
                return _channels;
            }
            set
            {
                if (value != _channels)
                {
                    _channels = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged("JoinedChannels");
                    NotifyPropertyChanged("AvailableChannels");
                }
            }
        }

        private List<Channel> _channels;

        public delegate void ChatOpenHandler(object source, EventArgs args);

        public event ChatOpenHandler ChatOpen;

        protected virtual void OnChatOpen(object source)
        {
            ChatOpen?.Invoke(source, EventArgs.Empty);
        }

    }
}