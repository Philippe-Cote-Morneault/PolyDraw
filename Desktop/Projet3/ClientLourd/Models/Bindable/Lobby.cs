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
            string gameName,
            string gameID,
            string host, 
            string hostID, 
            ObservableCollection<Player> players,
            GameModes gameMode, 
            DifficultyLevel difficulty, 
            int playersCount,
            int nPlayersMax
            )
        {
            PlayersCount = playersCount;
            Players = players;
            PlayersMax = nPlayersMax;
            GameName = gameName;
            ID = gameID;
            Host = host;
            HostID = hostID;
            Mode = gameMode;
            Difficulty = difficulty;
        }
        public string ID { get; set; }

        public string HostID { get; set; }


        public int PlayersMax { get; set; }
        public int PlayersCount { get; set; }
        public string GameName { get; set; }

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
