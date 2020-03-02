namespace ClientLourd.Models.Bindable
{
    public class Player: ModelBase
    {
        private User _user;
        private bool _isDrawing;
        private bool _guessedTheWord;
        private int _score;
        
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