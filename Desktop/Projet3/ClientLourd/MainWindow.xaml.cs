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
using ClientLourd.Views.Dialogs;
using ClientLourd.Views.Windows;
using ClientLourd.Utilities.Enums;
using ClientLourd.Services.EnumService;

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
            ((LoginViewModel) LoginScreen.DataContext).LoggedIn += OnLoggedIn;
            SetLanguageDictionary();
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
            (Home.DataContext as HomeViewModel).AfterLogin();
            //TODO: Remove this comment
            //DialogHost.Show(new Tutorial(), "Default");
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
            DevConfigButton.IsChecked = true;
        }


        /// <summary>
        /// Called when the chat is open or close
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ChatToggleButton_OnChecked(object sender, RoutedEventArgs e)
        {
            Task.Factory.StartNew(() =>
            {
                //Wait until the drawer is open
                Thread.Sleep(100);
                Application.Current.Dispatcher.InvokeAsync(() =>
                {
                    ChatBox.OnChatToggle(ChatToggleButton.IsChecked != null && (bool)ChatToggleButton.IsChecked);
                });
            });
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

        public object MainDialogHost { get; internal set; }

        private void ExportChat(object sender, RoutedEventArgs e)
        {
            Drawer.IsRightDrawerOpen = false;
            ChatToggleButton.IsEnabled = false;
            RightDrawerContent.Children.Clear();
            Task.Factory.StartNew(() =>
            {
                //Wait until the drawer is close
                Thread.Sleep(100);
                Application.Current.Dispatcher.InvokeAsync(() =>
                {
                    _chatWindow = new ChatWindow(ChatBox)
                    {
                        Title = "Chat",
                        DataContext = DataContext,
                        Owner = this,
                    };
                    _chatWindow.Show();
                });
            });
        }

        private void ConfigButton_OnClick(object sender, RoutedEventArgs e)
        {
            if (NetworkConfig.Visibility == Visibility.Hidden)
            {
                NetworkConfig.Visibility = Visibility.Visible;
                ConfigButton.Click -= ConfigButton_OnClick;
            }
            else
            {
                NetworkConfig.Visibility = Visibility.Hidden;
            }
        }

        private void CreateGame(object sender, RoutedEventArgs e)
        {
            DialogHost.Show(new GameCreationDialog(), "Default");
        }

        private void LeaveLobby(object sender, RoutedEventArgs e)
        {
            (Lobby.DataContext as LobbyViewModel).LeaveLobby();
        }


        private void SetLanguageDictionary()
        {
            ResourceDictionary dict = new ResourceDictionary();
            switch (Thread.CurrentThread.CurrentCulture.ToString())
            {
                case "en-US":
                    dict.Source = new Uri("..\\Resources\\Languages\\en.xaml", UriKind.Relative);
                    ((MainViewModel)DataContext).SelectedLanguage = Languages.EN.GetDescription();
                    break;
                case "fr-CA":
                    dict.Source = new Uri("..\\Resources\\Languages\\fr.xaml", UriKind.Relative);
                    ((MainViewModel)DataContext).SelectedLanguage = Languages.FR.GetDescription();
                    break;
                default:
                    dict.Source = new Uri("..\\Resources\\Languages\\en.xaml", UriKind.Relative);
                    ((MainViewModel)DataContext).SelectedLanguage = Languages.EN.GetDescription();
                    break;
            }
            Resources.MergedDictionaries.Add(dict);
            LanguageSelector.SelectionChanged += ChangeLang;
        }

        public void ChangeLang(object sender, EventArgs e)
        {
            ComboBox cmb = sender as ComboBox;
            string languageSelected = cmb.SelectedItem.ToString();
            ResourceDictionary dict = new ResourceDictionary();

            if (languageSelected == Languages.EN.GetDescription())
                dict.Source = new Uri("..\\Resources\\Languages\\en.xaml", UriKind.Relative);
            
            if (languageSelected == Languages.FR.GetDescription())
                dict.Source = new Uri("..\\Resources\\Languages\\fr.xaml", UriKind.Relative);
            
            Resources.MergedDictionaries[0] = dict;
        }

    }
}