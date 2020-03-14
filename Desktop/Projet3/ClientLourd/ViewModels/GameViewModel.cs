using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.InkCanvas;
using ClientLourd.Services.RestService;
using ClientLourd.Services.ServerStrokeDrawerService;
using ClientLourd.Services.SocketService;
using ClientLourd.Services.SoundService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Utilities.Extensions;
using ClientLourd.Views.Controls;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using Lobby = ClientLourd.Models.Bindable.Lobby;
using Timer = System.Timers.Timer;

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
        private long _round;
        private StrokesReader _stokesReader;
        private string _canvasMessage;
        public ServerStrokeDrawerService StrokeDrawerService { get; set; }

        public GameViewModel()
        {
            _players = new ObservableCollection<Player>();
            InitEventHandler();
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
            SocketClient.ServerStrokeSent += SocketClientOnServerStrokeSent;
            SocketClient.UserDeleteStroke += SocketClientOnUserDeleteStroke;
        }

        public SoundService SoundService
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SoundService; }
        }

        private void SocketClientOnUserDeleteStroke(object source, EventArgs args)
        {
            Application.Current.Dispatcher.Invoke(delegate 
            {
                var e = (DrawingEventArgs) args;
                

                byte[] idTodelete = e.Data.Clone();
                
                Array.Reverse(idTodelete, 0, 4);
                Array.Reverse(idTodelete, 4, 2);
                Array.Reverse(idTodelete, 6, 2);

                if(BitConverter.IsLittleEndian)
                    Array.Reverse(idTodelete);

                Editor.Canvas.RemoveStroke(new Guid(idTodelete));
            });
        }

        private void SocketClientOnServerStrokeSent(object source, EventArgs args)
        {

            StrokeDrawerService?.Enqueue((args as StrokeSentEventArgs).StrokeInfo);
        }

        private void SocketClientOnPlayerLeftMatch(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Players.Remove(Players.FirstOrDefault(p => p.User.ID == e.UserID));
            if (e.UserID == SessionInformations.User.ID)
            {
                StrokeDrawerService.Stop();
            }
        }

        private void SocketClientOnNewPlayerIsDrawing(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Time = e.Time;
            Players.ToList().ForEach(p => p.IsDrawing = false);
            Players.ToList().ForEach(p => p.GuessedTheWord = false);
            Players.FirstOrDefault(p => p.User.ID == e.UserID).IsDrawing = true;
            if (SessionInformations.User.ID != e.UserID)
            {
                ShowCanvasMessage($"{e.Username} is drawing the next word !");
            }
            Guess = new char[e.WordLength];
        }

        private void SocketClientOnYourTurnToDraw(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            //Enable the canvas
            Word = e.Word; 
            ShowCanvasMessage($"It is your turn to draw to word {e.Word}");
            ChangeCanvasStatus(true);
            _drawingID = e.DrawingID;
            _stokesReader.Start(_drawingID);
        }

        private void SocketClientOnMatchSync(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Round = e.Laps;
            Time = e.Time;
            var playersInfo = e.Players;
            foreach (dynamic info in playersInfo)
            {
                var tmpPlayer = Players.First(p => p.User.ID == info["UserID"]);
                tmpPlayer.Score = info["Points"];
            }
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
                ShowCanvasMessage($"+ {e.Points}");
                Players.First(p => p.User.ID == SessionInformations.User.ID).Score = e.PointsTotal;
                Application.Current.Dispatcher.Invoke(() => 
                {
                    SoundService.PlayWordGuessedRight();
                });
            }
            else
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    SoundService.PlayWordGuessedWrong();
                });
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
            PrepareNextRound();
            var e = (MatchEventArgs) args;
            //Round end
            if (e.Type == 1)
            {
                ShowCanvasMessage($"The word was {e.Word}");
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
            MessageBox.Show($"game end, {Winner.User.Username}");
            StrokeDrawerService.Stop();
            //DialogHost.Show(new MessageDialog("Game ended", $"The winner is {Winner.User.Username}"));
        }

        private void SocketClientOnMatchStarted(object source, EventArgs args)
        {
            StrokeDrawerService.Start();
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
        public long Round
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

        public string CanvasMessage
        {
            get => _canvasMessage;
            set
            {
               _canvasMessage = value;  
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
        
        private void PrepareNextRound()
        {
            ChangeCanvasStatus(false);
            _stokesReader.Stop();
            Word = "";
            Task.Run(() =>
            {
                Thread.Sleep(100);
                ClearCanvas();
            });
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

        private void ChangeCanvasStatus(bool isEnable)
        {
            Application.Current.Dispatcher.Invoke(() => { Editor.IsEnabled = isEnable; });
        }

        private void PrepareMatch()
        {
            ChangeCanvasStatus(false);
            ClearCanvas();
            Application.Current.Dispatcher.Invoke(() =>
            {
                HealthPoint = 3;
                Players = Lobby.Players;
                _stokesReader = new StrokesReader(Editor, SocketClient, ((EditorViewModel)Editor.DataContext).EditorInformation);
                SocketClient.SendMessage(new Tlv(SocketMessageTypes.ReadyToStart));
            });
        }

        private void ShowCanvasMessage(string message)
        {
            Task.Run(() =>
            {
                CanvasMessage = message;
                Thread.Sleep(2000);
                CanvasMessage = "";
            });
        }


    }
}