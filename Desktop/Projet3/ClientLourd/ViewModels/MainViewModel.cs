using System;
using System.Net.Sockets;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.ViewModels
{
    class MainViewModel : ViewModelBase
    {
        string _username;
        public RestClient RestClient { get; set; }
        public SocketClient SocketClient { get; set; }


        public MainViewModel()
        {
            Init();
        }


        public override void Init()
        {
            Username = "";
            RestClient = new RestClient();
            RestClient.StartWaiting += (source, args) => { IsWaiting = true; };
            RestClient.StopWaiting += (source, args) => { IsWaiting = false; };
            SocketClient = new SocketClient();
            SocketClient.StartWaiting += (source, args) => { IsWaiting = true; };
            SocketClient.StopWaiting += (source, args) => { IsWaiting = false; };
            SocketClient.ConnectionLost += SocketClientOnConnectionLost;
        }

        private bool _isWaiting;

        /// <summary>
        /// Indicate if the progress bar must be visible
        /// </summary>
        public bool IsWaiting
        {
            get { return _isWaiting; }
            set
            {
                _isWaiting = value;
                NotifyPropertyChanged();
            }
        }

        private RelayCommand<LoginViewModel> _logoutCommand;

        public ICommand LogoutCommand
        {
            get
            {
                return _logoutCommand ??
                       (_logoutCommand = new RelayCommand<LoginViewModel>(lvm => Logout(), lvm => !IsWaiting));
            }
        }

        private void Logout()
        {
            SocketClient.Close();
            OnUserLogout(this);
        }

        public delegate void LogOutHandler(object source, EventArgs args);

        public event LogOutHandler UserLogout;

        public string Username
        {
            get { return _username; }

            set
            {
                if (value != _username)
                {
                    _username = value;
                    NotifyPropertyChanged();
                }
            }
        }


        protected virtual void OnUserLogout(object source)
        {
            UserLogout?.Invoke(source, EventArgs.Empty);
        }

        private void SocketClientOnConnectionLost(object source, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke(delegate
            {
                DialogHost.Show(
                    new ClosableErrorDialog(
                        "You have lost connection to the server! Returning to the login page..."));
            });
            Logout();
        }
    }
}