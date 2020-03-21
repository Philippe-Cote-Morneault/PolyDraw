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
using ClientLourd.Services.SoundService;

namespace ClientLourd.ViewModels
{
    public class ChatViewModel : ViewModelBase
    {
        private const string GLOBAL_CHANNEL_ID = "00000000-0000-0000-0000-000000000000";
        private readonly Mutex _mutex = new Mutex();
        private List<User> _users;
        private readonly User _admin = new User("Admin", "-1");

        /// <summary>
        /// New message counter
        /// </summary>
        public int NewMessages
        {
            get { return Channels.Sum(c => c.Notification); }
        }

        private async Task<User> GetUser(string username, string id)
        {
            User user = _users.FirstOrDefault(u => u.ID == id);
            if (user == null)
            {
                user = (await RestClient.GetUserInfo(id));
                _users.Add(user);
            }
            return user;
        }

        public void OnChatToggle(bool isOpen)
        {
            NotifyPropertyChanged(nameof(NewMessages));
            if (isOpen)
            {
                SelectedChannel.IsSelected = true;
            }
            else
            {
                SelectedChannel.IsSelected = false;
            }
            
        }

        public SoundService SoundService
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SoundService; }
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
                    if (_selectedChannel != null)
                    {
                        //Remove the selection for the old channel
                        _selectedChannel.IsSelected = false;
                    }
                    _selectedChannel = value;
                    if (_selectedChannel != null)
                    {
                        _selectedChannel.IsSelected = true;
                        if (_selectedChannel.Messages.Count == 0)
                        {
                            LoadHistory(25);
                        }
                    }
                    NotifyPropertyChanged();
                    NotifyPropertyChanged("NewMessages");
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
            SelectedChannel.IsSelected = false;
            //Release the lock to accept socket event
            _mutex.ReleaseMutex();
        }

        public override void AfterLogOut()
        {
            ChannelFilter = "";
            _users = new List<User>();
            SocketClient.MessageReceived += SocketClientOnMessageReceived;
            SocketClient.UserCreatedChannel += SocketClientOnUserCreatedChannel;
            SocketClient.UserJoinedChannel += SocketClientOnUserJoinedChannel;
            SocketClient.UserLeftChannel += SocketClientOnUserLeftChannel;
            SocketClient.UserDeletedChannel += SocketClientOnUserDeletedChannel;
            SocketClient.UserChangedName += SocketClientOnUserChangedName;
            Channels = new List<Channel>();
            //We block all socket event until the channels are import
            _mutex.WaitOne();
        }

        private void SocketClientOnUserChangedName(object source, EventArgs args)
        {
            var e = (ChatEventArgs) args;
            var user = _users.FirstOrDefault(u => u.ID == e.UserID);
            if (user != null)
            {
                user.Username = e.NewName;
            }
        }

        private void SocketClientOnUserDeletedChannel(object source, EventArgs args)
        {
            _mutex.WaitOne();
            try
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                        var e = (ChatEventArgs) args;
                        Channel channel = Channels.FirstOrDefault(c => c.ID == e.ChannelId);
                        if (channel == null)
                            return;
                        Channels.Remove(channel);
                        if (SelectedChannel == channel)
                        {
                            SelectedChannel = Channels.First(c => c.ID == GLOBAL_CHANNEL_ID);
                            if (e.UserID != Guid.Empty.ToString())
                            {
                                DialogHost.Show(new MessageDialog("Oups",
                                $"{e.Username} delete the channel '{channel.Name}' !"));
                            }
                        }
                        UpdateChannels();
                });
            }
            finally{
                _mutex.ReleaseMutex();
            }
        }

        private void SocketClientOnUserLeftChannel(object source, EventArgs args)
        {
            _mutex.WaitOne();
            try
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    var e = (ChatEventArgs) args;
                    Channel channel = Channels.FirstOrDefault(c => c.ID == e.ChannelId);
                    if (channel == null)
                        return;
                    Message m = new Message(e.Date, _admin, $"{e.Username} left the channel");
                    var user = channel.Users.FirstOrDefault(u => u.ID == e.UserID);
                    channel.Messages.Add(m);
                    if (channel.IsGame)
                        Channels.Remove(channel);
                    if(user != null)
                        channel.Users.Remove(user);
                    UpdateChannels();
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
                Application.Current.Dispatcher.Invoke(async () =>
                {
                        var e = (ChatEventArgs) args;
                        Channel channel = Channels.FirstOrDefault(c => c.ID == e.ChannelId);
                       if(channel == null){ 
                           return;
                       } 
                        Message m = new Message(e.Date, _admin, $"{e.Username} joined the channel");
                        channel.Users.Add(await GetUser(e.Username, e.UserID));
                        channel.Messages.Add(m);
                        // Select this channel if I am the user concern
                        if (e.UserID == SessionInformations.User.ID)
                        {
                            SelectedChannel = channel;
                        }
                        UpdateChannels();
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
                    ChatEventArgs e = (ChatEventArgs) args;
                    var newChannel = new Channel(e.ChannelName, e.ChannelId, e.IsGame);
                    Channels.Add(newChannel);
                    if (e.UserID == SessionInformations.User.ID)
                    {
                        JoinChannel(newChannel);
                    }
                    UpdateChannels();
                });
            }
            finally
            {
                _mutex.ReleaseMutex();
            }
        }

        private async void SocketClientOnMessageReceived(object source, EventArgs e)
        {
            var args = (ChatEventArgs) e;
            //TODO cache user 
            await App.Current.Dispatcher.InvokeAsync(async() =>
            {
                if (SessionInformations.User.ID != args.UserID)
                {
                    SoundService.PlayNotification();
                }
                Message m = new Message(args.Date, await GetUser(args.Username, args.UserID), args.Message);
                Channels.First(c => c.ID == args.ChannelId).Messages.Add(m);
                NotifyPropertyChanged(nameof(NewMessages));
            });
        }
        
        
        RelayCommand<int> _loadHistoryCommand;

        public ICommand LoadHistoryCommand
        {
            get
            {
                return _loadHistoryCommand ??
                       (_loadHistoryCommand = new RelayCommand<int>(numberOfMessages => LoadHistory(numberOfMessages), numberOfMessage => SelectedChannel!= null && SelectedChannel.Messages.Count > 0));
            }
        }

        private async Task LoadHistory(int numberOfMessages)
        {
            if (SelectedChannel != null && !SelectedChannel.IsFullyLoaded)
            {
                _mutex.WaitOne();
                var messages = await RestClient.GetChannelMessages(SelectedChannel.ID,
                    SelectedChannel.UserMessageCount,
                    SelectedChannel.UserMessageCount + numberOfMessages - 1);
                messages.Reverse();
                foreach (var message in messages)
                {
                    User u = message.User;
                    message.User = (await GetUser(u.Username, u.ID));
                }
                SelectedChannel.Messages =
                    new ObservableCollection<Message>(messages.Concat(SelectedChannel.Messages));
                if (messages.Count < numberOfMessages)
                {
                    SelectedChannel.IsFullyLoaded = true;
                }
                _mutex.ReleaseMutex();
            }
        }
        
        RelayCommand<Channel> _deleteChannelCommand;

        public ICommand DeleteChannelCommand
        {
            get
            {
                return _deleteChannelCommand ??
                       (_deleteChannelCommand = new RelayCommand<Channel>(channel => DeleteChannel(channel)));
            }
        }

        private void DeleteChannel(Channel channel)
        {
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.DeleteChannel, new Guid(channel.ID)));
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
            var dialog = new InputDialog("Enter the name for the new channel", 20);
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
            NotifyPropertyChanged(nameof(Channels));
            NotifyPropertyChanged(nameof(JoinedChannels));
            NotifyPropertyChanged(nameof(AvailableChannels));
            NotifyPropertyChanged(nameof(NewMessages));

            if (SelectedChannel == null || SessionInformations.User == null) return;
            if(SelectedChannel.Users.FirstOrDefault(u => u.ID == SessionInformations.User.ID) == null)
            {
                SelectedChannel = Channels.First(c => c.ID == GLOBAL_CHANNEL_ID);
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
                    tBox.Clear();
                    tBox.Focus();
                }
                catch (Exception e)
                {
                    DialogHost.Show(new ClosableErrorDialog(e));
                }
            }
        }

        private string _channelFilter;
        public string ChannelFilter
        {
            get { return _channelFilter.ToLower(); }
            set
            {
                if (value != _channelFilter)
                {
                    _channelFilter = value;
                    UpdateChannels();
                }
            }
        }

        public ObservableCollection<Channel> JoinedChannels
        {
            get
            {
                var channels =  new ObservableCollection<Channel>(_channels.Where(c =>
                   c.Name.ToLower().Contains(ChannelFilter) && c.Users.Select(m => m.ID).Contains(SessionInformations.User.ID)).OrderBy(c => c.Name));
                var globalChannel = channels.FirstOrDefault(c => c.ID == GLOBAL_CHANNEL_ID);
                if (globalChannel != null)
                {
                    channels.Move(channels.IndexOf(globalChannel), 0);
                }
                return channels;
            }
        }

        public ObservableCollection<Channel> AvailableChannels
        {
            get
            {
                var channels =  new ObservableCollection<Channel>(_channels.Where(c =>
                   c.Name.ToLower().Contains(ChannelFilter) && !c.Users.Select(m => m.ID).Contains(SessionInformations.User.ID)).OrderBy(c => c.Name));
                var globalChannel = channels.FirstOrDefault(c => c.ID == GLOBAL_CHANNEL_ID);
                if (globalChannel != null)
                {
                    channels.Move(channels.IndexOf(globalChannel), 0);
                }
                return channels;
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
                    UpdateChannels();
                }
            }
        }

        private List<Channel> _channels;
  
    }
}