using ClientLourd.Models.Bindable;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Constants;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Services.RestService;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.ViewModels
{
    class ProfileViewModel: ViewModelBase
    {
        private SessionInformations _sessionInformations;
        private PrivateProfileInfo _profileInfo;
        private Stats _stats;

        public override void AfterLogOut()
        {
        
        }


        public override void AfterLogin()
        {
            _sessionInformations = (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations as SessionInformations;
            Task task1 = GetUserInfo(_sessionInformations.User.ID);
            Task task2 = GetUserStats();
        }

        private async Task GetUserInfo(string userID)
        {
            ProfileInfo = await RestClient.GetUserInfo(userID);
        }

        private async Task GetUserStats()
        {
            Stats = await RestClient.GetStats();
        }

        public RestClient RestClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        public SessionInformations SessionInformations
        {
            get { return _sessionInformations; }
        }

        public PrivateProfileInfo ProfileInfo
        {
            get { return _profileInfo; }
            set
            {
                if (value != _profileInfo)
                {
                    _profileInfo = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public Stats Stats
        {
            get { return _stats; }
            set
            {
                if (value != _stats)
                {
                    _stats = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private RelayCommand<object> _closeProfileCommand;

        public ICommand CloseProfileCommand
        {
            get { return _closeProfileCommand ?? (_closeProfileCommand = new RelayCommand<object>(obj => CloseProfile(obj))); }
        }

        private async Task CloseProfile(object obj)
        {
            (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).ContainedView = Enums.Views.Editor.ToString();
        }

        private RelayCommand<object> _editProfileCommand;

        public ICommand EditProfileCommand
        {
            get { return _editProfileCommand ?? (_editProfileCommand = new RelayCommand<object>(obj => EditProfile(obj))); }
        }

        private async Task EditProfile(object obj)
        {
            await DialogHost.Show(new EditProfileDialog(ProfileInfo));
        }

        private RelayCommand<object> _openConnectionsCommand;

        public ICommand OpenConnectionsCommand
        {
            get { return _openConnectionsCommand ?? (_openConnectionsCommand = new RelayCommand<object>(obj => OpenConnectionHistory(obj))); }
        }

        private async Task OpenConnectionHistory(object obj)
        {
            int lastMessageIndex = 20;
            dynamic data = await RestClient.GetStats(0, lastMessageIndex);
            
            ConnectionHistoryDialog connectionDialog = new ConnectionHistoryDialog(data, lastMessageIndex);

            await DialogHost.Show(connectionDialog, (object o, DialogClosingEventArgs closingEventHandler) =>
                {
                    (((MainWindow)Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = false;
                });
        }


        private RelayCommand<object> _openGamesPlayedCommand;

        public ICommand OpenGamesPlayedCommand
        {
            get { return _openGamesPlayedCommand ?? (_openGamesPlayedCommand = new RelayCommand<object>(obj => OpenGamesPlayedHistory(obj))); }
        }

        private async Task OpenGamesPlayedHistory(object obj)
        {

            await DialogHost.Show(new GamesPlayedHistoryDialog(), (object o, DialogClosingEventArgs closingEventHandler) =>
            {
                            (((MainWindow)Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = false;
            });

        }




    }
}
