using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Threading;
using ClientLourd.Services.ServerStrokeDrawerService;
using ClientLourd.Services.SocketService;
using ClientLourd.Services.SoundService;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls.Game
{
    public partial class EditorZone : UserControl
    {
        private Random _random;
        private DispatcherTimer _timer;

        public EditorZone()
        {
            InitializeComponent();
            Loaded += OnLoaded;
            SocketClient.GuessResponse += SocketClientOnGuessResponse;
            SocketClient.MatchTimesUp += SocketClientOnMatchTimesUp;
            SocketClient.MatchEnded += SocketClientOnMatchEnded;
            SocketClient.NewPlayerIsDrawing += SocketClientNewPlayerDrawing;
            SocketClient.RoundEnded += SocketClientOnRoundEnded;
            _random = new Random((int)DateTime.Now.Ticks);
            _timer = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(10) };
            _timer.Tick += (s, arg) => Confetti();
        }

        private void SocketClientOnRoundEnded(object source, EventArgs args)
        {
            Task.Run(() =>
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    LeaderBoard l = new LeaderBoard((MatchEventArgs)args);
                    LeaderBoardGrid.Children.Add(l);
                    LeaderBoardGrid.Visibility = Visibility.Visible;
                });
                Thread.Sleep(2000);
                Application.Current.Dispatcher.Invoke(() =>
                {
                    LeaderBoardGrid.Children.Clear();
                    LeaderBoardGrid.Visibility = Visibility.Collapsed;
                });
            });

    }


        private void SocketClientOnMatchEnded(object source, EventArgs args)
        {
            Task.Run(() =>
            {
                StartConfetti();
                Thread.Sleep(5000);
                StopConfetti();
            });
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

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            ((GameViewModel) DataContext).Editor = DrawingEditor;
            ((GameViewModel)DataContext).StrokeDrawerService = new ServerStrokeDrawerService(DrawingEditor.Canvas, false);
        }

        private GameViewModel ViewModel
        {
            get => Application.Current.Dispatcher.Invoke(() => { return (GameViewModel) DataContext; });
        }

        private void TextBoxBase_OnTextChanged(object sender, TextChangedEventArgs e)
        {
            var tb = (TextBox) sender;
            var index = (int) tb.Tag;
            if (tb.Text != "")
            {
                ViewModel.Guess[index] = tb.Text[0];
                if (index != ViewModel.Guess.Length - 1)
                {
                    var request = new TraversalRequest(FocusNavigationDirection.Next);
                    request.Wrapped = true;
                    tb.MoveFocus(request); 
                }
            }
            else
            {
                ViewModel.Guess[index] = '\0';
            }
        }

        private void UIElement_OnKeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Back)
            {
                var tb = (TextBox) sender;
                if (tb != null)
                {
                    tb.Text = "";
                    var index = (int) tb.Tag;
                    if (index != 0)
                    {
                        var request = new TraversalRequest(FocusNavigationDirection.Previous);
                        request.Wrapped = true;
                        tb.MoveFocus(request);
                    }
                }
            }
        }

        private void SocketClientOnGuessResponse(object sender, EventArgs args)
        {
            var e = (MatchEventArgs)args;

            
            if (e.Valid)
            {
                Task.Run(() =>
                {   
                    PlayRightGuessAnimation(GetTextBoxes());        
                });
            }
            else
            {
                Task.Run(() =>
                {
                    PlayWrongGuessAnimation(GetTextBoxes());
                });
            }
        }

        private List<TextBox> GetTextBoxes() 
        {
            List<TextBox> textBoxes = new List<TextBox>();

            for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ContentPresenter c = (ContentPresenter)GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                    textBoxes.Add(c.ContentTemplate.FindName("textbox", c) as TextBox);
                });
            }

            return textBoxes;
        }

        private void PlayRightGuessAnimation(List<TextBox> textBoxes) 
        {
            for (int i = 0; i < textBoxes.Count; i++)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    Storyboard sb = (Storyboard)FindResource("GuessRight");
                    for (int j = 0; j < sb.Children.Count; j++)
                    {
                        Storyboard.SetTarget(sb.Children[j], textBoxes[i]);
                    }
                    sb.Begin();
                });
                Thread.Sleep(200);
            }
        }

        private void PlayWrongGuessAnimation(List<TextBox> textBoxes)
        {

            Application.Current.Dispatcher.Invoke(() =>
            {
                Storyboard sb = (Storyboard)FindResource("GuessWrong");

                for (int i = 0; i < textBoxes.Count; i++)
                {
                    for (int j = 0; j < sb.Children.Count; j++)
                    {
                        Storyboard.SetTarget(sb.Children[j], textBoxes[i]);
                    }
                    sb.Begin();
                }
            });
        }

        private void SocketClientOnMatchTimesUp(object sender, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            if (e.Type == 1)
            {
                Task.Run(() =>
                {
                    Thread.Sleep(2000);
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        Storyboard sb = (Storyboard)FindResource("NextRoundBegin");
                        sb.Begin();
                    });
                    Thread.Sleep(1000);
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        Storyboard sb = (Storyboard)FindResource("NextRoundEnd");
                        sb.Begin();
                    });
                });
            }
        }


        private void StartConfetti()
        {
            _timer.Start();
        }

        private void StopConfetti()
        {
            _timer.Stop();
        }
       

        private void Confetti() 
        {

            int canvasTop = -(int)(CanvasContainer.ActualHeight / 2) + 60;
            int canvasBottom = (int)(CanvasContainer.ActualHeight / 2);
            int canvasTopLeft = -(int)CanvasContainer.ActualWidth / 2;
            int canvasTopRight = (int)CanvasContainer.ActualWidth / 2;

            int x = _random.Next(canvasTopLeft, canvasTopRight);
            int y = canvasTop;
            double s = _random.Next(2, 5) * .1;
            int r = _random.Next(0, 270);

            TransformGroup transformGroup = new TransformGroup();
            transformGroup.Children.Add(new ScaleTransform(s, s));
            transformGroup.Children.Add(new RotateTransform(r));
            transformGroup.Children.Add(new TranslateTransform(x,y));

            Confetti confetti = new Confetti()
            {   
                RenderTransformOrigin = new Point(0.5, 0.5),
                RenderTransform = transformGroup,
            };
            
            CanvasContainer.Children.Add(confetti);

            Duration d = new Duration(TimeSpan.FromSeconds(_random.Next(1, 4)));

            int endY = canvasBottom;
            DoubleAnimation ay = new DoubleAnimation {From=y, To = endY, Duration = d };
            Storyboard.SetTarget(ay, confetti);
            Storyboard.SetTargetProperty(ay, new PropertyPath("(RenderTransform).Children[2].(TranslateTransform.Y)"));

            int endR = r + _random.Next(90, 360);
            DoubleAnimation ar = new DoubleAnimation {From=r, To = endR, Duration = d };
            Storyboard.SetTarget(ar, confetti);
            Storyboard.SetTargetProperty(ar, new PropertyPath("(RenderTransform).Children[1].(RotateTransform.Angle)"));
            
            Storyboard story = new Storyboard();
            story.Completed += (sender, e) => CanvasContainer.Children.Remove(confetti);
            story.Children.Add(ay);
            story.Children.Add(ar);
            story.Begin();
        }

        public void OnFocusLost(object sender, EventArgs e)
        {
            (DataContext as GameViewModel).GuessButtonIsDefault = false;
        }

        public void OnFocus(object sender, EventArgs e)
        {
            (DataContext as GameViewModel).GuessButtonIsDefault = true;
        }

        private void SocketClientNewPlayerDrawing(object sender, EventArgs e)
        {
            FocusFirstTextBox();
        }

        private void FocusFirstTextBox()
        {
            Task.Delay(100).ContinueWith(_ =>
            {
                Application.Current.Dispatcher.Invoke(new Action(() =>
                {
                    ContentPresenter c = (ContentPresenter)GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(0);
                    TextBox textBox = (c.ContentTemplate.FindName("textbox", c) as TextBox);
                    textBox.Focus();
                }));
            });
        }

        public void SelectFirstEmptyTextBox(object sender, EventArgs e)
        {
            for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
            {
                if (Convert.ToChar(GuessTextBoxes.Items[i]) == '\0')
                {
                    ContentPresenter c = (ContentPresenter)GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                    TextBox tb = c.ContentTemplate.FindName("textbox", c) as TextBox;
                    Action focusAction = () => tb.Focus();
                    Dispatcher.BeginInvoke(focusAction, DispatcherPriority.Render);

                    return;
                }
            }
        }
    }
}