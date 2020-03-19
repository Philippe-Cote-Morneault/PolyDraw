using ClientLourd.Models.Bindable;
using ClientLourd.Services.EnumService;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Utilities.ValidationRules;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;


namespace ClientLourd.ViewModels
{
    public class LobbyCreationViewModel: ViewModelBase
    {
        public LobbyCreationViewModel()
        {
            GameName = "";
            SelectedMode = GameModes.FFA.GetDescription();
            SelectedNumberOfPlayers = 8;
            SelectedDifficulty = DifficultyLevel.Easy.GetDescription();
        }

        public SessionInformations SessionInformations
        {
            get
            {
                return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations;
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

        

        private DifficultyLevel _selectedDifficulty;

        public string SelectedDifficulty
        {
            get
            {
                return _selectedDifficulty.GetDescription();
            }
            set
            {
                if (!string.IsNullOrWhiteSpace(value))
                {
                    _selectedDifficulty = value.GetEnumFromDescription<DifficultyLevel>();
                }
            }
        }

        public List<string> Difficulties
        {
            get { return EnumManager.GetAllDescriptions<DifficultyLevel>(); }
        }


        private GameModes _selectedMode;

        public string SelectedMode
        {
            get { return _selectedMode.GetDescription(); }
            set
            {
                if (!string.IsNullOrWhiteSpace(value))
                {
                    _selectedMode = value.GetEnumFromDescription<GameModes>();
                    
                    if (_selectedMode == GameModes.Coop)
                    {
                        SelectedNumberOfPlayers = 4;
                    }
                    
                    if (_selectedMode == GameModes.Solo)
                    {
                        SelectedNumberOfPlayers = 1;
                    }
                    
                    if (_selectedMode == GameModes.FFA)
                    {
                        SelectedNumberOfPlayers = 8;
                    }

                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(NumberOfPlayersList));
                }
            }
        }

        public List<string> Modes
        {
            get { return EnumManager.GetAllDescriptions<GameModes>(); }
        }


        private int _selectedNumberOfPlayers;
        public int SelectedNumberOfPlayers
        {
            get => _selectedNumberOfPlayers;
            set
            {
                if (value != _selectedNumberOfPlayers)
                {
                    _selectedNumberOfPlayers = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public List<int> NumberOfPlayersList
        {
            get
            {
                if (SelectedMode == GameModes.Solo.GetDescription())
                    return new List<int>() { 1 };

                if (SelectedMode == GameModes.FFA.GetDescription())
                    return new List<int>() { 8, 7, 6, 5, 4, 3, 2 };

                if (SelectedMode == GameModes.Coop.GetDescription())
                    return new List<int>() { 4, 3, 2 };

                NotifyPropertyChanged();
                return new List<int>();
            }
        }




        private string _gameName;

        public string GameName
        {
            get => _gameName;
            set
            {
                if (value != _gameName)
                {
                    _gameName = value;
                }
            }
        }

        public override void AfterLogOut()
        {
            throw new NotImplementedException();
        }

        private RelayCommand<string> _createLobbyCommand;


        public ICommand CreateLobbyCommand
        {
            get
            {
                return _createLobbyCommand ?? (_createLobbyCommand = new RelayCommand<string>(lobbyName => CreateLobby(), lobbyName => LobbyNameValid(lobbyName)));
            }
        }

        private async void CreateLobby()
        {
            try
            {
                await RestClient.PostGroup(GameName, SelectedNumberOfPlayers, SelectedMode.GetEnumFromDescription<GameModes>(), SelectedDifficulty.GetEnumFromDescription<DifficultyLevel>());
                DialogHost.CloseDialogCommand.Execute(null, null);
            }
            catch (Exception e)
            {
                await DialogHost.Show(new ClosableErrorDialog(e), "Dialog");
            }
        }
        
        private bool LobbyNameValid(string lobbyName)
        {
            return LobbyNameRule.IsAlphaNumerical(lobbyName);
        }

        public override void AfterLogin()
        {
            throw new NotImplementedException();
        }
    }
}
