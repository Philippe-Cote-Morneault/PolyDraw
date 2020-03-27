using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Utilities.Enums;
using MaterialDesignThemes.Wpf;
using ClientLourd.Views.Dialogs;
using ClientLourd.Services.SoundService;
using System.Windows.Media.Imaging;
using ClientLourd.Services.ProfileViewerService;
using ClientLourd.Utilities.Constants;

namespace ClientLourd.ViewModels
{
    class LobbyViewModel : ViewModelBase
    {
        private bool _canStart;
        public bool CanStart
        {
            get
            {
                return _canStart;
            }
            set
            {
                _canStart = value;
                NotifyPropertyChanged();
            }
        }

        private void SocketClientOnMatchEnded(object source, EventArgs args)
        {
            Task.Run(() =>
            {
                Thread.Sleep(MatchTiming.GAME_ENDED_TIMEOUT);
                Application.Current.Dispatcher.Invoke(() =>
                {
                    LeaveLobby();
                });
            });
        }

        private bool _isGameStarted;
        public bool IsGameStarted
        {
            get => _isGameStarted;
            set
            {
                _isGameStarted = value;
                NotifyPropertyChanged();
            }
        }
        public SocketClient SocketClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }
        public RestClient RestClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        public string ContainedView
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.ContainedView;
            }
            set
            {
                (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).ContainedView = value;
            }
        }

        public MainWindow MainWindow
        {
            get
            {
                return (MainWindow)Application.Current.MainWindow;
            }
        }


        public HomeViewModel HomeViewModel
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.Home?.DataContext as HomeViewModel);
            }
        }


        public Lobby CurrentLobby
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.CurrentLobby; }
            set { (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).CurrentLobby = value; NotifyPropertyChanged(); }
        }

        public SessionInformations SessionInformations
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations;
            }
        }

        public override void AfterLogin()
        {
            SocketClient.JoinLobbyResponse += OnJoinLobbyResponse;
            SocketClient.LobbyDeleted += OnLobbyDeleted;
            SocketClient.UserJoinedLobby += OnUserJoinedLobby;
            SocketClient.UserLeftLobby += OnUserLeftLobby;
            SocketClient.StartGameResponse += OnStartGameResponse;
            SocketClient.MatchEnded += SocketClientOnMatchEnded;
            IsGameStarted = false;
        }

        public override void AfterLogOut()
        {
            throw new NotImplementedException();
        }

        private RelayCommand<object> _leaveLobbyCommand;

        public ICommand LeaveLobbyCommand
        {
            get
            {
                return _leaveLobbyCommand ?? (_leaveLobbyCommand = new RelayCommand<object>(obj => LeaveLobby()));
            }
        }

        public void LeaveLobby()
        {
            CurrentLobby = null;
            if (!IsGameStarted)
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.QuitLobbyRequest));
            else
            {
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.LeaveMatch));
                IsGameStarted = false;
            }

            HomeViewModel.FetchLobbies();
            ContainedView = Utilities.Enums.Views.Home.ToString();
        }

        private void OnJoinLobbyResponse(object sender, EventArgs e)
        {
            var joinLobbyArgs = (LobbyEventArgs)e;
            if (joinLobbyArgs.Response)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    //Trigger NotifyProperty...
                    NotifyPropertyChanged(nameof(CurrentLobby));
                    CanStart = CanStartGame();
                });
            }
        }

        private void OnLobbyDeleted(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                var lobbyDeletedArgs = (LobbyEventArgs)e;

                string lobbyDeletedID = new Guid(lobbyDeletedArgs.Bytes).ToString();

                if (UserIsInLobby(lobbyDeletedID) && !UserIsHost())
                {
                    CurrentLobby = null;
                    ContainedView = Utilities.Enums.Views.Home.ToString();
                    DialogHost.Show(new ClosableErrorDialog($"The host has left the lobby!"), "Default");
                }
            });
        }

        private bool UserIsInLobby(string lobbyID)
        {
            if (CurrentLobby == null)
            {
                return false;
            }

            if (CurrentLobby.ID != lobbyID)
            {
                return false;
            }

            foreach (Player player in CurrentLobby.Players)
            {
                if (player.User.ID == SessionInformations.User.ID)
                {
                    return true;
                }
            }

            return false;
        }

        private bool UserIsHost()
        {
            if (SessionInformations != null && SessionInformations.User != null)
            {
                return CurrentLobby.HostID == SessionInformations.User.ID;
            }

            return false;
        }

        private RelayCommand<object> _startGameCommand;

        public ICommand StartGameCommand
        {
            get
            {
                return _startGameCommand ?? (_startGameCommand = new RelayCommand<object>(obj => StartGame()));
            }
        }

        private void StartGame()
        {
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.StartGameRequest));
        }


        private bool CanStartGame()
        {
            if (CurrentLobby == null || !UserIsHost())
            {
                return false;
            }

            // If solo
            if (CurrentLobby.Mode == GameModes.Solo && CurrentLobby.PlayersCount == 1)
            {
                return true;
            }


            // Count number of players if FFA or coop
            int nHumanPlayers = 0;

            if (CurrentLobby != null)
            {

                foreach (Player player in CurrentLobby.Players)
                {
                    if (!player.User.IsCPU)
                    {
                        nHumanPlayers++;
                    }
                }
            }

            return nHumanPlayers >= 2;
        }


        private void OnStartGameResponse(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                var gameStartedArgs = (LobbyEventArgs)e;
                if (gameStartedArgs.Response)
                {
                    IsGameStarted = true;
                }
                else
                {
                    DialogHost.Show(new ClosableErrorDialog($"{gameStartedArgs.Error}"), "Default");
                }

            });
        }

        private void OnUserJoinedLobby(object sender, EventArgs e)
        {

            Application.Current.Dispatcher.Invoke(() =>
            {
                NotifyPropertyChanged(nameof(CurrentLobby));
                CanStart = CanStartGame();
            });
        }

        private void OnUserLeftLobby(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                LobbyEventArgs lobbyEventArgs = (LobbyEventArgs)e;
                if (SessionInformations.User.ID == lobbyEventArgs.UserID)
                {
                    HomeViewModel.FetchLobbies();
                    ContainedView = Utilities.Enums.Views.Home.ToString();
                }

                NotifyPropertyChanged(nameof(CurrentLobby));
                CanStart = CanStartGame();
            });
        }


        private RelayCommand<Player> _kickPlayerCommand;

        public ICommand KickPlayerCommand
        {
            get
            {
                return _kickPlayerCommand ?? (_kickPlayerCommand = new RelayCommand<Player>(obj => KickPlayer(obj)));
            }
        }

        public async void  KickPlayer(Player player)
        {
            ConfirmationDialog confirmationDialog = new ConfirmationDialog($"Remove player", $"Are you sure you want to remove the player {player.User.Username}?");
            confirmationDialog.Height = 300;
            confirmationDialog.MessageTextBlock.Margin = new Thickness(30, 0, 30, 0);
            var response = await DialogHost.Show(confirmationDialog, "Default");
            if (bool.Parse(response.ToString()))
            {
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.KickPlayer, new Guid(player.User.ID)));
            }
        }

        private RelayCommand<Player> _addVirtualPlayerCommand;

        public ICommand AddVirtualPlayerCommand
        {
            get
            {
                return _addVirtualPlayerCommand ?? (_addVirtualPlayerCommand = new RelayCommand<Player>(obj => AddVirtualPlayer()));
            }
        }

        public void AddVirtualPlayer()
        {
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.AddVirtualPlayer, new { nbVirtualPlayer = 1 }));
        }

        public ICommand ViewPublicProfile
        {
            get { return ProfileViewer.ViewPublicProfileCommand; }
        }

    }
}
