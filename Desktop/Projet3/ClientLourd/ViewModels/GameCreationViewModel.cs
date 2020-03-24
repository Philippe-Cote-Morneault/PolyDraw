using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.EnumService;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Utilities.Extensions;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using MaterialDesignThemes.Wpf.Transitions;
using ClientLourd.Services.ServerStrokeDrawerService;

namespace ClientLourd.ViewModels
{
    public class GameCreationViewModel : ViewModelBase
    {        
        public ServerStrokeDrawerService StrokeDrawerService { get; set; }
        private int _numberStrokesReceived;
        private Game _game;

        public GameCreationViewModel()
        {
            Game = new Game();
            _numberStrokesReceived = 0;
            PreviewGUIEnabled = true;
            BlackLevelThreshold = 50;
            BrushSize = 12;
            NumberOfHints = new List<int>(){1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            SelectedNumberOfHints = 1;
        }


        public void OnPreviewDrawingDone(object source, EventArgs e)
        {
            StrokeDrawerService.Stop();
            PreviewGUIEnabled = true;
            _numberStrokesReceived = 0;
        }


        public void AddSocketListeners()
        {
            SocketClient.ServerStartsDrawing += SocketClientOnServerStartsDrawing;
            SocketClient.ServerEndsDrawing += SocketClientOnServerEndsDrawing;
            SocketClient.DrawingPreviewResponse += SocketClientOnDrawingPreviewResponse;
            SocketClient.ServerStrokeSent += SocketClientOnServerStrokeSent;
        }

        public void RemoveSocketListeners()
        {
            SocketClient.ServerStartsDrawing -= SocketClientOnServerStartsDrawing;
            SocketClient.ServerEndsDrawing -= SocketClientOnServerEndsDrawing;
            SocketClient.DrawingPreviewResponse -= SocketClientOnDrawingPreviewResponse;
            SocketClient.ServerStrokeSent -= SocketClientOnServerStrokeSent;
        }

        private void SocketClientOnServerStartsDrawing(object source, EventArgs args)
        {
            StrokeDrawerService.Start();
            PreviewGUIEnabled = false;
        }

        private void SocketClientOnServerEndsDrawing(object source, EventArgs args)
        { 
            StrokeDrawerService.TotalMessagesSent = _numberStrokesReceived;
        }
        
        private void SocketClientOnDrawingPreviewResponse(object source, EventArgs args)
        {
            Application.Current.Dispatcher.Invoke(delegate
            {
                if ((args as DrawingEventArgs).Data == 0)
                {
                    DialogHost.Show(new ClosableErrorDialog("The preview request was refused."), "Dialog");

                }
            });

        }

        private void SocketClientOnServerStrokeSent(object source, EventArgs args)
        {
            if (StrokeDrawerService != null)
            {
                _numberStrokesReceived++;
                StrokeDrawerService?.Enqueue((args as StrokeSentEventArgs).StrokeInfo);
            }
            
        }
        
        public override void AfterLogOut() { throw new System.NotImplementedException(); }
        public override void AfterLogin() { throw new System.NotImplementedException(); }
        
        
        public RestClient RestClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }
        
        public SocketClient SocketClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }



        public string SelectedMode 
        {
            get { return _selectedMode.GetDescription(); }
            set
            {
                if (!string.IsNullOrWhiteSpace(value))
                {
                    _selectedMode = value.GetEnumFromDescription<PotraceMode>();
                    NotifyPropertyChanged();
                }
            } 
        }

        public List<int> NumberOfHints { get; set; }
        private int _selectedNumberOfHints;
        public int SelectedNumberOfHints
        {
            get => _selectedNumberOfHints;
            set
            {
                _selectedNumberOfHints = value;
                Game.Hints.ToList().ForEach(h => h.IsSelected = false);
                Game.Hints.Take(value).ToList().ForEach(h => h.IsSelected = true);
                NotifyPropertyChanged(nameof(Hints));
            }
        }

        public ObservableCollection<Hint> Hints
        {
            get => new ObservableCollection<Hint>(Game.Hints.Take(SelectedNumberOfHints));
        }
        
        private PotraceMode _selectedMode;


        public List<string> PotraceModes
        {
            get
            {
                
                var list = EnumManager.GetAllDescriptions<PotraceMode>();
                if (IsUploadModeSelected)
                {
                    list.Remove(PotraceMode.Classic.GetDescription());
                }
                return list;
            }
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
                    NotifyPropertyChanged(nameof(IsUploadModeSelected));
                }
            }
        }

        private string _image;

        public bool IsUploadModeSelected { get; set; }
        public bool IsImageUpload
        {
            get { return !string.IsNullOrWhiteSpace(_image); }
        }

        private string _gameID;

        public string GameID
        {
            get => _gameID;
        }

        private InkCanvas _currentCanvas;

        public InkCanvas CurrentCanvas
        {
            get => _currentCanvas;
            set
            {
                _currentCanvas = value;
                NotifyPropertyChanged();
            }
        }

        public Game Game
        {
            get => _game;
            set
            {
                if (value != _game)
                {
                    _game = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public int BlackLevelThreshold { get;  set;}
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
                _gameID = await RestClient.PostGameInformations(Game.Word, Hints.Select(h => h.Text).ToArray(), _selectedDifficulty);
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
                       (_uploadImageCommand = new RelayCommand<object>(param => UploadImage(param)));
            }
        }


        private async  Task UploadImage(object param)
        {
            try
            {
                if (IsUploadModeSelected)
                {
                    SelectedMode = PotraceMode.LeftToRight.GetDescription();
                    await RestClient.PostGameImage(_gameID, _image, PotraceMode.LeftToRight, BlackLevelThreshold / 100.0, BrushSize);
                }

                else
                {
                    SelectedMode = PotraceMode.Classic.GetDescription();
                    await RestClient.PostGameImage(_gameID, DrawnImagePath, PotraceMode.Classic, BlackLevelThreshold / 100.0, BrushSize);
                }

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
        
        RelayCommand<object> _playPreviewCommand;
        public ICommand PlayPreviewCommand
        {
            get
            {
                return _playPreviewCommand ??
                       (_playPreviewCommand = new RelayCommand<object>(image => PlayPreview()));
            }
        }

        private async void PlayPreview()
        {
            try
            {
                CurrentCanvas.Strokes.Clear();
                await RestClient.PutGameInformations(GameID, _selectedMode,BlackLevelThreshold / 100.0, BrushSize);
                Console.WriteLine($"Played preview mode: {_selectedMode}");
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.DrawingPreviewRequest, new Guid(GameID)));
            }
            catch (Exception e)
            {
                await DialogHost.Show(new ClosableErrorDialog(e), "Dialog");
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
            FileInfo f = new FileInfo(image);
            if (f.Length >= 5000000)
            {
                DialogHost.Show(new ClosableErrorDialog("You cant upload a file larger than 5MB"), "Dialog");
            }
            else
            {
                Image = image;
            }
        }

        bool _previewGUIEnabled;

        public bool PreviewGUIEnabled
        {
            get { return _previewGUIEnabled; }
            set
            {
                if (value != _previewGUIEnabled)
                {
                    _previewGUIEnabled = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public string DrawnImagePath { get; set; }
    }
}