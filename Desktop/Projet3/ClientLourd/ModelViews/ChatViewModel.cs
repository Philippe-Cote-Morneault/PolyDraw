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
                    User = user2,
                    Text = "The true Southern watermelon is a boon apart, and not to be mentioned with commoner things.  It is chief of the world's luxuries, king by the grace of God over all the fruits of the earth.  When one has tasted it, he knows what the angels eat.  It was not a Southern watermelon that Eve took; we know it because she repented.",
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