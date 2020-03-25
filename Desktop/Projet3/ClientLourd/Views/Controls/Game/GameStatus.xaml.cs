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
 
            Loaded += OnLoaded;
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            GameViewModel.ScoreUpdatedEvent += OnScoreUpdated;
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