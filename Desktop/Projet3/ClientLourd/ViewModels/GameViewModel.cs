using System;
using System.Collections.ObjectModel;
using System.Linq;
using System.Timers;
using ClientLourd.Models.Bindable;

namespace ClientLourd.ViewModels
{
    public class GameViewModel : ModelBase
    {

        private string _guess;
        private string _artistID;
        private DateTime _time;
        private Timer _timer;
        public GameViewModel()
        {
            InitTimer();
        }

        private void InitTimer()
        {
            Time = DateTime.MinValue.AddMinutes(1);
            _timer = new Timer(1000);
            _timer.Elapsed += (sender, args) => { Time = Time.AddSeconds(-1); };
            _timer.Start();
        }

        public User Artist
        {
            get => Users.FirstOrDefault(u => u.ID == _artistID);
        }
        
        public ObservableCollection<User> Users { get; set; }

        
        public string Guess
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




    }
}