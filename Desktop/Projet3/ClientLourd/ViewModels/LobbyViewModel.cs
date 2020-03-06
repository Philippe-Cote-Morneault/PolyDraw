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

namespace ClientLourd.ViewModels
{
    class LobbyViewModel : ViewModelBase
    {
        public LobbyViewModel()
        {
            SocketClient.JoinLobbyResponse += OnJoinLobbyResponse;
            SocketClient.LobbyDeleted += OnLobbyDeleted;
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
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.QuitLobbyRequest));
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


                
                if (IsInALobby() && lobbyDeletedID == CurrentLobby.ID && CurrentLobby.HostID != SessionInformations.User.ID)
                {
                    ContainedView = Utilities.Enums.Views.Home.ToString();
                    DialogHost.Show(new ClosableErrorDialog($"The host has left the lobby!"), "Default");
                }
            });
        }

        private bool IsInALobby()
        {
            return CurrentLobby != null;
        }

    }
}