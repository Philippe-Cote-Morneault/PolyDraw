using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
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
using ClientLourd.Services.RestService;

namespace ClientLourd.ViewModels
{
    public class ChatViewModel : ViewModelBase
    {
        private const string GLOBAL_CHANNEL_ID = "00000000-0000-0000-0000-000000000000";
        private int _newMessages;
        private readonly Mutex _mutex = new Mutex();


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

        public SessionInformations SessionInformations
        {
            get
            {
                return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                    ?.SessionInformations;
            }
        }

        public SocketClient SocketClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }

        public RestClient RestClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
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
            AfterLogOut();
        }

        public override void AfterLogin()
        {
            GetChannels();
        }

        private async Task GetChannels()
        {
            Channels = await RestClient.GetChannels();
            SelectedChannel = Channels.First(c => c.ID == GLOBAL_CHANNEL_ID);
            //Release the lock to accept socket event
            _mutex.ReleaseMutex();
        }

        public override void AfterLogOut()
        {
            SocketClient.MessageReceived += SocketClientOnMessageReceived;
            SocketClient.UserCreatedChannel += SocketClientOnUserCreatedChannel;
            SocketClient.UserJoinedChannel += SocketClientOnUserJoinedChannel;
            SocketClient.UserLeftChannel += SocketClientOnUserLeftChannel;
            Channels = new List<Channel>();
            NewMessages = 0;
            //We block all socket event until the channels are import
            _mutex.WaitOne();
        }

        private void SocketClientOnUserLeftChannel(object source, EventArgs args)
        {
            _mutex.WaitOne();
            try
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                        var e = (MessageReceivedEventArgs) args;
                        Channel channel = Channels.First(c => c.ID == e.ChannelId);
                        Message m = new Message(e.Date, new User("admin", "-1"), $"{e.Username} left the channel");
                        channel.Users.Remove(channel.Users.First(u => u.ID == e.UserID));
                        Channels.First(c => c.ID == e.ChannelId).Messages.Add(m);
                        UpdateChannels();
                        NewMessages++;
                });
            }
            finally{
                _mutex.ReleaseMutex();
            }
        }

        private void SocketClientOnUserJoinedChannel(object source, EventArgs args)
        {
            _mutex.WaitOne();
            try
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                        var e = (MessageReceivedEventArgs) args;
                        Channel channel = Channels.First(c => c.ID == e.ChannelId);
                        Message m = new Message(e.Date, new User("admin", "-1"), $"{e.Username} joined the channel");
                        // TODO Cache user
                        channel.Users.Add(new User(e.Username, e.UserID));
                        Channels.First(c => c.ID == e.ChannelId).Messages.Add(m);
                        UpdateChannels();
                        NewMessages++;
                });
            }
            finally
            {
                _mutex.ReleaseMutex();
            }
        }

        private void SocketClientOnUserCreatedChannel(object source, EventArgs args)
        {
            _mutex.WaitOne();
            try
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    MessageReceivedEventArgs e = (MessageReceivedEventArgs) args;
                    var newChannel = new Channel(e.ChannelName, e.ChannelId);
                    Channels.Add(newChannel);
                    UpdateChannels();
                });
            }
            finally
            {
                _mutex.ReleaseMutex();
            }
        }

        private void SocketClientOnMessageReceived(object source, EventArgs e)
        {
            var args = (MessageReceivedEventArgs) e;
            //TODO cache user 
            Message m = new Message(args.Date, new User(args.SenderName, args.SenderID), args.Message);
            App.Current.Dispatcher.Invoke(() => { Channels.First(c => c.ID == args.ChannelId).Messages.Add(m); });
            NewMessages++;
        }

        RelayCommand<object> _createChannelCommand;

        public ICommand CreateChannelCommand
        {
            get
            {
                return _createChannelCommand ??
                       (_createChannelCommand = new RelayCommand<object>(param => CreateChannel()));
            }
        }

        private async Task CreateChannel()
        {
            var dialog = new InputDialog("Enter the name for the new channel");
            var result = await DialogHost.Show(dialog);
            if (bool.Parse(result.ToString()))
            {
                var data = new {ChannelName = dialog.Result};
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.CreateChannel, data));
            }
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
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.JoinChannel, new Guid(channel.ID)));
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
            if (channel.ID != GLOBAL_CHANNEL_ID)
            {
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.LeaveChannel, new Guid(channel.ID)));
            }
            else
            {
                DialogHost.Show(new ClosableErrorDialog("You can't leave the Global channel"));
            }
        }


        private void UpdateChannels()
        {
            NotifyPropertyChanged("Channels");
            NotifyPropertyChanged("JoinedChannels");
            NotifyPropertyChanged("AvailableChannels");
            if (SelectedChannel.Users.FirstOrDefault(u => u.ID == SessionInformations.User.ID) == null)
            {
                SelectedChannel = Channels.First(c => c.ID == GLOBAL_CHANNEL_ID);
            }
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
                return _openDrawerCommand ?? (_openDrawerCommand =
                           new RelayCommand<object[]>(param => OpenChatDrawer(param), param => (bool) param[0]));
            }
        }

        public void OpenChatDrawer(object[] param)
        {
            ((DrawerHost) param[1]).IsRightDrawerOpen = !((DrawerHost) param[1]).IsRightDrawerOpen;
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
            //TODO removed username
            string username = param[1] as string;
            string message = tBox.Text;
            if (!string.IsNullOrWhiteSpace(message) && SelectedChannel != null)
            {
                var data = new {Message = message, ChannelID = SelectedChannel.ID};
                try
                {
                    SocketClient.SendMessage(new Tlv(SocketMessageTypes.MessageSent, data));
                    //Clear the chat textbox
                    tBox.Text = "";
                }
                catch (Exception e)
                {
                    DialogHost.Show(new ClosableErrorDialog(e));
                }
            }
        }


        public ObservableCollection<Channel> JoinedChannels
        {
            get
            {
                return new ObservableCollection<Channel>(_channels.Where(c =>
                    c.Users.Select(m => m.ID).Contains(SessionInformations.User.ID)));
            }
        }

        public ObservableCollection<Channel> AvailableChannels
        {
            get
            {
                return new ObservableCollection<Channel>(_channels.Where(c =>
                    !c.Users.Select(m => m.ID).Contains(SessionInformations.User.ID)));
            }
        }

        public List<Channel> Channels
        {
            get { return _channels; }
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
    }
}