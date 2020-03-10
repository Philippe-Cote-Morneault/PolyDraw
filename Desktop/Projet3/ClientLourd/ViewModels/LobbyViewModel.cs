﻿using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Utilities.Enums;
using MaterialDesignThemes.Wpf;
using ClientLourd.Views.Dialogs;
using ClientLourd.Services.SoundService;

namespace ClientLourd.ViewModels
{
    class LobbyViewModel : ViewModelBase
    {
        public LobbyViewModel()
        {
            SocketClient.JoinLobbyResponse += OnJoinLobbyResponse;
            SocketClient.LobbyDeleted += OnLobbyDeleted;
            SocketClient.UserJoinedLobby += OnUserJoinedLobby;
            SocketClient.UserLeftLobby += OnUserLeftLobby;
            SocketClient.StartGameResponse += OnStartGameResponse;
            IsGameStarted = false;
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
            throw new NotImplementedException();
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
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.QuitLobbyRequest));
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
                    CurrentLobby = CurrentLobby;
                });
            }
        }

        private void OnLobbyDeleted(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                var lobbyDeletedArgs = (LobbyEventArgs)e;

                string lobbyDeletedID = new Guid(lobbyDeletedArgs.Bytes).ToString();

                if (UserIsInLobby() && !UserIsHost())
                {
                    CurrentLobby = null;
                    ContainedView = Utilities.Enums.Views.Home.ToString();
                    DialogHost.Show(new ClosableErrorDialog($"The host has left the lobby!"), "Default");
                }
            });
        }

        private bool UserIsInLobby()
        {
            if (CurrentLobby == null)
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
            if (SessionInformations != null && SessionInformations.User !=  null)
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
                return _startGameCommand ?? (_startGameCommand = new RelayCommand<object>(obj => StartGame(), obj => CanStartGame()));
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

            int nHumanPlayers = 0;

            if (CurrentLobby != null)
            {
                
                foreach (Player player in CurrentLobby.Players)
                {
                    if (!player.IsCPU)
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
                // This event is only sent to host... To talk with server

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
                CurrentLobby = CurrentLobby;
            });
        }

        private void OnUserLeftLobby(object sender, EventArgs e)
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                CurrentLobby = CurrentLobby;
            });
        }

    }
}