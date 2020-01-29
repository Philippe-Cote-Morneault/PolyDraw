using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
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
        private int _messagesCount;

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

        private void SocketClientOnMessageReceived(object source, MessageReceivedEventArgs e)
        {
            //TODO
            Message m = new Message(e.Date, new User(e.UserName, e.UserId), e.Message);
            App.Current.Dispatcher.Invoke(() => { Channels[0].Messages.Add(m); });
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

        private void UpdateMessagesCount()
        {
            var currentMessageCount = 0;
            foreach (var messages in Channels.Select(c => c.Messages))
            {
                currentMessageCount += messages.Count;
            }
            NewMessages += currentMessageCount - _messagesCount;
            _messagesCount = currentMessageCount;
        }

        private ObservableCollection<Channel> _channels;
        
        
    }
}