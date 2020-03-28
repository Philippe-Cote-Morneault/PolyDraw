﻿using ClientLourd.Services.SocketService;
using ClientLourd.Services.SoundService;
using ClientLourd.Utilities.Enums;
using ClientLourd.ViewModels;
using System;
using System.Threading;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Animation;

namespace ClientLourd.Views.Controls.Game
{
    public partial class TopBar : UserControl
    {
        public TopBar()
        {
            InitializeComponent();
            SocketClient.MatchSync += SocketClientOnMatchSync;
            SocketClient.NewPlayerIsDrawing += SocketClientOnNewPlayerIsDrawing;
            SocketClient.MatchCheckPoint += SocketClientMatchCheckPoint;
            SocketClient.CoopWordGuessed += SocketClientCoopWordGuessed;
            SocketClient.CoopTeamateGuessedIncorrectly += SocketClientTeamateGuessedWrong;
        }

       

        private void SocketClientCoopWordGuessed(object source, EventArgs args)
        {
            MatchEventArgs e = (MatchEventArgs)args;
            
            if (GameViewModel.Mode == GameModes.Coop)
            {
                GameViewModel.TeamPoints = e.Points;
                GameViewModel.TeamNewPoints = e.NewPoints;
                Application.Current.Dispatcher.Invoke(() => AnimatePointsGained());
            }
        }

        private void AnimatePointsGained()
        {
            Storyboard sb = (Storyboard)FindResource("PointsGainedAnimations");
            sb.Begin();
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

        public SoundService SoundService
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SoundService; }
        }

        public GameViewModel GameViewModel
        {
            get => Application.Current.Dispatcher.Invoke(() => { return (GameViewModel)DataContext; }); 
        }

        private void SocketClientOnMatchSync(object sender, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            DateTime timeLeft = e.Time;
            if (TimeSpan.FromTicks(timeLeft.Ticks).TotalSeconds <= 10)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    SoundService.PlayTimerWarning();
                    Storyboard sb = (Storyboard)FindResource("TimerCloseToEnd");
                    sb.Begin();
                });
            }
        }

        


        private void AnimateLostHeart(int lives) 
        {
            UIElement el = (HeartsContainer.Children[GameViewModel.HealthPoint - 1] as UIElement);
            GameViewModel.HealthPoint = lives;
            Storyboard sb = (Storyboard)FindResource("HealthLost");
            for (int j = 0; j < sb.Children.Count; j++)
            {
                Storyboard.SetTarget(sb.Children[j], el);
            }
            sb.Begin();
        }

        private void SocketClientTeamateGuessedWrong(object source, EventArgs args)
        {
            var e = (MatchEventArgs)args;

            Application.Current.Dispatcher.Invoke(() =>
            {
                if ((GameViewModel.Mode == GameModes.Coop || GameViewModel.Mode == GameModes.Solo) && GameViewModel.HealthPoint > 0)
                {
                    AnimateLostHeart(e.Lives);
                }

            });
        }


        private void SocketClientOnNewPlayerIsDrawing(object sender, EventArgs args)
        {
            var e = (MatchEventArgs)args;

            if (GameViewModel.Mode == GameModes.Coop || GameViewModel.Mode == GameModes.Solo)
            {
                if (GameViewModel.HealthPoint != 3)
                {
                    GameViewModel.HealthPoint = 3;
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        AnimateHeartsReset();
                    });
                }
            }
        }

        private void AnimateHeartsReset()
        {
            
            foreach(UIElement el in HeartsContainer.Children)
            {
                if ((((el.RenderTransform as TransformGroup).Children[0]) as ScaleTransform).ScaleX < 1)
                {
                    Storyboard sb = (Storyboard)FindResource("HealthReset");
                    foreach (DependencyObject animation in sb.Children)
                    {
                        Storyboard.SetTarget(animation, el);
                    }
                    sb.Begin();
                }
                
            }
        }
        private void SocketClientMatchCheckPoint(object sender, EventArgs args)
        {
            var e = (MatchEventArgs)args;

            if (e.Bonus > 0)
            {
                GameViewModel.TimeGained = DateTime.MinValue.AddMilliseconds(e.Bonus);
                Application.Current.Dispatcher.Invoke(() =>
                {
                    AnimateTimeGained();
                });
            }
            else if (e.Bonus < 0)
            {
                GameViewModel.TimeGained = DateTime.MinValue.AddMilliseconds(-e.Bonus);
                Application.Current.Dispatcher.Invoke(() =>
                {
                    AnimateTimeLost();
                });
            } 
        }

        private void AnimateTimeGained()
        {
            Storyboard sb = (Storyboard)FindResource("TimeGainedAnimation");
            sb.Begin();
        }

        private void AnimateTimeLost()
        {
            Storyboard sb = (Storyboard)FindResource("TimeLostAnimation");
            sb.Begin();
        }

        
    }
}