using System;
using System.Net.Sockets;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using ClientLourd.Utilities.Enums;

namespace ClientLourd.ViewModels
{
    class MainViewModel : ViewModelBase
    {
        string _containedView;
       
        public RestClient RestClient { get; set; }
        public SocketClient SocketClient { get; set; }
        public NetworkInformations NetworkInformations { get; set; }
        private SessionInformations _sessionInformations;

        public SessionInformations SessionInformations
        {
            get { return _sessionInformations; }
            set
            {
                if (value != _sessionInformations)
                {
                    _sessionInformations = value;
                    NotifyPropertyChanged();
                }
            }
        }


        public MainViewModel()
        {
            AfterLogOut();
        }

        public override void AfterLogin()
        {
            //TODO

        }

        public override void AfterLogOut()
        {
            NetworkInformations = new NetworkInformations();
            SessionInformations = new SessionInformations();
            ContainedView = Utilities.Enums.Views.Editor.ToString();
            RestClient = new RestClient(NetworkInformations);
            RestClient.StartWaiting += (source, args) => { IsWaiting = true; };
            RestClient.StopWaiting += (source, args) => { IsWaiting = false; };
            SocketClient = new SocketClient(NetworkInformations);
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
        
        private RelayCommand<string> _changeNetworkCommand;
        public ICommand ChangeNetworkCommand
        {
            get
            {
                return _changeNetworkCommand ??
                       (_changeNetworkCommand = new RelayCommand<string>(config => NetworkInformations.Config = int.Parse(config)));
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

        public string ContainedView
        {
            get { return _containedView; }

            set
            {
                if (value != _containedView)
                {
                    _containedView = value;
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

        private RelayCommand<object> _myProfileCommand;

        public ICommand MyProfileCommand
        {
            get
            {
                return _myProfileCommand ?? (_myProfileCommand = new RelayCommand<object>(obj => MyProfile()));
            }
        }

        private void MyProfile()
        {
            ContainedView = Utilities.Enums.Views.Profile.ToString();
        }

        private RelayCommand<object> _homeCommand;

        public ICommand HomeCommand
        {
            get
            {
                return _homeCommand ?? (_homeCommand = new RelayCommand<object>(obj => Home()));
            }
        }

        private void Home()
        {
            // TODO: Change to home view
            ContainedView = Utilities.Enums.Views.Editor.ToString();
        }

    }
}