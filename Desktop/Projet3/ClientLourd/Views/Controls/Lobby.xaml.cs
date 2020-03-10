using ClientLourd.Models.Bindable;
using ClientLourd.Services.SocketService;
using ClientLourd.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

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
                   
                    ChatContainer.Content = null;
                    MainWindow.ChatToggleButton.IsEnabled = true;
                    MainWindow.RightDrawerContent.Children.Add(MainWindow.ChatBox);
                }
                
            });
        }

        public void ExportChat()
        {
            MainWindow.Drawer.IsRightDrawerOpen = false;
            MainWindow.ChatToggleButton.IsEnabled = false;
            MainWindow.RightDrawerContent.Children.Clear();
            ChatContainer.Content = MainWindow.ChatBox;
        }
    }
}
