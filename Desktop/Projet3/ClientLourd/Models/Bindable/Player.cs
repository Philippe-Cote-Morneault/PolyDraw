using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

﻿namespace ClientLourd.Models.Bindable
{
    public class Player: ModelBase
    {
        private User _user;
        private bool _isDrawing;
        private bool _guessedTheWord;
        private long _score;
        private long _pointsRecentlyGained;


        public Player(bool isCPU, string id, string username)
        {
            User = new User(username, id, isCPU);
        }

        public Player()
        {

        }
        
        public User User
        {
            get => _user;
            set
            {
                if (value != _user)
                {
                    _user = value;
                    NotifyPropertyChanged();
                }
            }
        }
        

        public long Score
        {
            get => _score;
            set
            {
                if (value != _score)
                {
                    _score = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public bool GuessedTheWord
        {
            get => _guessedTheWord;
            set
            {
                if (value != _guessedTheWord)
                {
                    _guessedTheWord = value;
                    NotifyPropertyChanged();
                }
            }
        }
        
        public bool IsDrawing
        {
            get => _isDrawing;
            set
            {
                if (value != _isDrawing)
                {
                    _isDrawing = value;
                    NotifyPropertyChanged();
                    
                }
            }
        }

        public long PointsRecentlyGained
        {
            get => _pointsRecentlyGained;
            set
            {
                if (value != _pointsRecentlyGained)
                {
                    _pointsRecentlyGained = value;
                    NotifyPropertyChanged();
                }
            }
        }
    }
}
