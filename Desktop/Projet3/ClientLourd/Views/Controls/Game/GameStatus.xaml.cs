using ClientLourd.Models.Bindable;
using ClientLourd.Services.SocketService;
using ClientLourd.ViewModels;
using System;
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
            SocketClient.GuessResponse += SocketClientOnGuessResponse;
            SocketClient.MatchSync += SocketClientOnMatchSync;
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

        public GameViewModel GameViewModel
        {
            get => Application.Current.Dispatcher.Invoke(() => { return (GameViewModel)DataContext; });
        }

        public SessionInformations SessionInformations
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                {
                    return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)
                        ?.SessionInformations;
                });
            }
        }

        private void SocketClientOnMatchSync(object source, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            
            var playersInfo = e.Players;

            foreach (dynamic info in playersInfo)
            {
                var tmpPlayer = GameViewModel.Players.First(p => p.User.ID == info["UserID"]);
                if (tmpPlayer.Score != info["Points"])
                {
                    tmpPlayer.PointsRecentlyGained = info["Points"] - tmpPlayer.Score;
                    AnimatePointsGained(info["UserID"]);
                }
                tmpPlayer.Score = info["Points"];
            }

            

            GameViewModel.Players = GameViewModel.Players;
        }

        private void SocketClientOnGuessResponse(object source, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            if (e.Valid)
            {
                AnimatePointsGained(SessionInformations.User.ID);
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