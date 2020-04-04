using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.Bindable
{
    public class Stats : ModelBase
    {
        private int _gamesPlayed;
        private double _winRatio;
        private long _avgGameDuration;
        private long _timePlayed;
        private long _bestScoreSolo;


        public Stats()
        {
        }

        public int GamesPlayed
        {
            get { return _gamesPlayed; }
            set
            {
                _gamesPlayed = value;
                NotifyPropertyChanged();
            }
        }

        public double WinRatio
        {
            get { return _winRatio; }
            set
            {
                _winRatio = value;
                NotifyPropertyChanged();
            }
        }

        public long AvgGameDuration
        {
            get { return _avgGameDuration; }
            set
            {
                _avgGameDuration = value;
                NotifyPropertyChanged();
            }
        }

        public long TimePlayed
        {
            get { return _timePlayed; }
            set
            {
                _timePlayed = value;
                NotifyPropertyChanged();
            }
        }

        public long BestScoreSolo
        {
            get { return _bestScoreSolo; }
            set
            {
                _bestScoreSolo = value;
                NotifyPropertyChanged();
            }
        }
    }
}