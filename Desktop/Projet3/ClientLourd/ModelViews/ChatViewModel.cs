using System;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Models;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Utilities.SocketEventsArguments;

namespace ClientLourd.ModelViews
{
    public class ChatViewModel: ViewModelBase
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
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?._socketClient; }
        }

        public ChatViewModel()
        {
            SocketClient.MessageReceived += SocketClientOnMessageReceived;
            _channels = new ObservableCollection<Channel>()
            {
                new Channel()
                {
                    Messages = new ObservableCollection<Message>(),
                    Name = "Global",
                },
            };
        }

        private void SocketClientOnMessageReceived(object source, EventArgs e)
        {
            var args = (MessageReceivedEventArgs) e;
            Message m = new Message(args.Date, new User(args.UserName, args.UserId), args.Message);
            App.Current.Dispatcher.Invoke(() => { Channels[0].Messages.Add(m); });
            NewMessages++;
        }


        RelayCommand<object[]> _sendMessageCommand; public ICommand SendMessageCommand
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
            var data = new {Message = message, CanalID = "0"};
            SocketClient.sendMessage(new TLV(SocketMessageTypes.MessageSent, data));
            //Clear the chat textbox
            tBox.Text = "";
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