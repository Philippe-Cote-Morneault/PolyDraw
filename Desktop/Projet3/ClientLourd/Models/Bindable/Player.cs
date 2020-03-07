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
        private int _score;
        private bool _isCPU;


        public Player(bool isCPU, string id, string username)
        {
            IsCPU = isCPU;
            User = new User(username, id);
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
        
    public bool IsCPU
        {
            get => _isCPU;
            set
            {
                if (value != _isCPU)
                {
                    _isCPU = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public int Score
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
    }
}
