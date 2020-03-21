using ClientLourd.Services.SocketService;
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
            SocketClient.GuessResponse += SocketClientOnGuessResponse;
            SocketClient.NewPlayerIsDrawing += SocketClientOnNewPlayerIsDrawing;
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
            if (timeLeft.Second <= 10)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    SoundService.PlayTimerWarning();
                    Storyboard sb = (Storyboard)FindResource("TimerCloseToEnd");
                    sb.Begin();
                });
            }
        }

        private void SocketClientOnGuessResponse(object sender, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            if (!e.Valid)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    // TODO: If solo
                    if (GameViewModel.Mode == GameModes.FFA && GameViewModel.HealthPoint > 0) 
                    {
                        AnimateLostHeart();
                    }

                });
            }
        }

        private void AnimateLostHeart() 
        {
            UIElement el = (HeartsContainer.Children[GameViewModel.HealthPoint - 1] as UIElement);
            GameViewModel.HealthPoint--;
            Storyboard sb = (Storyboard)FindResource("HealthLost");
            for (int j = 0; j < sb.Children.Count; j++)
            {
                Storyboard.SetTarget(sb.Children[j], el);
            }
            sb.Begin();
        }

        
        private void SocketClientOnNewPlayerIsDrawing(object sender, EventArgs args)
        {
            if (GameViewModel.Mode == GameModes.Coop || GameViewModel.Mode == GameModes.Solo || GameViewModel.Mode == GameModes.FFA)
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
                if ((((el.RenderTransform as TransformGroup).Children[0]) as ScaleTransform).ScaleX == 0)
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
    }
}