using System;
using System.Net.Sockets;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using ClientLourd.Utilities.Enums;
using System.Media;
using ClientLourd.Services.SoundService;
using ClientLourd.Services.EnumService;
using System.Collections.Generic;
using ClientLourd.Services.UserManagerService;

namespace ClientLourd.ViewModels
{
    public class MainViewModel : ViewModelBase
    {
        string _containedView;

        public RestClient RestClient { get; set; }
        public SocketClient SocketClient { get; set; }

        public Lobby CurrentLobby { get; set; }

        public NetworkInformations NetworkInformations { get; set; }
        private SessionInformations _sessionInformations;

        public bool IsSystemLanguage { get; set; }

        public SessionInformations SessionInformations
        {
            get { return _sessionInformations; }
            set
            {
                if (value != _sessionInformations)
                {
                    _sessionInformations = value;
                    NotifyPropertyChanged();
                }
            }
        }


        public MainViewModel()
        {
            AfterLogOut();
            SoundService = new SoundService();
        }

        public SoundService SoundService { get; set; }

        private UserSettingsManagerService _userManager;

        public override void AfterLogin()
        {
            //TODO

            _userManager = new UserSettingsManagerService(SessionInformations.User.ID);

            if (!IsSystemLanguage)
            {
                _userManager.SetLanguage(SelectedLanguage);
            }

            else if(IsSystemLanguage && _userManager.GetLanguage() != "System")
            {
                SelectedLanguage = _userManager.GetLanguage();
            }

            SocketClient?.SendMessage((_selectedLanguage == Utilities.Enums.Languages.EN) ? new Tlv(SocketMessageTypes.ChangeLanguage, new { Language = 0 }) : new Tlv(SocketMessageTypes.ChangeLanguage, new { Language = 1 }));
        }

        public override void AfterLogOut()
        {
            NetworkInformations = new NetworkInformations();
            SessionInformations = new SessionInformations();
            ContainedView = Utilities.Enums.Views.Home.ToString();
            RestClient = new RestClient(NetworkInformations);
            RestClient.StartWaiting += (source, args) => { IsWaiting = true; };
            RestClient.StopWaiting += (source, args) => { IsWaiting = false; };
            SocketClient = new SocketClient(NetworkInformations);
            SocketClient.StartWaiting += (source, args) => { IsWaiting = true; };
            SocketClient.StopWaiting += (source, args) => { IsWaiting = false; };
            SocketClient.ConnectionLost += SocketClientOnConnectionLost;
            SocketClient.ServerMessage += SocketClientOnServerMessage;


        }

        private void SocketClientOnServerMessage(object source, EventArgs args)
        {
            Application.Current.Dispatcher.Invoke(delegate
            {
                try
                {
                    DialogHost.Show(new ClosableErrorDialog(((SocketErrorEventArgs) args).Message));
                }
                catch
                {
                    //The message will be ignore
                }
            });
        }

        private bool _isWaiting;

        /// <summary>
        /// Indicate if the progress bar must be visible
        /// </summary>
        public bool IsWaiting
        {
            get { return _isWaiting; }
            set
            {
                _isWaiting = value;
                NotifyPropertyChanged();
            }
        }
        
        private RelayCommand<string> _changeNetworkCommand;
        public ICommand ChangeNetworkCommand
        {
            get
            {
                return _changeNetworkCommand ??
                       (_changeNetworkCommand = new RelayCommand<string>(config => NetworkInformations.Config = int.Parse(config)));
            }
        }
        
        

        private RelayCommand<LoginViewModel> _logoutCommand;

        public ICommand LogoutCommand
        {
            get
            {
                return _logoutCommand ??
                       (_logoutCommand = new RelayCommand<LoginViewModel>(lvm => Logout(), lvm => !IsWaiting));
            }
        }

        private void Logout()
        {
            SocketClient.Close();
            OnUserLogout(this);
        }

        public delegate void LogOutHandler(object source, EventArgs args);

        public event LogOutHandler UserLogout;

        public string ContainedView
        {
            get { return _containedView; }

            set
            {
                if (value != _containedView)
                {
                    _containedView = value;
                    NotifyPropertyChanged();
                }
            }
        }


        protected virtual void OnUserLogout(object source)
        {
            UserLogout?.Invoke(source, EventArgs.Empty);
        }

        private void SocketClientOnConnectionLost(object source, EventArgs e)
        {
            Logout();
        }

        private RelayCommand<object> _myProfileCommand;

        public ICommand MyProfileCommand
        {
            get
            {
                return _myProfileCommand ?? (_myProfileCommand = new RelayCommand<object>(obj => MyProfile()));
            }
        }

        private void MyProfile()
        {
            ContainedView = Utilities.Enums.Views.Profile.ToString();
        }

        private RelayCommand<object> _homeCommand;

        public ICommand HomeCommand
        {
            get
            {
                return _homeCommand ?? (_homeCommand = new RelayCommand<object>(obj => Home()));
            }
        }

        private void Home()
        {
            ContainedView = Utilities.Enums.Views.Home.ToString();
        }

        // TODO: Delete this
        private RelayCommand<object> _editorCommand;

        public ICommand EditorCommand
        {
            get
            {
                return _editorCommand ?? (_editorCommand = new RelayCommand<object>(obj => Editor()));
            }
        }

        private void Editor()
        {
            ContainedView = Utilities.Enums.Views.Editor.ToString();
        }


        private RelayCommand<object> _toggleSoundCommand;

        public ICommand ToggleSoundCommand
        {
            get
            {
                return _toggleSoundCommand ?? (_toggleSoundCommand = new RelayCommand<object>(obj => ToggleSound()));
            }
        }

        private void ToggleSound()
        {
            SoundService.ToggleSound();
        }

        private Languages _selectedLanguage;

        public Languages CurrentLanguage
        {
            get => _selectedLanguage;
        }

        public string SelectedLanguage
        {
            get
            {
                return _selectedLanguage.GetDescription();
            }
            set
            {
                if (!string.IsNullOrWhiteSpace(value) && _selectedLanguage.GetDescription() != value)
                {
                    _selectedLanguage = value.GetEnumFromDescription<Languages>();
                    SocketClient?.SendMessage((_selectedLanguage == Utilities.Enums.Languages.EN) ? new Tlv(SocketMessageTypes.ChangeLanguage, new { Language = 0 }): new Tlv(SocketMessageTypes.ChangeLanguage, new { Language = 1}));
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(Languages));
                    NotifyPropertyChanged(nameof(CurrentLanguage));
                    _userManager?.SetLanguage(_selectedLanguage.GetDescription());
                    LanguageChangedEvent?.Invoke(this, EventArgs.Empty);
                }
            }
        }

        public void TriggerLangChangedEvent()
        {
           LanguageChangedEvent?.Invoke(this, EventArgs.Empty);
        }


        public List<string> Languages
        {
            get { return EnumManager.GetAllDescriptions<Languages>(); }
        }
        public ResourceDictionary CurrentDictionary { get; set; }

        public delegate void LanguageChangedHandler(object source, EventArgs args);
        public event LanguageChangedHandler LanguageChangedEvent;

    }
}