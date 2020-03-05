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

namespace ClientLourd.ViewModels
{
    public class HomeViewModel : ViewModelBase
    {
        private ObservableCollection<Lobby> _lobbies;

        public HomeViewModel()
        {
            SocketClient.LobbyCreated += OnLobbyCreated;
            SocketClient.JoinLobbyResponse += OnJoinLobbyResponse;
            Lobbies = new ObservableCollection<Lobby>();
            Lobbies.Add(new Lobby("My nice lobby come join COOP", "0",  "TamereShortz", "0", new ObservableCollection<Player>(), GameModes.Coop, DifficultyLevel.Easy,0, 8));
            Lobbies.Add(new Lobby("My nice lobby come join SOLO", "0", "Tame2", "0", new ObservableCollection<Player>(), GameModes.Solo, DifficultyLevel.Hard,1, 1));
            Lobbies.Add(new Lobby("FFA", "0", "FFALover", "0", new ObservableCollection<Player>(), GameModes.FFA, DifficultyLevel.Easy,2, 8));
            Lobbies.Add(new Lobby("My nice lobby come join COOP", "0", "TamereShortz", "0", new ObservableCollection<Player>(), GameModes.Coop, DifficultyLevel.Medium, 3, 8));
            Lobbies.Add(new Lobby("My nice lobby come join SOLO", "0", "Tame2", "0", new ObservableCollection<Player>(), GameModes.Solo, DifficultyLevel.Medium,0, 1));
            Lobbies.Add(new Lobby("FFA", "0", "FFALover", "0", new ObservableCollection<Player>(), GameModes.FFA, DifficultyLevel.Easy, 8, 8));
            Lobbies.Add(new Lobby("My nice lobby come join COOP", "0", "TamereShortz", "0", new ObservableCollection<Player>(), GameModes.Coop, DifficultyLevel.Hard, 1, 8));
            Lobbies.Add(new Lobby("My nice lobby come join SOLO", "0", "Tame2", "0", new ObservableCollection<Player>(), GameModes.Solo, DifficultyLevel.Medium, 1, 1));
            Lobbies.Add(new Lobby("FFA", "0", "FFALover", "0", new ObservableCollection<Player>(), GameModes.FFA, DifficultyLevel.Easy, 1, 8));
            _modeFilteredAscending = false;
            _lobbyFilteredAscending = false;
            _hostFilteredAscending = false;
            _playerCountFilteredAscending = false;
            
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


        public SocketClient SocketClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }

        public SessionInformations SessionInformations
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations;
            }
        }

        public Lobby CurrentLobby
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.CurrentLobby; }
            set { (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).CurrentLobby = value; }
        }

        public override void AfterLogin()
        {

            // GET
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


        private void OnLobbyCreated(object sender, EventArgs e)
        {
            var lobbyCreated = (LobbyEventArgs)e;
            Application.Current.Dispatcher.Invoke(() =>
            {
                Lobby lobby = new Lobby(
                    lobbyCreated.Name, 
                    lobbyCreated.ID,
                    lobbyCreated.OwnerName, 
                    lobbyCreated.OwnerID,
                    lobbyCreated.Players,
                    (GameModes)lobbyCreated.Mode, 
                    (DifficultyLevel)lobbyCreated.Difficulty, 
                    lobbyCreated.Players.Count, 
                    lobbyCreated.PlayersMax
                    );
                Lobbies.Insert(0, lobby);
                if (IsCreatedByUser(lobbyCreated.OwnerID))
                {
                    CurrentLobby = lobby;
                    CurrentLobby.Host = lobbyCreated.OwnerName;
                }
                    
            });
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
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => (int)lobby.Mode).ToList());
                _modeFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => (int)lobby.Mode).ToList());
                _modeFilteredAscending = false;
            }
        }



        private bool _lobbyFilteredAscending;

        private void FilterLobbyName()
        {

            if (!_lobbyFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => lobby.GameName).ToList());
                _lobbyFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => lobby.GameName).ToList());
                _lobbyFilteredAscending = false;
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
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => lobby.PlayersCount).ToList());
                _playerCountFilteredAscending = false;
            }
        }

        private bool _difficultyFilteredAscending;

        private void FilterDifficulty()
        {

            if (!_difficultyFilteredAscending)
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderBy((lobby) => (int)lobby.Difficulty).ToList());
                _difficultyFilteredAscending = true;
            }
            else
            {
                Lobbies = new ObservableCollection<Lobby>(Lobbies.OrderByDescending((lobby) => (int)lobby.Difficulty).ToList());
                _difficultyFilteredAscending = false;
            }
        }

        private RelayCommand<string> _filterLobbies;

        public ICommand FilterLobbiesCommand
        {
            get
            {
                return _filterLobbies ?? (_filterLobbies = new RelayCommand<string>(attribute => FilterLobbies(attribute)));
            }
        }


        private void FilterLobbies(string attribute)
        {
            if (attribute == "LobbyName")
                FilterLobbyName();
            if (attribute == "Mode")
                FilterMode();
            if (attribute == "Host")
                FilterHost();
            if (attribute == "Players")
                FilterPlayerCount();
            if (attribute == "Difficulty")
                FilterDifficulty();
        }

        public void JoinLobby(Lobby lobby) 
        {
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.JoinLobbyRequest, new Guid(lobby.ID)));
        }

        private void OnJoinLobbyResponse(object sender, EventArgs e)
        {
            var joinLobbyArgs = (LobbyEventArgs)e;

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
    }
}


   

