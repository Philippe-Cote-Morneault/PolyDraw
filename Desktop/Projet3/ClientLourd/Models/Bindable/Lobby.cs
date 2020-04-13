using ClientLourd.Utilities.Enums;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using ClientLourd.Utilities.Constants;
using ClientLourd.Services.EnumService;

namespace ClientLourd.Models.Bindable
{
    public class Lobby : ModelBase
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

            Duration = CalculateDuration();
        }

        public string ID { get; set; }

        public string HostID { get; set; }

        public Languages Language { get; set; }

        public int PlayersMax { get; set; }

        public string LanguageDesc
        {
            get => Language.GetDescription();
        }
        public int Rounds { get; set; }

        private int _playersCount;

        public int PlayersCount
        {
            get => _playersCount;
            set
            {
                _playersCount = value;
                NotifyPropertyChanged();


                Duration = CalculateDuration();
            }
        }


        private DateTime _duration;

        public DateTime Duration
        {
            get => _duration;
            set
            {
                _duration = value;
                NotifyPropertyChanged();
            }
        }

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

        private DifficultyLevel _difficulty;

        public DifficultyLevel Difficulty
        {
            get => _difficulty;
            set
            {
                _difficulty = value;
                NotifyPropertyChanged();
            }
        }

        private ObservableCollection<Player> _players;

        GameModes _mode;

        public GameModes Mode
        {
            get => _mode;
            set
            {
                _mode = value;
                NotifyPropertyChanged();
            }
        }

        public ObservableCollection<Player> Players
        {
            get { return _players; }
            set
            {
                if (value != _players)
                {
                    _players = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public DateTime CalculateDuration()
        {
            if (Mode == GameModes.FFA)
            {
                return DateTime.Now.Date + TimeSpan.FromMinutes(_playersCount * Rounds);
            }
            else
            {
                switch (Difficulty)
                {
                    case DifficultyLevel.Easy:
                        return DateTime.Now.Date + TimeSpan.FromMinutes(GameDurations.EASY_GAME);

                    case DifficultyLevel.Medium:
                        return DateTime.Now.Date + TimeSpan.FromMinutes(GameDurations.MEDIUM_GAME);

                    case DifficultyLevel.Hard:
                        return DateTime.Now.Date + TimeSpan.FromMinutes(GameDurations.HARD_GAME);

                    default:
                        return DateTime.Now.Date + TimeSpan.FromMinutes(-1);
                }
            }
        }
    }
}