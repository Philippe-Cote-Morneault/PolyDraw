using ClientLourd.Models.Bindable;
using ClientLourd.Services.SocketService;
using ClientLourd.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Animation;

namespace ClientLourd.Views.Controls.Game
{
    public partial class GameStatus : UserControl
    {
        public GameStatus()
        {
            InitializeComponent();
            SocketClient.RoundEnded += SocketClientOnRoundEnded;
            SocketClient.MatchSync += SocketClientOnMatchSync;

        }

        private void SocketClientOnMatchSync(object source, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            UpdatePlayersScore(e.Players);
        }

        private void SocketClientOnRoundEnded(object source, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            UpdatePlayersScore(e.Players);
        }

        public SocketClient SocketClient
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                {
                    return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)
                        ?.SocketClient;
                });
            }
        }
        private void OnScoreUpdated(object source, string userID)
        {
            Application.Current.Dispatcher.Invoke(() => 
            {
                AnimatePointsGained(userID);
            });
        }


        public GameViewModel GameViewModel
        {
            get => Application.Current.Dispatcher.Invoke(() => { return (GameViewModel)DataContext; });
        }

        private void UpdatePlayersScore(dynamic playersInfo)
        {
            foreach (dynamic info in playersInfo)
            {
                var dic = (Dictionary<object, object>)info;
                if (!dic.ContainsKey("Points") || !dic.ContainsKey("UserID"))
                    break;
                var tmpPlayer = GameViewModel.Players.FirstOrDefault(p => p.User.ID == info["UserID"]);
                var newPoints = info["Points"] - tmpPlayer.Score;
                if (tmpPlayer != null && newPoints != 0)
                {
                    tmpPlayer.Score = info["Points"];
                    tmpPlayer.PointsRecentlyGained = newPoints;
                    AnimatePointsGained(tmpPlayer.User.ID);
                }
            }
        }


        private void AnimatePointsGained(string playerID)
        {
            for (int i = 0; i < List.Items.Count; i++)
            {
                if ((List.Items[i] as Player).User.ID == playerID)
                {
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        ContentPresenter c = (ContentPresenter)List.ItemContainerGenerator.ContainerFromIndex(i);
                        TextBlock tb = (c.ContentTemplate.FindName("PointsGainedTextBlock", c) as TextBlock);

                        Storyboard sb = (Storyboard)FindResource("PointsGainedAnimations");
                        for (int j = 0; j < sb.Children.Count; j++)
                        {
                            Storyboard.SetTarget(sb.Children[j], tb);
                        }
                        sb.Completed += (sender, ev) => GameViewModel.OrderPlayers();
                        sb.Begin();
                    });
                }
            }
        }
    }
}