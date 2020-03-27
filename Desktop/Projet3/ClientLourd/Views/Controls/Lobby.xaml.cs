using ClientLourd.Models.Bindable;
using ClientLourd.Services.SocketService;
using ClientLourd.ViewModels;
using System;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using ClientLourd.Utilities.Constants;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Lobby.xaml
    /// </summary>
    public partial class Lobby : UserControl
    {

        public MainWindow MainWindow
        {
            get
            {
                return (MainWindow)Application.Current.MainWindow;
            }
        }

        public SocketClient SocketClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }

        public SessionInformations SessionInformations
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations;
            }
        }


        public Models.Bindable.Lobby CurrentLobby
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.CurrentLobby; }
        }

        public Lobby()
        {
            InitializeComponent();
            SocketClient.JoinLobbyResponse += OnJoinLobbyResponse;
            SocketClient.UserLeftLobby += OnUserLeftLobby;
            SocketClient.PlayerLeftMatch += SocketClientOnPlayerLeftMatch;
            SocketClient.StartGameResponse += SocketClientOnGameResponse;
            SocketClient.MatchEnded += SocketClientOnMatchEnded;
        }

        private void SocketClientOnMatchEnded(object source, EventArgs args)
        {
            Task.Delay(MatchTiming.GAME_ENDED_TIMEOUT).ContinueWith((t) =>
            {
                Application.Current.Dispatcher.Invoke(() => { returnChat(); });
            });
        }
        private void SocketClientOnGameResponse(object source, EventArgs args)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                Chat chat = (Chat)ChatContainer.Content;
                var channel =
                ((ChatViewModel)chat.DataContext).Channels.FirstOrDefault(c => c.IsGame == true);
                ((GameViewModel)GameView.DataContext).GameChannel = channel;
            });
        }

        private void SocketClientOnPlayerLeftMatch(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Application.Current.Dispatcher.Invoke(() =>
            {
                if (e.UserID == SessionInformations.User.ID)
                {
                    returnChat();
                }
            });
        }


        private void OnJoinLobbyResponse(object sender, EventArgs e)
        {
            var joinLobbyArgs = (LobbyEventArgs)e;
            if (joinLobbyArgs.Response)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ExportChat();
                });
            }
        }
        

        private void OnUserLeftLobby(object sender, EventArgs e)
        {
            var userLeftLobbyArgs = (LobbyEventArgs)e;

            Application.Current.Dispatcher.Invoke(() =>
            {    
                if (SessionInformations.User.ID == userLeftLobbyArgs.UserID || (CurrentLobby != null && CurrentLobby.HostID == userLeftLobbyArgs.UserID))
               {
                    returnChat();
                }
                
            });
        }

        public void returnChat()
        {
            ChatContainer.Content = null;
            MainWindow.ChatToggleButton.IsEnabled = true;
            MainWindow.RightDrawerContent.Children.Add(MainWindow.ChatBox);
        }

        public void ExportChat()
        {
            MainWindow.ChatWindow?.Close();
            MainWindow.Drawer.IsRightDrawerOpen = false;
            MainWindow.ChatToggleButton.IsEnabled = false;
            MainWindow.RightDrawerContent.Children.Clear();
            ChatContainer.Content = MainWindow.ChatBox;
            Task.Delay(50).ContinueWith(_ =>
            {
                Application.Current.Dispatcher.Invoke(new Action(() =>
                {
                    Chat chat = ChatContainer.Content as Chat;
                    chat.MessageTextBox.Focus();
                }));
            });
        }
    }
}
