using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Runtime.InteropServices;
using System.Timers;
using System.Windows;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Enums;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.ViewModels
{
    public class GameViewModel : ModelBase
    {

        private char[] _guess;
        private DateTime _time;
        private Timer _timer;
        private int _healthPoint;
        private ObservableCollection<Player> _players;
        private int _round;
        public GameViewModel()
        {
            InitTimer();
            HealthPoint = 3;
            Guess = new char[20];
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
            SocketClient.MatchReadyToStart += SocketClientOnMatchReadyToStart;
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
            //Enable the canvas
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

        private void SocketClientOnMatchReadyToStart(object source, EventArgs args)
        {
            InitTimer();
            HealthPoint = 3;
            Guess = new char[20];
        }

        private void SocketClientOnMatchCheckPoint(object source, EventArgs args)
        {
            throw new NotImplementedException();
        }

        private void SocketClientOnMatchTimesUp(object source, EventArgs args)
        {
            throw new NotImplementedException();
        }

        private void SocketClientOnMatchEnded(object source, EventArgs args)
        {
            _timer.Stop();
            throw new NotImplementedException();
        }

        private void SocketClientOnMatchStarted(object source, EventArgs args)
        {
            _timer.Start();
        }

        private void InitTimer()
        {
            Time = DateTime.MinValue.AddMinutes(1);
            _timer = new Timer(1000);
            _timer.Elapsed += (sender, args) => { 
                Time = Time.AddSeconds(-5);
                if(Time == DateTime.MinValue)
                {
                    _timer.Stop();
                }
            };
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

        RelayCommand<object> _prepareMatchCommand;
        
        /// <summary>
        /// Set the environment for the match
        /// </summary>
        public ICommand PrepareMatchCommand
        {
            get
            {
                return _prepareMatchCommand ??
                       (_prepareMatchCommand = new RelayCommand<object>(channel => PrepareMatch()));
            }
        }

        private void PrepareMatch()
        {
            //TODO
            SocketClient.SendMessage(new Tlv(SocketMessageTypes.ReadyToStart));
        }
        
        
        


    }
}