using System.Collections.ObjectModel;
using System.Windows.Controls;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.SocketService;

namespace ClientLourd.Views.Controls.Game
{
    public partial class LeaderBoard : UserControl
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
        }
        
        public ObservableCollection<Player> Players { get; set; }
    }
}