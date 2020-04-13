using ClientLourd.Models.Bindable;
using ClientLourd.Utilities.Commands;
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
using System.Collections.ObjectModel;

namespace ClientLourd.ViewModels
{
    class ProfileViewModel : ViewModelBase
    {
        private SessionInformations _sessionInformations;
        private User _user;
        private Stats _stats;
        private StatsHistory _statsHistory;
        public int _end;

        public ProfileViewModel()
        {
            _end = 20;
            ((MainWindow)Application.Current.MainWindow).ViewModel.LanguageChangedEvent += ViewModelOnLanguageChangedEvent;
        }

        public void AddStatsHistory(StatsHistory sh)
        {
            var tmpMatches = new ObservableCollection<MatchPlayed>(StatsHistory.MatchesPlayedHistory);
            StatsHistory.MatchesPlayedHistory.Clear();

            for (int i = 0; i < sh.MatchesPlayedHistory.Count; i++)
            {
                StatsHistory.MatchesPlayedHistory.Add(sh.MatchesPlayedHistory[i]);
            }

            for (int i = 0; i < tmpMatches.Count; i++)
            {
                StatsHistory.MatchesPlayedHistory.Add(tmpMatches[i]);
            }

            NotifyPropertyChanged(nameof(StatsHistory));
        }

        private void ViewModelOnLanguageChangedEvent(object source, EventArgs args)
        {
            if (StatsHistory != null)
            {
                NotifyPropertyChanged(nameof(StatsHistory));
            }
        }

        public override void AfterLogOut()
        {
        }

        public void GetAllStats()
        {
            try
            {
                Task task2 = GetUserStats();
                Task task3 = GetUserStats(0, _end);
            }
            catch
            {
                //
            }
        }


        public override void AfterLogin()
        {
            _sessionInformations =
                (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations as
                SessionInformations;
            Task task1 = GetUserInfo(_sessionInformations.User.ID);
            GetAllStats();
        }

        private async Task GetUserInfo(string userID)
        {
            //TODO maybe it should be in mainviewmodel ?
            User = await RestClient.GetUserInfo(userID);
        }

        private async Task GetUserStats()
        {
            Stats = await RestClient.GetStats();
        }

        private async Task GetUserStats(int start, int end)
        {
            StatsHistory = await RestClient.GetStats(start, end);
            NotifyPropertyChanged(nameof(StatsHistory));
        }


        public StatsHistory StatsHistory
        {
            get => _statsHistory;
            set
            {
                NotifyPropertyChanged();
                _statsHistory = value;
            }
        }



        public RestClient RestClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        public SessionInformations SessionInformations
        {
            get {
                return
               (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations as
             SessionInformations; }
        }

        public User User
        {
            get
            {
                return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                    ?.SessionInformations.User;
            }
            set
            {
                (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel).SessionInformations.User =
                    value;
                NotifyPropertyChanged();
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
            get
            {
                return _closeProfileCommand ??
                       (_closeProfileCommand = new RelayCommand<object>(obj => CloseProfile(obj)));
            }
        }

        private async Task CloseProfile(object obj)
        {
            (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel).ContainedView =
                Utilities.Enums.Views.Editor.ToString();
        }

        private RelayCommand<object> _editProfileCommand;

        public ICommand EditProfileCommand
        {
            get
            {
                return _editProfileCommand ?? (_editProfileCommand = new RelayCommand<object>(obj => EditProfile(obj)));
            }
        }

        private async Task EditProfile(object obj)
        {
            await DialogHost.Show(new EditProfileDialog());
        }

        private RelayCommand<object> _openConnectionsCommand;

        public ICommand OpenConnectionsCommand
        {
            get
            {
                return _openConnectionsCommand ??
                       (_openConnectionsCommand = new RelayCommand<object>(obj => OpenConnectionHistory(obj)));
            }
        }

        private async Task OpenConnectionHistory(object obj)
        {
            ConnectionHistoryDialog connectionDialog = new ConnectionHistoryDialog(StatsHistory, _end);

            await DialogHost.Show(connectionDialog,
                (object o, DialogClosingEventArgs closingEventHandler) =>
                {
                    (((MainWindow) Application.Current.MainWindow).MainWindowDialogHost as DialogHost)
                        .CloseOnClickAway = false;
                });
        }


    }
}