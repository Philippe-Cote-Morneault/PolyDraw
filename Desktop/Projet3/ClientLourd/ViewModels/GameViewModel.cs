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
        private string _artistID;
        private DateTime _time;
        private Timer _timer;
        public GameViewModel()
        {
            InitTimer();
            Guess = new char[20];
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

        public User Artist
        {
            get => Users.FirstOrDefault(u => u.ID == _artistID);
        }
        
        public ObservableCollection<User> Users { get; set; }
        
        
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