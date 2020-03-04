﻿using ClientLourd.Utilities.Enums;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ClientLourd.Models.Bindable
{
    class Lobby: ModelBase
    {
        public Lobby(string gameName, string host, GameModes gameMode, DifficultyLevel difficulty, int playersCount,int nPlayersMax)
        {
            PlayersCount = playersCount;
            Players = new ObservableCollection<string>();
            PlayersMax = nPlayersMax;
            GameName = gameName;
            Host = host;
            Mode = gameMode;
            Difficulty = difficulty;
        }

        public int PlayersMax { get; set; }
        public int PlayersCount { get; set; }
        public string GameName { get; set; }
        public string Host { get; set; }

        public DifficultyLevel Difficulty { get; set; }

        private ObservableCollection<string> _players;

        public GameModes Mode { get; set; }

        public ObservableCollection<string> Players 
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
