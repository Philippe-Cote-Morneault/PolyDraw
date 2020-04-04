using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ClientLourd.Utilities.Enums;
using ClientLourd.Services.SocketService;
using System.Windows;
using ClientLourd.Utilities.Commands;
using System.Windows.Input;
using MaterialDesignThemes.Wpf;
using ClientLourd.Views.Dialogs;
using ClientLourd.Services.RestService;
using System.Windows.Media.Imaging;

namespace ClientLourd.ViewModels
{
    public class HomeViewModel : ViewModelBase
    {
        private ObservableCollection<Lobby> _lobbies;

        public string ContainedView
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.ContainedView; }
            set { (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel).ContainedView = value; }
        }


        public SocketClient SocketClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }

        public RestClient RestClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        public SessionInformations SessionInformations
        {
            get
            {
                return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                    ?.SessionInformations;
            }
        }

        public Lobby CurrentLobby
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.CurrentLobby; }
            set { (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel).CurrentLobby = value; }
        }


        public MainViewModel MainViewModel
        {
            get => (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel);
        }

        public async override void AfterLogin()
        {
            FetchLobbies();

            SocketClient.LobbyCreated += OnLobbyCreated;
            SocketClient.JoinLobbyResponse += OnJoinLobbyResponse;
            SocketClient.UserJoinedLobby += OnUserJoinedLobby;
            SocketClient.UserLeftLobby += OnUserLeftLobby;
            SocketClient.LobbyDeleted += OnLobbyDeleted;
            Lobbies = new ObservableCollection<Lobby>();
            _modeFilteredAscending = false;
            _hostFilteredAscending = false;
            _playerCountFilteredAscending = false;
            _languageFilteredAscending = false;
            _durationFilteredAscending = false;
            MainViewModel.LanguageChangedEvent += OnLanguageChanged;
        }

        private void OnLanguageChanged(object source, EventArgs args)
        {
            foreach (Lobby lobby in Lobbies)
            {
                // Line below doesnt work for some reason (binding is trash in WPF)
                // NotifyPropertyChanged(nameof(lobby.Difficulty));
                lobby.Difficulty = lobby.Difficulty;

                lobby.Mode = lobby.Mode;
            }
        }

        public override void AfterLogOut()
        {
            //??
        }

        public ObservableCollection<Lobby> Lobbies
        {
            get => _lobbies;
            set
            {
                if (value != _lobbies)
                {
                    _lobbies = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public async void FetchLobbies()
        {
            Lobbies = await RestClient.GetGroup();
        }


        private bool IsCreatedByUser(string ownerID)
        {
            return ownerID == SessionInformations.User.ID;
        }


        private bool _modeFilteredAscending;

        private void FilterMode()
        {
            if (!_modeFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => (int) lobby.Mode).ToList());
                _modeFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => (int) lobby.Mode)
                    .ToList());
                _modeFilteredAscending = false;
            }
        }


        private bool _hostFilteredAscending;

        private void FilterHost()
        {
            if (!_hostFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => lobby.Host).ToList());
                _hostFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => lobby.Host).ToList());
                _hostFilteredAscending = false;
            }
        }


        private bool _playerCountFilteredAscending;

        private void FilterPlayerCount()
        {
            if (!_playerCountFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => lobby.PlayersCount).ToList());
                _playerCountFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => lobby.PlayersCount)
                    .ToList());
                _playerCountFilteredAscending = false;
            }
        }

        private bool _durationFilteredAscending;

        private void FilterDuration()
        {
            if (!_durationFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies
                    .OrderBy((lobby) => TimeSpan.FromTicks(lobby.Duration.Ticks).TotalMinutes).ToList());
                _durationFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies
                    .OrderByDescending((lobby) => TimeSpan.FromTicks(lobby.Duration.Ticks).TotalMinutes).ToList());
                _durationFilteredAscending = false;
            }
        }

        private bool _languageFilteredAscending;

        private void FilterLanguage()
        {
            if (!_languageFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => (int) lobby.Language).ToList());
                _languageFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => (int) lobby.Language)
                    .ToList());
                _languageFilteredAscending = false;
            }
        }


        private bool _difficultyFilteredAscending;

        private void FilterDifficulty()
        {
            if (!_difficultyFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => (int) lobby.Difficulty).ToList());
                _difficultyFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => (int) lobby.Difficulty)
                    .ToList());
                _difficultyFilteredAscending = false;
            }
        }

        private RelayCommand<string> _filterLobbies;

        public ICommand FilterLobbiesCommand
        {
            get
            {
                return _filterLobbies ??
                       (_filterLobbies = new RelayCommand<string>(attribute => FilterLobbies(attribute)));
            }
        }


        private void FilterLobbies(string attribute)
        {
            if (attribute == "Mode")
                FilterMode();
            if (attribute == "Host")
                FilterHost();
            if (attribute == "Players")
                FilterPlayerCount();
            if (attribute == "Difficulty")
                FilterDifficulty();
            if (attribute == "Language")
                FilterLanguage();
            if (attribute == "Duration")
                FilterDuration();
        }

        public void JoinLobby(Lobby lobby)
        {
            CurrentLobby = lobby;
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.JoinLobbyRequest, new Guid(lobby.ID)));
        }

        private void OnLobbyCreated(object sender, EventArgs e)
        {
            var lobbyCreated = (LobbyEventArgs) e;
            Application.Current.Dispatcher.Invoke(async () =>
            {
                Lobby lobby = new Lobby(
                    lobbyCreated.ID,
                    lobbyCreated.OwnerName,
                    lobbyCreated.OwnerID,
                    lobbyCreated.Players,
                    (GameModes) lobbyCreated.Mode,
                    (DifficultyLevel) lobbyCreated.Difficulty,
                    lobbyCreated.Players.Count,
                    lobbyCreated.PlayersMax,
                    lobbyCreated.Language,
                    lobbyCreated.Rounds
                );
                Lobbies.Insert(0, lobby);
                if (IsCreatedByUser(lobbyCreated.OwnerID))
                {
                    CurrentLobby = lobby;
                    CurrentLobby.Host = lobbyCreated.OwnerName;
                }
            });
        }

        private void OnLobbyDeleted(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                var lobbyDeletedArgs = (LobbyEventArgs) e;

                string lobbyDeletedID = new Guid(lobbyDeletedArgs.Bytes).ToString();

                var lobbyToDelete = FindLobby(lobbyDeletedID);

                if (lobbyToDelete != null)
                {
                    Lobbies.Remove(lobbyToDelete);
                }

                FetchLobbies();
            });
        }


        private Lobby FindLobby(string lobbyID)
        {
            foreach (Lobby lobby in Lobbies)
            {
                if (lobby.ID == lobbyID)
                {
                    return lobby;
                }
            }

            return null;
        }


        private void OnJoinLobbyResponse(object sender, EventArgs e)
        {
            var joinLobbyArgs = (LobbyEventArgs) e;

            Application.Current.Dispatcher.Invoke(() =>
            {
                if (joinLobbyArgs.Response)
                {
                    ContainedView = Utilities.Enums.Views.Lobby.ToString();
                }
                else
                {
                    DialogHost.Show(new ClosableErrorDialog($"{joinLobbyArgs.Error}"), "Default");
                }
            });
        }

        private void OnUserJoinedLobby(object sender, EventArgs e)
        {
            var userJoinedLobbyArgs = (LobbyEventArgs) e;

            Application.Current.Dispatcher.Invoke(async () =>
            {
                var lobbyModified = Lobbies.Single(lobby => lobby.ID == userJoinedLobbyArgs.GroupID);


                if (!LobbyContainsPlayer(lobbyModified, userJoinedLobbyArgs))
                {
                    lobbyModified.Players.Add(new Player(userJoinedLobbyArgs.IsCPU, userJoinedLobbyArgs.UserID,
                        userJoinedLobbyArgs.Username));
                    lobbyModified.PlayersCount = lobbyModified.Players.Count;
                }

                SetUsersInfo(lobbyModified);
            });
        }

        private async void SetUsersInfo(Lobby lobby)
        {
            try
            {
                foreach (Player player in lobby.Players)
                {
                    if (player.User.Avatar == null)
                    {
                        player.User = await RestClient.GetUserInfo(player.User.ID);
                    }
                }
            }
            catch
            {
                //backlog #43
                SetUsersInfo(lobby);
            }
        }

        private bool LobbyContainsPlayer(Lobby lobby, LobbyEventArgs userJoinedLobbyArgs)
        {
            foreach (Player player in lobby.Players)
            {
                if (player.User.ID == userJoinedLobbyArgs.UserID)
                {
                    return true;
                }
            }

            return false;
        }

        private bool LobbyExists(string lobbyID)
        {
            foreach (Lobby lobby in Lobbies)
            {
                if (lobby.ID == lobbyID)
                {
                    return true;
                }
            }

            return false;
        }

        private void OnUserLeftLobby(object sender, EventArgs e)
        {
            var userLeftLobbyArgs = (LobbyEventArgs) e;

            Application.Current.Dispatcher.Invoke(() =>
            {
                if (LobbyExists(userLeftLobbyArgs.GroupID))
                {
                    var lobbyModif = Lobbies.Single(lobby => lobby.ID == userLeftLobbyArgs.GroupID);
                    if (LobbyContainsPlayer(lobbyModif, userLeftLobbyArgs))
                    {
                        var userToRemove =
                            lobbyModif.Players.Single(player => player.User.ID == userLeftLobbyArgs.UserID);
                        lobbyModif.Players.Remove(userToRemove);
                        lobbyModif.PlayersCount = lobbyModif.Players.Count;
                    }
                }
            });
        }
    }
}