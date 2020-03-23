using ClientLourd.Utilities.Enums;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.Bindable
{
    public class Lobby: ModelBase
    {
        public Lobby(
            
            string gameID,
            string host, 
            string hostID, 
            ObservableCollection<Player> players,
            GameModes gameMode, 
            DifficultyLevel difficulty, 
            int playersCount,
            int nPlayersMax,
            Languages language,
            int nbRounds
            )
        {
            PlayersCount = playersCount;
            Players = players;
            PlayersMax = nPlayersMax;
            
            ID = gameID;
            Host = host;
            HostID = hostID;
            Mode = gameMode;
            Difficulty = difficulty;
            Language = language;
            Rounds = nbRounds;
        }
        public string ID { get; set; }

        public string HostID { get; set; }

        public Languages Language { get; set; }

        public int PlayersMax { get; set; }

        public int Rounds { get; set; }

        private int _playersCount;
        public int PlayersCount { get => _playersCount; set { _playersCount = value; NotifyPropertyChanged(); } }

        private string _host;
        public string Host 
        { 
            get => _host;
            set
            {
                _host = value;
                NotifyPropertyChanged();
            } 
        }

        public DifficultyLevel Difficulty { get; set; }

        private ObservableCollection<Player> _players;

        public GameModes Mode { get; set; }

        public ObservableCollection<Player> Players 
        {
            get { return _players; }
            set
            {
                if ( value != _players) 
                {
                    _players = value;
                    NotifyPropertyChanged();
                }
            }
        }

        
    }
}
