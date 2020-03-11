using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.InteropServices;
using System.Timers;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.InkCanvas;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Views.Controls;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using Lobby = ClientLourd.Models.Bindable.Lobby;

namespace ClientLourd.ViewModels
{
    public class GameViewModel : ModelBase
    {
        private string _drawingID;
        private string _word;
        private char[] _guess;
        private DateTime _time;
        private Timer _timer;
        private int _healthPoint;
        private ObservableCollection<Player> _players;
        private int _round;
        private StrokesReader _stokesReader;
        public GameViewModel()
        {
            InitEventHandler();
            HealthPoint = 3;
            Guess = new char[20];
            Word = "test";
            Players = new ObservableCollection<Player>()
            {
                new Player()
                {
                    User = new User("test1", "1"),
                    GuessedTheWord = true,
                },
                new Player()
                {
                    User = new User("test2", "2"),
                    IsDrawing = true,
                },
                new Player()
                {
                    User = new User("test3", "3"),
                },
                new Player()
                {
                    User = new User("test4", "4"),
                },
            };
        }

        private void InitEventHandler()
        {
            SocketClient.MatchStarted += SocketClientOnMatchStarted;
            SocketClient.MatchEnded += SocketClientOnMatchEnded;
            SocketClient.MatchTimesUp += SocketClientOnMatchTimesUp;
            SocketClient.MatchCheckPoint += SocketClientOnMatchCheckPoint;
            SocketClient.AreYouReady += SocketClientOnAreYouReady;
            SocketClient.GuessResponse += SocketClientOnGuessResponse;
            SocketClient.PlayerGuessedTheWord += SocketClientOnPlayerGuessedTheWord;
            SocketClient.MatchSync += SocketClientOnMatchSync;
            SocketClient.YourTurnToDraw += SocketClientOnYourTurnToDraw;
            SocketClient.NewPlayerIsDrawing += SocketClientOnNewPlayerIsDrawing;
            SocketClient.PlayerLeftMatch += SocketClientOnPlayerLeftMatch;
        }

        private void SocketClientOnPlayerLeftMatch(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Players.Remove(Players.FirstOrDefault(p => p.User.ID == e.UserID));
        }

        private void SocketClientOnNewPlayerIsDrawing(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Players.ToList().ForEach(p => p.IsDrawing = false);
            Players.FirstOrDefault(p => p.User.ID == e.UserID).IsDrawing = true;
            //Disable the canvas
        }

        private void SocketClientOnYourTurnToDraw(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            //Enable the canvas
            Word = e.Word;
            Time = e.Time;
            _drawingID = e.DrawingID;
            _stokesReader.Start(_drawingID);
        }

        private void SocketClientOnMatchSync(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            _round = e.Laps;
            Time = e.Time;
        }

        private void SocketClientOnPlayerGuessedTheWord(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Players.FirstOrDefault(p => p.User.ID == e.UserID).GuessedTheWord = true;
        }

        private void SocketClientOnGuessResponse(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            if (e.Valid)
            {
                Players.FirstOrDefault(p => p.User.ID == SessionInformations.User.ID).Score = e.PointsTotal;
                //disable canvas
            }
        }

        private void SocketClientOnAreYouReady(object source, EventArgs args)
        {
            PrepareMatch();
        }

        private void SocketClientOnMatchCheckPoint(object source, EventArgs args)
        {
            //TODO
        }

        private void SocketClientOnMatchTimesUp(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Editor.Canvas.Strokes.Clear();
            //Round end
            if (e.Type == 1)
            {
                _stokesReader.Stop();    
            }
            //Game end
            else if (e.Type == 2)
            {
            }
        }

        private void SocketClientOnMatchEnded(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Player Winner = Players.FirstOrDefault(p => p.User.ID == e.WinnerID);
            MessageBox.Show("game end");
            DialogHost.Show(new MessageDialog("Game ended", $"The winner is {Winner.User.Username}"));
        }

        private void SocketClientOnMatchStarted(object source, EventArgs args)
        {
        }
        
        public Lobby Lobby
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                {
                    return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                        ?.CurrentLobby;
                });
            }
        }
        public SessionInformations SessionInformations
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                {
                    return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                        ?.SessionInformations;
                });
            }
        }

        public SocketClient SocketClient
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                    {
                        return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                            ?.SocketClient;
                    });
            }
        }

        public RestClient RestClient
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                    {
                        return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                            ?.RestClient;
                    });
            }
        }

        public Player Artist
        {
            get => Players.FirstOrDefault(p => p.IsDrawing);
        }

        public ObservableCollection<Player> Players
        {
            get => new ObservableCollection<Player>(_players.OrderByDescending(p => p.Score)); 
            set
            {
                _players = value;
                NotifyPropertyChanged();
            }
        }
        
        public char[] Guess
        {
            get => _guess;
            set
            {
                _guess = value;
                NotifyPropertyChanged();
            }
        }

        public bool IsYourTurn
        {
            get => !string.IsNullOrEmpty(Word);
        }

        public string Word
        {
            get => _word;
            set
            {
                _word = value;
                NotifyPropertyChanged();
                NotifyPropertyChanged(nameof(IsYourTurn));
            }
        }
        public int Round
        {
            get => _round;
            set
            {
                _round = value;
                NotifyPropertyChanged();
            }
        }
        public DateTime Time
        {
            get => _time;
            set
            {
                _time = value;
                NotifyPropertyChanged();
            }
        }

        public int HealthPoint
        {
            get => _healthPoint;
            set
            {
                _healthPoint = value;
                NotifyPropertyChanged();
            }
        }
        public Editor Editor { get; set; }
        
        RelayCommand<object> _sendGuessCommand;

        public ICommand SendGuessCommand
        {
            get
            {
                return _sendGuessCommand ??
                       (_sendGuessCommand = new RelayCommand<object>(channel => SendGuess()));
            }
        }

        private void SendGuess()
        {
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.GuessTheWord, new string(Guess)));
        }

        private void ClearCanvas()
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                Editor.Canvas.Strokes.Clear();
            });
        }

        private void PrepareMatch()
        {
            ClearCanvas();
            Application.Current.Dispatcher.Invoke(() =>
            {
                HealthPoint = 3;
                Players = Lobby.Players;
                _stokesReader = new StrokesReader(Editor, SocketClient, ((EditorViewModel)Editor.DataContext).EditorInformation);
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.ReadyToStart));
            });
        }


    }
}