using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Web.UI.WebControls;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using ClientLourd.Views;
using MaterialDesignThemes.Wpf;
using ClientLourd.Utilities.Commands;
using ClientLourd.ViewModels;
using ClientLourd.Views.Windows;

namespace ClientLourd
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            ((MainViewModel) DataContext).UserLogout += OnUserLogout;
            ((LoginViewModel)LoginScreen.DataContext).LoggedIn += OnLoggedIn;
        }

        private void OnLoggedIn(object source, EventArgs args)
        {
            
            var loginViewModel = (LoginViewModel) source;
            Dispatcher.Invoke(() =>
            {
                AfterLogin(loginViewModel);
                ChatBox.AfterLogin();
                LoginScreen.AfterLogin();
            });
        }

        private void AfterLogin(LoginViewModel loginViewModel)
        {
            var mainViewModel = (MainViewModel) DataContext;
            mainViewModel.SessionInformations.Tokens = loginViewModel.Tokens;
            mainViewModel.SessionInformations.User = loginViewModel.User;
            mainViewModel.AfterLogin();
            (Profile.DataContext as ProfileViewModel).AfterLogin();
        }

        private void OnUserLogout(object source, EventArgs args)
        {
            Dispatcher.Invoke(() =>
            {
                AfterLogout();
                ChatBox.AfterLogout();
                LoginScreen.AfterLogout();
            });
        }

        private void AfterLogout()
        {
            ((ViewModelBase) DataContext).AfterLogOut();
            MenuToggleButton.IsChecked = false;
            ChatToggleButton.IsChecked = false;
            _chatWindow?.Close();
        }

        /// <summary>
        /// Clear the chat notification when the chat is open or close
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ChatToggleButton_OnChecked(object sender, RoutedEventArgs e)
        {
            ClearChatNotification();
            Task.Factory.StartNew(() =>
            {
                //Wait until the drawer is open
                Thread.Sleep(100);
                Application.Current.Dispatcher.InvokeAsync(() =>
                {
                    ChatBox.OnChatOpen();
                });
            });
        }

        public void ClearChatNotification()
        {
            //Clear the notification when chatToggleButton is checked or unchecked
            ((ChatViewModel) ChatBox.DataContext).ClearNotificationCommand.Execute(null);
        }

        private ChatWindow _chatWindow;

        RelayCommand<object> _exportChatCommand;

        /// <summary>
        /// Command use to export the chat as an external window
        /// </summary>
        public ICommand ExportChatCommand
        {
            get
            {
                return _exportChatCommand ??
                       (_exportChatCommand = new RelayCommand<object>(param => ExportChat(this, null),
                           o => ChatToggleButton.IsEnabled));
            }
        }

        private void ExportChat(object sender, RoutedEventArgs e)
        {
            Drawer.IsRightDrawerOpen = false;
            RightDrawerContent.Children.Clear();
            _chatWindow = new ChatWindow(ChatBox)
            {
                Title = "Chat",
                DataContext = DataContext,
                Owner = this,
            };
            ChatToggleButton.IsEnabled = false;
            _chatWindow.Show();
        }
    }
}