using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models;
using ClientLourd.Utilities.Commands;

namespace ClientLourd.ModelViews
{
    public class ChatViewModel
    {

        public ChatViewModel()
        {
            
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
        
        public ObservableCollection<Message> ReceivedMessages
        {
            get
            {
                return new ObservableCollection<Message>(_receivedMessage);
            }
        }

        private ObservableCollection<Message> _receivedMessage;
        
        public ObservableCollection<Message> SentMessages
        {
            get
            {
                return new ObservableCollection<Message>(_sentMessage);
            }
        }

        private ObservableCollection<Message> _sentMessage;
        
    }
}