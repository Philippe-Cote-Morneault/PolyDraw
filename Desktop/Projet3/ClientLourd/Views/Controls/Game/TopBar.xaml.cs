using ClientLourd.Services.SocketService;
using ClientLourd.Services.SoundService;
using ClientLourd.ViewModels;
using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media.Animation;

namespace ClientLourd.Views.Controls.Game
{
    public partial class TopBar : UserControl
    {
        public TopBar()
        {
            InitializeComponent();
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

        public SoundService SoundService
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SoundService; }
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
    }
}