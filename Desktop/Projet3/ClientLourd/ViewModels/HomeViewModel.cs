using ClientLourd.Models.Bindable;
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

namespace ClientLourd.ViewModels
{
    class HomeViewModel : ViewModelBase
    {
        private ObservableCollection<Lobby> _lobbies;

        public HomeViewModel()
        {
            SocketClient.LobbyCreated += OnLobbyCreated;
            Lobbies = new ObservableCollection<Lobby>();
            Lobbies.Add(new Lobby("My nice lobby come join COOP", "TamereShortz", GameModes.Coop,0, 8));
            Lobbies.Add(new Lobby("My nice lobby come join SOLO", "Tame2", GameModes.Solo,1, 1));
            Lobbies.Add(new Lobby("FFA", "FFALover", GameModes.FFA,2, 8));
            Lobbies.Add(new Lobby("My nice lobby come join COOP", "TamereShortz", GameModes.Coop, 3, 8));
            Lobbies.Add(new Lobby("My nice lobby come join SOLO", "Tame2", GameModes.Solo, 0, 1));
            Lobbies.Add(new Lobby("FFA", "FFALover", GameModes.FFA, 8, 8));
            Lobbies.Add(new Lobby("My nice lobby come join COOP", "TamereShortz", GameModes.Coop, 1, 8));
            Lobbies.Add(new Lobby("My nice lobby come join SOLO", "Tame2", GameModes.Solo, 1, 1));
            Lobbies.Add(new Lobby("FFA", "FFALover", GameModes.FFA, 1, 8));
            _modeFilteredAscending = false;
            _lobbyFilteredAscending = false;
            _hostFilteredAscending = false;
            _playerCountFilteredAscending = false;
            
        }

        public SocketClient SocketClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
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
            var lobbyCreated = (LobbyCreatedArgs)e;
            Application.Current.Dispatcher.Invoke(() =>
            {

                Lobbies.Insert(0, new Lobby(lobbyCreated.Name, lobbyCreated.OwnerName, (GameModes)lobbyCreated.Mode, 1, lobbyCreated.PlayersMax));
            });
        }

        private RelayCommand<object> _filterMode;

        public ICommand FilterModeCommand
        {
            get
            {
                return _filterMode ?? (_filterMode = new RelayCommand<object>(param => FilterMode()));
            }
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


        private RelayCommand<object> _filterLobbyName;

        public ICommand FilterLobbyNameCommand
        {
            get
            {
                return _filterLobbyName ?? (_filterLobbyName = new RelayCommand<object>(param => FilterLobby()));
            }
        }

        private bool _lobbyFilteredAscending;

        private void FilterLobby()
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

        private RelayCommand<object> _filterHostName;

        public ICommand FilterHostNameCommand
        {
            get
            {
                return _filterHostName ?? (_filterHostName = new RelayCommand<object>(param => FilterHost()));
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

        private RelayCommand<object> _filterPlayerCountName;

        public ICommand FilterPlayerCountCommand
        {
            get
            {
                return _filterPlayerCountName ?? (_filterPlayerCountName = new RelayCommand<object>(param => FilterPlayerCount()));
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

    }
}


   

