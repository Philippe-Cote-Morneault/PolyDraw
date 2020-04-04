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
using ClientLourd.Services.SocketService;
using ClientLourd.Views.Controls;
using ClientLourd.Services.UserSettingsManagerService;

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

        private void SocketClientOnConnectionLost(object source, EventArgs args)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                DialogHost.CloseDialogCommand.Execute(null, MainWindowDialogHost);
                
                MainWindowDialogHost.ShowDialog(new ClosableErrorDialog(((MainViewModel)DataContext).CurrentDictionary["LostConnection"].ToString()));

            });
        }

        private void OnLoggedIn(object source, EventArgs args)
        {
            var loginViewModel = (LoginViewModel) source;
            Dispatcher.Invoke(() =>
            {
                (DataContext as MainViewModel).SocketClient.ConnectionLost += SocketClientOnConnectionLost;
                AfterLogin(loginViewModel);
                ChatBox.AfterLogin();
                LoginScreen.AfterLogin();
                Profile.AfterLogin();
                Home.AfterLogin();
                Lobby.AfterLogin();
            });
        }

        private void AfterLogin(LoginViewModel loginViewModel)
        {
            var mainViewModel = (MainViewModel) DataContext;
            mainViewModel.SessionInformations.Tokens = loginViewModel.Tokens;
            mainViewModel.SessionInformations.User = loginViewModel.User;
            mainViewModel.AfterLogin();

            var userSettingsManager = new UserSettingsManagerService(mainViewModel.SessionInformations.User.ID);
            if (!userSettingsManager.HasSeenTutorial())
            {
                DialogHost.Show(new Tutorial(), "Default");
            }
        }

        private void OnUserLogout(object source, EventArgs args)
        {
            Dispatcher.Invoke(() =>
            {
                AfterLogout();
                ChatBox.AfterLogout();
                LoginScreen.AfterLogout();
                Lobby.AfterLogout();
                Home.AfterLogout();
                Profile.AfterLogout();
            });
        }

        private void AfterLogout()
        {
            ((ViewModelBase) DataContext).AfterLogOut();
            MenuToggleButton.IsChecked = false;
            ReturnTheChat();
            ProdConfigButton.IsChecked = true;
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



        public ChatWindow ChatWindow { get; set; }

        RelayCommand<object> _exportChatCommand;

        /// <summary>
        /// Command use to export the chat as an external window
        /// </summary>
        public ICommand ExportChatCommand
        {
            get
            {
                return _exportChatCommand ??
                       (_exportChatCommand = new RelayCommand<object>(param => ExportChatAsNewWindow(this, null),
                           o => ChatToggleButton.IsEnabled));
            }
        }

        /// <summary>
        /// Remove the chat from his current parent
        /// </summary>
        /// <returns></returns>
        public Chat GetChat()
        {
            //Close the chat window
            ChatWindow?.Close();
            //Close the chat
            Drawer.IsRightDrawerOpen = false;
            //Hide the chat button
            ChatToggleButton.IsEnabled = false;
            //Remove the chat from his parent
            ((Grid)ChatBox.Parent)?.Children.Clear();
            //Disable notification for the current channel
            Task.Delay(100).ContinueWith((t) =>
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ((ChatViewModel) ChatBox.DataContext).OnChatToggle(true);
                });
            });
            return ChatBox;
        }
        
        /// <summary>
        /// Return the chat to the mainwindow if the parent is null or a grid
        /// </summary>
        public void ReturnTheChat()
        {
            ((Grid)ChatBox.Parent)?.Children.Clear();
            MainWindowChatContainer.Children.Add(ChatBox);
            ChatToggleButton.IsEnabled = true;
            //Close the chat
            Drawer.IsRightDrawerOpen = false;
            //enable notification for the current channel
            Task.Delay(100).ContinueWith((t) =>
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ((ChatViewModel)ChatBox.DataContext).OnChatToggle(false);
                });
            });
        }

        private void ExportChatAsNewWindow(object sender, RoutedEventArgs e)
        {
            Chat chat = GetChat();
            Task.Factory.StartNew(() =>
            {
                //Wait until the drawer is close
                Thread.Sleep(100);
                Application.Current.Dispatcher.InvokeAsync(() =>
                {
                    ChatWindow = new ChatWindow(chat)
                    {
                        Title = "Chat",
                        DataContext = DataContext,
                        Owner = this,
                    };
                    ChatWindow.Show();
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


        /// <summary>
        /// Sets language based on system language
        /// </summary>
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
            (DataContext as MainViewModel).CurrentDictionary = dict;
            (DataContext as MainViewModel).TriggerLangChangedEvent();
            (DataContext as MainViewModel).IsSystemLanguage = true;
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
            (DataContext as MainViewModel).CurrentDictionary = dict;
            Resources.MergedDictionaries[0] = dict;
            (DataContext as MainViewModel).IsSystemLanguage = false;
            (DataContext as MainViewModel).TriggerLangChangedEvent();

        }

        public static readonly DependencyProperty ScaleValueProperty = DependencyProperty.Register("ScaleValue", typeof(double), typeof(MainWindow), new UIPropertyMetadata(1.0, new PropertyChangedCallback(OnScaleValueChanged), new CoerceValueCallback(OnCoerceScaleValue)));
    
        private static object OnCoerceScaleValue(DependencyObject o, object value)
        {
            MainWindow mainWindow = o as MainWindow;
            if (mainWindow != null)
                return mainWindow.OnCoerceScaleValue((double)value);
            else
                return value;
        }
    
        private static void OnScaleValueChanged(DependencyObject o, DependencyPropertyChangedEventArgs e)
        {
            MainWindow mainWindow = o as MainWindow;
            if (mainWindow != null)
                mainWindow.OnScaleValueChanged((double)e.OldValue, (double)e.NewValue);
        }
    
        protected virtual double OnCoerceScaleValue(double value)
        {
            if (double.IsNaN(value))
                return 1.0f;
    
            value = Math.Max(0.1, value);
            return value;
        }
    
        protected virtual void OnScaleValueChanged(double oldValue, double newValue)
        {
    
        }
    
        public double ScaleValue
        {            
            get => (double)GetValue(ScaleValueProperty);
            set
            {
                SetValue(ScaleValueProperty, value);
            }
        }
    
        private void MainGrid_SizeChanged(object sender, EventArgs e)
        {
            CalculateScale();
        }
    
        private void CalculateScale()
        {
            double yScale = ActualHeight / 1080;
            double xScale = ActualWidth / 1920;
            double value = Math.Min(xScale, yScale);
            ScaleValue = (double)OnCoerceScaleValue(PolyDraw, value);
        }

        private void LogoutItem_OnMouseUp(object sender, MouseButtonEventArgs e)
        {
            if (ViewModel.LogoutCommand.CanExecute(LoginScreen.DataContext))
            {
                ViewModel.LogoutCommand.Execute(LoginScreen.DataContext);
            }
        }

        private void ProfileItem_OnMouseUp(object sender, MouseButtonEventArgs e)
        {
            if (ViewModel.MyProfileCommand.CanExecute(null))
            {
                Profile.UpdateStats();
                ViewModel.MyProfileCommand.Execute(null);
            }
        }

        private void HomeItem_OnMouseUp(object sender, MouseButtonEventArgs e)
        {
            if (ViewModel.HomeCommand.CanExecute(null))
            {
                ViewModel.HomeCommand.Execute(null);
            }
        }

        private void OpenTutorial(object sender, MouseButtonEventArgs e)
        {
            DialogHost.Show(new Tutorial(), "Default");
        }
    }
}