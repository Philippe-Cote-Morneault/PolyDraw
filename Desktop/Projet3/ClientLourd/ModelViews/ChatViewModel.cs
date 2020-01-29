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
            //SocketClient.MessageReceived += SocketClientOnMessageReceived;
            
            
            
            User user1 = new User()
            {
                ID = "1",
                Name = "user1",
            };
            
            User user2 = new User()
            {
                ID = "2",
                Name = "user2",
            };
            
            User user3 = new User()
            {
                ID = "3",
                Name = "user3",
            };

            ObservableCollection<Message> messages1 = new ObservableCollection<Message>()
            {
                new Message()
                {
                    Date = new DateTime(1991, 01, 01),
                    User = user1,
                    Text = "Messge 2",
                },
                new Message()
                {
                    Date = new DateTime(1990, 01, 01),
                    User = user1,
                    Text = "Messge 1",
                },
                new Message()
                {
                    Date = new DateTime(1993, 01, 01),
                    User = user2,
                    Text = "Messge 3",
                },
            };
            
            ObservableCollection<Message> messages2 = new ObservableCollection<Message>()
            {
                new Message()
                {
                    Date = new DateTime(2019, 01, 01),
                    User = user2,
                    Text = "Today is what happened to yesterday.",
                },
                new Message()
                {
                    Date = new DateTime(2010, 01, 01),
                    User = user1,
                    Text = "You will be the last person to buy a Chrysler",
                },
                new Message()
                {
                    Date = new DateTime(2000, 01, 01),
                    User = user3,
                    Text = "The true Southern watermelon is a boon apart, and not to be mentioned with commoner things.  It is chief of the world's luxuries, king by the grace of God over all the fruits of the earth.  When one has tasted it, he knows what the angels eat.  It was not a Southern watermelon that Eve took; we know it because she repented.",
                },
            };

            Channels = new ObservableCollection<Channel>()
            {
                new Channel()
                {
                    Name = "channel1",
                    Messages = messages1,
                },
                new Channel()
                {
                    Name = "channel2",
                    Messages = messages2,
                }
            };
            
            
            
        }

        private void SocketClientOnMessageReceived(object source, EventArgs args)
        {
            //TODO
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
            SocketSendMessage(message);
            if (!String.IsNullOrEmpty(message))
            {
                Message mes = new Message();
                mes.Text = message;
                mes.User = new User(){ ID = username, Name = username,};
                mes.Date = DateTime.Now;
                Channels[0].Messages.Add(mes);
                UpdateMessagesCount();
                clearTextBox(tBox);
            }
        }

        private void SocketSendMessage(string message)
        {            
            SocketClient.sendMessage(new TLV(SocketMessageTypes.MessageSent, message, "channel1"));
        }

        private void clearTextBox(TextBox tbox)
        {
            tbox.Text = "";
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