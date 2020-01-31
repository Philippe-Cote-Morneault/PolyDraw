using System;
using System.ComponentModel.Design;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Services.Rest;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Views;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.ModelViews
{
    class MainViewModel: ViewModelBase
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
            SocketClient = new SocketClient();
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
            SocketClient.Close();
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
