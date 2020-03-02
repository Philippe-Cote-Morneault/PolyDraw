using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Timers;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Utilities.Commands;
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
            NotifyPropertyChanged(nameof(Players));
        }

        private void InitTimer()
        {
            Time = DateTime.MinValue.AddMinutes(1);
            _timer = new Timer(1000);
            _timer.Elapsed += (sender, args) => { 
                Time = Time.AddSeconds(-1);
                if(Time == DateTime.MinValue)
                {
                    _timer.Stop();
                }
            };
            _timer.Start();
        }

        public Player Artist
        {
            get => Players.FirstOrDefault(p => p.IsDrawing);
        }
        
        public ObservableCollection<Player> Players { get; set; }
        
        
        public char[] Guess
        {
            get => _guess;
            set
            {
                _guess = value;
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
            DialogHost.Show(new MessageDialog("Guess", new string(Guess)), "Default");
        }



    }
}