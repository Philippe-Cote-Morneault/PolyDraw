using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.Bindable
{
    public class Stats: ModelBase
    {
        private int _gamesPlayed;
        private double _winRatio;
        private long _avgGameDuration;
        private long _timePlayed;
        private StatsHistory _statsHistory;

        private LinkedList<object> test1;
        private LinkedList<object> test2;


        public Stats()
        {
            _statsHistory = new StatsHistory();
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

        /*public LinkedList<object> ConnectionHistory
        {
            get { return test1; }
            set
            {
                if (value != test1)
                {
                    test1 = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public LinkedList<object> MatchesPlayedHistory
        {
            get { return test2; }
            set
            {
                if (value != test2)
                {
                    test2 = value;
                    NotifyPropertyChanged();
                }
            }
        }*/

        public LinkedList<ConnectionDisconnection> ConnectionHistory
        {
            get { return _statsHistory.ConnectionHistory; }
            set
            {
                if (value != _statsHistory.ConnectionHistory)
                {
                    _statsHistory.ConnectionHistory = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public LinkedList<MatchPlayed> MatchesPlayedHistory
        {
            get { return _statsHistory.MatchesPlayedHistory; }
            set
            {
                if (value != _statsHistory.MatchesPlayedHistory)
                {
                    _statsHistory.MatchesPlayedHistory = value;
                    NotifyPropertyChanged();
                }
            }
        }

    }
}
