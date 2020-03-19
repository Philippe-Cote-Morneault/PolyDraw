﻿using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.InteropServices;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
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
        private long _totalRound;
        private StrokesReader _stokesReader;
        private string _canvasMessage;
        private GameModes _mode;
        private bool _roundStarted;
        public ServerStrokeDrawerService StrokeDrawerService { get; set; }

        public GameViewModel()
        {
            _players = new ObservableCollection<Player>();
            InitEventHandler();
            CanStillGuess = true;
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
            SocketClient.HintResponse += SocketClientOnHintResponse;
        }

        private void SocketClientOnHintResponse(object source, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            if (e.HasHint)
            {
                DialogHost.Show(((MatchEventArgs) args).Hint);
            }
            else
            {
                DialogHost.Show(((MatchEventArgs) args).Error);
            }
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
                

                if(BitConverter.IsLittleEndian)
                    Array.Reverse(idTodelete);

                Editor.Canvas.RemoveStroke(new Guid(idTodelete));
            });
        }

        private void SocketClientOnServerStrokeSent(object source, EventArgs args)
        {
            if (_roundStarted)
            {
                StrokeDrawerService?.Enqueue((args as StrokeSentEventArgs).StrokeInfo);
            }
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
            _roundStarted = true;
            CanStillGuess = true;
            Players.ToList().ForEach(p => p.IsDrawing = false);
            Players.ToList().ForEach(p => p.GuessedTheWord = false);
            Players.FirstOrDefault(p => p.User.ID == e.UserID).IsDrawing = true;
            NotifyPropertyChanged(nameof(DrawerIsCPU));
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
            _drawingID = e.DrawingID;
            ShowCanvasMessage($"It is your turn to draw to word {e.Word}");
            ChangeCanvasStatus(true);
        }

        private void SocketClientOnMatchSync(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            Round = e.Laps;
            TotalRound = e.LapTotal;
            Time = e.Time;
            NotifyPropertyChanged(nameof(Players));
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
                
                //ShowCanvasMessage($"+ {e.Points}");
                Player player = Players.First(p => p.User.ID == SessionInformations.User.ID);
                player.PointsRecentlyGained = e.Points;
                player.Score = e.PointsTotal;

                NotifyPropertyChanged(nameof(Players));

                Application.Current.Dispatcher.Invoke(() => 
                {
                    CanStillGuess = false;
                    SoundService.PlayWordGuessedRight();
                });
            }
            else
            {
                ShowCanvasMessage($"Try again !");
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
            Time = DateTime.MinValue;
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
            //TODO show leaderboard
            StrokeDrawerService.Stop();
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

        public void OrderPlayers()
        {
            Players = new ObservableCollection<Player>(_players.OrderByDescending(p => p.Score));
        }

        public ObservableCollection<Player> Players
        {
            get => _players;
            
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

        private bool _canStillGuess;
        public bool CanStillGuess
        {
            get => _canStillGuess;
            set
            {
                _canStillGuess = value;
                NotifyPropertyChanged();
            }
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
        
        public long TotalRound
        {
            get => _totalRound;
            set
            {
                _totalRound = value;
                NotifyPropertyChanged();
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
               NotifyPropertyChanged(nameof(IsMessageDisplay));
            } 
        }

        public GameModes Mode
        {
            get => _mode;
            set
            {
                _mode = value;
                NotifyPropertyChanged();
            }
        }


        public bool DrawerIsCPU
        {
            get
            {
                var player = Players.FirstOrDefault(p => p.IsDrawing);
                if(player != null)
                {
                    return player.IsCPU;
                }
                return false;
            } 
            
        }

        public bool IsMessageDisplay
        {
            get => !String.IsNullOrWhiteSpace(CanvasMessage);
        }

        public Editor Editor { get; set; }
        
        RelayCommand<object> _askHintCommand;

        public ICommand AskHintCommand
        {
            get
            {
                return _askHintCommand ??
                       (_askHintCommand = new RelayCommand<object>(channel => SocketClient.SendMessage(new Tlv(SocketMessageTypes.AskForHint))));
            }
        }
        
        
        
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
            _roundStarted = false;
            Word = "";
        }
        private void SendGuess()
        {
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.GuessTheWord, new string(Guess)));
        }

        private void ClearCanvas()
        {
            Application.Current.Dispatcher.Invoke(() =>
            {
                Editor.SelectPen();
                Editor.Canvas.Strokes.Clear();
            });
        }

        private void ChangeCanvasStatus(bool isEnable)
        {
            Application.Current.Dispatcher.Invoke(() => { Editor.IsEnabled = isEnable; });
            if (isEnable)
            {
                _stokesReader.Start(_drawingID);
            }
            else
            {
                _stokesReader.Stop();
                
            }

            Task.Run(() =>
            {
                Thread.Sleep(100);
                ClearCanvas();
            });
        }

        private void PrepareMatch()
        {
            _mode = Lobby.Mode;
            if (_mode == GameModes.FFA)
            {
            }
            else
            {
                HealthPoint = 3;
            }
            Application.Current.Dispatcher.Invoke(() =>
            {
                Players = Lobby.Players;
                _stokesReader = new StrokesReader(Editor, SocketClient, ((EditorViewModel)Editor.DataContext).EditorInformation);
                ChangeCanvasStatus(false);
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


        private bool _guessButtonIsDefault;
        public bool GuessButtonIsDefault
        {
            get => _guessButtonIsDefault;
            set
            {
                _guessButtonIsDefault = value;
                NotifyPropertyChanged();
            }
        }


    }
}