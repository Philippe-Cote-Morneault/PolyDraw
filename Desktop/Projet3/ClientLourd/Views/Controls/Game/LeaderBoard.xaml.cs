using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Windows.Controls;
using ClientLourd.Annotations;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.SocketService;

namespace ClientLourd.Views.Controls.Game
{
    public partial class LeaderBoard : UserControl, INotifyPropertyChanged
    {
        public LeaderBoard(MatchEventArgs e, bool gameEnded)
        {
            GameEnded = gameEnded;
            OnPropertyChanged(nameof(GameEnded));
            Players = new ObservableCollection<Player>();
            ExtractInformation(e);
            InitializeComponent();
        }

        private void ExtractInformation(MatchEventArgs e)
        {
            if (GameEnded)
            {
                Winner = e.WinnerName;
                OnPropertyChanged(nameof(Winner));
            }
            else
            {
                Word = e.Word;
                OnPropertyChanged(nameof(Word));
            }
            foreach (dynamic info in e.Players)
            {
                Player p = new Player(false, info["UserID"], info["Username"]);
                if (GameEnded)
                {
                    p.Score = info["Points"];
                }
                else
                {
                    p.PointsRecentlyGained = info["Points"];
                    p.Score = info["PointsTotal"];
                }
                Players.Add(p);
            }

            Players = new ObservableCollection<Player>(Players.OrderBy(p => p.Score));
            OnPropertyChanged(nameof(Players));
        }

        
        public ObservableCollection<Player> Players { get; set; }
        public string Word { get; set; }
        public string Winner { get; set; }
        public bool GameEnded { get; set; }
        public event PropertyChangedEventHandler PropertyChanged;

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}