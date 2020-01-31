using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Models;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Utilities.SocketEventsArguments;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.ModelViews
{
    public class ChatViewModel : ViewModelBase
    {
        private int _newMessages;


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

        private int _selectedChannelIndex;

        public int SelectedChannelIndex
        {
            get { return _selectedChannelIndex; }
            set
            {
                if (value != _selectedChannelIndex)
                {
                    _selectedChannelIndex = value;
                }
            }
        }

        public ChatViewModel()
        {
            Channels = new ObservableCollection<Channel>()
            {
                new Channel()
                {
                    Messages = new ObservableCollection<Message>(),
                    Name = "Global",
                },
            };
            Init();
        }

        public override void Init()
        {
            SocketClient.MessageReceived += SocketClientOnMessageReceived;
            Channels.ToList().ForEach(c => c.Messages.Clear());
            NewMessages = 0;
        }

        private void SocketClientOnMessageReceived(object source, EventArgs e)
        {
            var args = (MessageReceivedEventArgs) e;
            Message m = new Message(args.Date, new User(args.UserName, args.UserId), args.Message);
            App.Current.Dispatcher.Invoke(() => { Channels[0].Messages.Add(m); });
            NewMessages++;
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
                return _openDrawerCommand ?? (_openDrawerCommand = new RelayCommand<object[]>(
                           param => ((DrawerHost) param[1]).IsRightDrawerOpen =
                               !((DrawerHost) param[1]).IsRightDrawerOpen, param => (bool) param[0]));
            }
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
            if (!string.IsNullOrEmpty(message))
            {
                var data = new {Message = message, ChannelID = "0"};
                SocketClient.sendMessage(new TLV(SocketMessageTypes.MessageSent, data));
                //Clear the chat textbox
                tBox.Text = "";
            }
        }

        public ObservableCollection<Channel> Channels
        {
            get { return _channels; }
            set
            {
                if (value != _channels)
                {
                    _channels = value;
                    NotifyPropertyChanged();
                }
            }
        }


        private ObservableCollection<Channel> _channels;
    }
}