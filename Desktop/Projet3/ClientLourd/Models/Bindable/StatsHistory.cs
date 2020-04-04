using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.Bindable
{
    public class StatsHistory : ModelBase
    {
        private LinkedList<ConnectionDisconnection> _connectionHistory;
        private LinkedList<MatchPlayed> _matchesPlayed;

        public StatsHistory()
        {
            _connectionHistory = new LinkedList<ConnectionDisconnection>();
            _matchesPlayed = new LinkedList<MatchPlayed>();
        }

        public LinkedList<ConnectionDisconnection> ConnectionHistory
        {
            get { return _connectionHistory; }
            set
            {
                if (value != _connectionHistory)
                {
                    _connectionHistory = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public LinkedList<MatchPlayed> MatchesPlayedHistory
        {
            get { return _matchesPlayed; }
            set
            {
                if (value != _matchesPlayed)
                {
                    _matchesPlayed = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }

    public class ConnectionDisconnection : ModelBase
    {
        private long _connectedAt;
        private long _disconnectedAt;

        public ConnectionDisconnection()
        {
        }

        public long ConnectedAt
        {
            get { return _connectedAt; }
            set
            {
                if (value != _connectedAt)
                {
                    _connectedAt = value;
                    NotifyPropertyChanged();
                }
            }
        }

        // TODO: Change to DisconnectedAt
        public long DeconnectedAt
        {
            get { return _disconnectedAt; }
            set
            {
                if (value != _disconnectedAt)
                {
                    _disconnectedAt = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }


    public class MatchPlayed : ModelBase
    {
        // TODO: Add other attributes
        private long _matchDuration;
        private string _winnerName;
        private string _matchType;
        private string[] _playersName;

        public MatchPlayed()
        {
        }

        public long MatchDuration
        {
            get { return _matchDuration; }
            set
            {
                if (value != _matchDuration)
                {
                    _matchDuration = value;
                    NotifyPropertyChanged();
                }
            }
        }


        public string WinnerName
        {
            get { return _winnerName; }
            set
            {
                if (value != _winnerName)
                {
                    _winnerName = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public string MatchType
        {
            get { return _matchType; }
            set
            {
                if (value != _matchType)
                {
                    _matchType = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public string[] PlayersName
        {
            get { return _playersName; }
            set
            {
                if (value != _playersName)
                {
                    _playersName = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }
}