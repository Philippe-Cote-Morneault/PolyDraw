using System;
using System.ComponentModel.Design;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Services.Rest;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Views;

namespace ClientLourd.ModelViews
{
    class MainViewModel: ViewModelBase
    {

        string _username;
        public RestClient _restClient;
        public SocketClient _socketClient;

        public MainViewModel()
        {
            _username = "";
            _restClient = new RestClient();
            _socketClient = new SocketClient();
        }

        private RelayCommand<LoginViewModel> _logoutCommand;

        public ICommand LogoutCommand
        {
            get
            {
                return _logoutCommand ?? (_logoutCommand = new RelayCommand<LoginViewModel>(lvm => Logout(lvm)));
            }
        }

        private void Logout(LoginViewModel lvm)
        {
            _socketClient.Close();
            lvm.IsLoggedIn = false;
            OnUserLogout(this);
        }
        public delegate void LogOutHandler(object source, EventArgs args);
        public event LogOutHandler UserLogout;

        public string Username
        {
            get
            {
                return _username;
            }

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
    }

    
}
