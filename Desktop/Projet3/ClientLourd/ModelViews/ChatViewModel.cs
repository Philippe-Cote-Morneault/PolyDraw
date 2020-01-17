using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models;
using ClientLourd.Utilities.Commands;

namespace ClientLourd.ModelViews
{
    public class ChatViewModel: ViewModelBase
    {

        public ChatViewModel()
        {
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

            ObservableCollection<Message> messages1 = new ObservableCollection<Message>()
            {
                new Message()
                {
                    Date = new DateTime(1991, 01, 01),
                    User = user1,
                },
                new Message()
                {
                    Date = new DateTime(1990, 01, 01),
                    User = user1,
                },
                new Message()
                {
                    Date = new DateTime(1993, 01, 01),
                    User = user2,
                },
            };
            
            ObservableCollection<Message> messages2 = new ObservableCollection<Message>()
            {
                new Message()
                {
                    Date = new DateTime(2019, 01, 01),
                    User = user2,
                },
                new Message()
                {
                    Date = new DateTime(2010, 01, 01),
                    User = user1,
                },
                new Message()
                {
                    Date = new DateTime(2000, 01, 01),
                    User = user2,
                },
            };

            Channels = new ObservableCollection<Channel>()
            {
                new Channel()
                {
                    Name = "c1",
                    Messages = messages1,
                },
                new Channel()
                {
                    Name = "c2",
                    Messages = messages2,
                }
            };
            
            
            
        }

            
        RelayCommand<string> _sendMessageCommand; public ICommand SendMessageCommand
        {
            get
            {
                if (_sendMessageCommand == null)
                {
                    _sendMessageCommand = new RelayCommand<string>(param => this.SendMessage(param));
                }
                return _sendMessageCommand;
            }
        }

        private void SendMessage(string message)
        {
            MessageBox.Show(message);
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