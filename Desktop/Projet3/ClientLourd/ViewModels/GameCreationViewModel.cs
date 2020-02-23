using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Documents;
using System.Windows.Input;
using ClientLourd.Services.EnumService;
using ClientLourd.Services.RestService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using MaterialDesignThemes.Wpf.Transitions;

namespace ClientLourd.ViewModels
{
    public class GameCreationViewModel : ViewModelBase
    {
        public GameCreationViewModel()
        {
            Hints = new ObservableCollection<string>(new string[3]);
            Hints.CollectionChanged += (sender, args) => { NotifyPropertyChanged(nameof(AreFieldsEmpty)); };
            BlackLevelThreshold = 50;
            BrushSize = 12;
        }
        
        public override void AfterLogOut() { throw new System.NotImplementedException(); }
        public override void AfterLogin() { throw new System.NotImplementedException(); }

        private ObservableCollection<string> _hints;
        public ObservableCollection<string> Hints 
        {
            get { return _hints; }
            set
            {
                if (_hints != value)
                {
                    _hints = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(AreFieldsEmpty));
                }
            }
        }
        
        public RestClient RestClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }
        
        public bool AreFieldsEmpty
        {
            get { return string.IsNullOrEmpty(_word) || _hints.Any(string.IsNullOrEmpty); }
        }
        public string Word 
        {
            get { return _word; }
            set
            {
                if (_word != value)
                {
                    _word = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(AreFieldsEmpty));
                }
            }
        }
        private string _word;

        public string SelectedMode 
        {
            get { return _selectedMode.GetDescription(); }
            set { _selectedMode = value.GetEnumFromDescription<PotraceMode>(); } 
        }
        private PotraceMode _selectedMode;

        public List<string> PotraceModes
        {
            get
            {
                
                var list = EnumManager.GetAllDescriptions<PotraceMode>();
                if (IsImageUpload)
                {
                    list.Remove(PotraceMode.Classic.GetDescription());
                }
                return list;
            }
        }

        private DifficultyLevel _selectedDifficulty;

        public string SelectedDifficulty
        {
            get { return _selectedDifficulty.GetDescription(); }
            set { _selectedDifficulty = value.GetEnumFromDescription<DifficultyLevel>(); } 
        }
        
        public List<string> DifficultyLevels
        {
            get { return EnumManager.GetAllDescriptions<DifficultyLevel>(); }
        }

        public string Image
        {
            get { return _image; }
            set
            {
                if (_image != value)
                {
                    _image = value;
                    NotifyPropertyChanged();
                    NotifyPropertyChanged(nameof(IsImageUpload));
                }
            }
        }

        private string _image;
        public bool IsImageUpload
        {
            get { return !string.IsNullOrWhiteSpace(_image); }
        }

        private string _gameID;
        
        public int BlackLevelThreshold { get; set; }
        public int BrushSize { get; set; }
        
        RelayCommand<object> _validateGameCommand;

        public ICommand ValidateGameCommand
        {
            get
            {
                return _validateGameCommand??
                       (_validateGameCommand = new RelayCommand<object>(param => ValidateGame()));
            }
        }
        private async Task ValidateGame()
        {
            try
            {
                _gameID = await RestClient.PostGameInformations(Word, Hints.ToArray(), _selectedDifficulty);
                //If the game is valid move to the next slide
                Transitioner.MoveNextCommand.Execute(null,null);
            }
            catch(Exception e)
            {
                await DialogHost.Show(new ClosableErrorDialog(e), "Dialog");
            }
        }
        
        
        RelayCommand<object> _uploadImageCommand;

        public ICommand UploadImageCommand
        {
            get
            {
                return _uploadImageCommand??
                       (_uploadImageCommand = new RelayCommand<object>(param => UploadImage(), o => !string.IsNullOrWhiteSpace(_image)));
            }
        }

        private async  Task UploadImage()
        {
            try
            {
                await RestClient.PostGameImage(_gameID, _image, PotraceMode.LeftToRight, BlackLevelThreshold/100.0, BrushSize);
                NotifyPropertyChanged(nameof(PotraceModes));
                NotifyPropertyChanged(nameof(BlackLevelThreshold));
                NotifyPropertyChanged(nameof(BrushSize));
                //If the game is valid move to the next slide
                Transitioner.MoveNextCommand.Execute(null,null);
            }
            catch(Exception e)
            {
                await DialogHost.Show(new ClosableErrorDialog(e), "Dialog");
            }
        }
        
        RelayCommand<string> _addImageCommand;

        public ICommand AddImageCommand
        {
            get
            {
                return _addImageCommand ??
                       (_addImageCommand = new RelayCommand<string>(image => AddImage(image)));
            }
        }
        
        RelayCommand<object> _removeImageCommand;
        public ICommand RemoveImageCommand
        {
            get
            {
                return _removeImageCommand ??
                       (_removeImageCommand = new RelayCommand<object>(image => Image = ""));
            }
        }

        private void AddImage(string image)
        {
            //TODO validate the file
            Image = image;
        }
        
        
    }
}