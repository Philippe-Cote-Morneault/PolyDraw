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
        public LeaderBoard(MatchEventArgs e)
        {
            Players = new ObservableCollection<Player>();
            ExtractInformation(e);
            InitializeComponent();
        }

        private void ExtractInformation(MatchEventArgs e)
        {
            foreach (dynamic info in e.Players)
            {
                Player p = new Player(false, info["UserID"], info["Username"]);
                p.PointsRecentlyGained = info["Points"];
                p.Score = info["PointsTotal"];
                Players.Add(p);
            }

            Players = new ObservableCollection<Player>(Players.OrderBy(p => p.Score));
            OnPropertyChanged(nameof(Players));
        }
        
        public ObservableCollection<Player> Players { get; set; }
        public event PropertyChangedEventHandler PropertyChanged;

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}