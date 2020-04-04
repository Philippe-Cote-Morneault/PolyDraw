using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Threading;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.ServerStrokeDrawerService;
using ClientLourd.Services.SocketService;
using ClientLourd.Services.SoundService;
using ClientLourd.Utilities.Constants;
using ClientLourd.Utilities.Enums;
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
            _random = new Random((int) DateTime.Now.Ticks);
            _timer = new DispatcherTimer {Interval = TimeSpan.FromMilliseconds(10)};
            _timer.Tick += (s, arg) => Confetti();
        }

        public void AfterLogin()
        {
            InitEventHandler();
        }

        public void AfterLogout()
        {
        }

        private void InitEventHandler()
        {
            ViewModel.PropertyChanged += ViewModelOnPropertyChanged;
            SocketClient.GuessResponse += SocketClientOnGuessResponse;
            SocketClient.MatchTimesUp += SocketClientOnMatchTimesUp;
            SocketClient.MatchEnded += SocketClientOnMatchEnded;
            SocketClient.NewPlayerIsDrawing += SocketClientNewPlayerDrawing;
            SocketClient.RoundEnded += SocketClientOnRoundEnded;
            SocketClient.CoopTeamateGuessedIncorrectly += SocketClientTeamateGuessedWrong;
        }

        private void ViewModelOnPropertyChanged(object sender, PropertyChangedEventArgs e)
        {
            if (e.PropertyName == nameof(ViewModel.CanStillGuess))
            {
                FocusFirstTextBox();
            }
        }

        public SessionInformations SessionInformations
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                {
                    return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                        ?.SessionInformations;
                });
            }
        }

        private void SocketClientTeamateGuessedWrong(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;

            if (e.UserID != SessionInformations.User.ID)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    Storyboard sb = (Storyboard) FindResource("TeamateGuessedWrong");

                    sb.Begin();
                });
            }
        }

        private void SocketClientOnRoundEnded(object source, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            if (ViewModel.Mode == GameModes.FFA)
            {
                Task.Run(() =>
                {
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        LeaderBoardGrid.Children.Clear();
                        LeaderBoardGrid.Children.Add(new LeaderBoard(e, false));
                        LeaderBoardGrid.Visibility = Visibility.Visible;
                    });
                    Thread.Sleep(MatchTiming.ROUND_END_TIMEOUT);
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        LeaderBoardGrid.Children.Clear();
                        LeaderBoardGrid.Visibility = Visibility.Collapsed;
                    });
                });
            }
            else if (!e.Guessed)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ShowCanvasMessage($"{ViewModel.CurrentDictionary["WordWas"]} {e.Word}", MatchTiming.ROUND_END_TIMEOUT);
                });
            }
        }


        private void SocketClientOnMatchEnded(object source, EventArgs args)
        {
            var e = ((MatchEventArgs) args);
            if (ViewModel.Mode == GameModes.FFA)
            {
                Task.Run(() =>
                {
                    if (e.WinnerID == SessionInformations.User.ID)
                        StartConfetti();
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        LeaderBoardGrid.Children.Clear();
                        LeaderBoardGrid.Children.Add(new LeaderBoard((MatchEventArgs) args, true));
                        LeaderBoardGrid.Visibility = Visibility.Visible;
                    });
                    Thread.Sleep(MatchTiming.GAME_ENDED_TIMEOUT);
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        LeaderBoardGrid.Children.Clear();
                        LeaderBoardGrid.Visibility = Visibility.Collapsed;
                    });
                    if (e.WinnerID == SessionInformations.User.ID)
                        StopConfetti();
                });
            }
            else
            {
                //Coop and solo game end
                Task.Run(() =>
                {
                    StartConfetti();
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        ShowCanvasMessage(
                            $"{(string) ViewModel.CurrentDictionary["CoopSoloEnding"]} {ViewModel.TeamPoints}", MatchTiming.GAME_ENDED_TIMEOUT);
                        LeaderBoardGrid.Visibility = Visibility.Visible;
                    });
                    Thread.Sleep(MatchTiming.GAME_ENDED_TIMEOUT);
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        LeaderBoardGrid.Children.Clear();
                        LeaderBoardGrid.Visibility = Visibility.Collapsed;
                    });
                    StopConfetti();
                });
            }
        }

        private void ShowCanvasMessage(string message, int time = MatchTiming.ANNIMATION_TIMEOUT)
        {
            Task.Run(() =>
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    var tb = new TextBlock()
                    {
                        Text = message,
                        FontWeight = FontWeights.Black,
                        FontSize = 30,
                        Foreground = Brushes.White,
                        VerticalAlignment = VerticalAlignment.Center,
                        HorizontalAlignment = HorizontalAlignment.Center,
                    };
                    LeaderBoardGrid.Children.Clear();
                    LeaderBoardGrid.Children.Add(tb);
                    LeaderBoardGrid.Visibility = Visibility.Visible;
                });
                Thread.Sleep(time);
                Application.Current.Dispatcher.Invoke(() =>
                {
                    LeaderBoardGrid.Children.Clear();
                    LeaderBoardGrid.Visibility = Visibility.Collapsed;
                });
            });
        }

        public SocketClient SocketClient
        {
            get
            {
                return Application.Current.Dispatcher.Invoke(() =>
                {
                    return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)
                        ?.SocketClient;
                });
            }
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            ViewModel.Editor = DrawingEditor;
            ViewModel.StrokeDrawerService = new ServerStrokeDrawerService(DrawingEditor.Canvas, false);
            ViewModel.NewCanavasMessage += ShowCanvasMessage;
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
            var tb = (TextBox) sender;
            if (tb != null)
            {
                if (e.Key == Key.Back)
                {
                    if (tb.Text != "")
                    {
                        tb.Text = "";
                    }
                    else
                    {
                        var index = (int) tb.Tag;
                        if (index != 0)
                        {
                            var request = new TraversalRequest(FocusNavigationDirection.Previous);
                            request.Wrapped = true;
                            tb.MoveFocus(request);
                        }
                    }
                }
                else if (e.Key == Key.Left)
                {
                    var index = (int) tb.Tag;
                    if (index != 0)
                    {
                        var request = new TraversalRequest(FocusNavigationDirection.Previous);
                        request.Wrapped = true;
                        tb.MoveFocus(request);
                    }
                }
                else if (e.Key == Key.Right)
                {
                    var index = (int) tb.Tag;
                    if (index != ViewModel.Guess.Length - 1)
                    {
                        var request = new TraversalRequest(FocusNavigationDirection.Next);
                        request.Wrapped = true;
                        tb.MoveFocus(request);
                    }
                }
            }
        }

        private void SocketClientOnGuessResponse(object sender, EventArgs args)
        {
            var e = (MatchEventArgs) args;


            if (e.Valid)
            {
                Task.Run(() => { PlayRightGuessAnimation(GetTextBoxes()); });
            }
            else
            {
                Task.Run(() => { PlayWrongGuessAnimation(GetTextBoxes()); });
            }
        }

        private List<TextBox> GetTextBoxes()
        {
            List<TextBox> textBoxes = new List<TextBox>();

            for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ContentPresenter c = (ContentPresenter) GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
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
                    Storyboard sb = (Storyboard) FindResource("GuessRight");
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
                Storyboard sb = (Storyboard) FindResource("GuessWrong");

                for (int i = 0; i < textBoxes.Count; i++)
                {
                    for (int j = 0; j < sb.Children.Count; j++)
                    {
                        Storyboard.SetTarget(sb.Children[j], textBoxes[i]);
                    }

                    sb.Completed += (s, e) => ClearTextBoxes();
                    sb.Begin();
                }
            });
        }

        private void ClearTextBoxes()
        {
            for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ContentPresenter c = (ContentPresenter) GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                    TextBox tb = (c.ContentTemplate.FindName("textbox", c) as TextBox);
                    tb.Text = "";
                });
            }
        }

        private void SocketClientOnMatchTimesUp(object sender, EventArgs args)
        {
            var e = (MatchEventArgs) args;
            if (e.Type == 1)
            {
                Task.Run(() =>
                {
                    Thread.Sleep(MatchTiming.ROUND_END_TIMEOUT);
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        Storyboard sb = (Storyboard) FindResource("NextRoundBegin");
                        sb.Begin();
                    });
                    Thread.Sleep(MatchTiming.ANNIMATION_TIMEOUT);
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        Storyboard sb = (Storyboard) FindResource("NextRoundEnd");
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
            int canvasTop = -(int) (CanvasContainer.ActualHeight / 2) + 60;
            int canvasBottom = (int) (CanvasContainer.ActualHeight / 2);
            int canvasTopLeft = -(int) CanvasContainer.ActualWidth / 2;
            int canvasTopRight = (int) CanvasContainer.ActualWidth / 2;

            int x = _random.Next(canvasTopLeft, canvasTopRight);
            int y = canvasTop;
            double s = _random.Next(2, 5) * .1;
            int r = _random.Next(0, 270);

            TransformGroup transformGroup = new TransformGroup();
            transformGroup.Children.Add(new ScaleTransform(s, s));
            transformGroup.Children.Add(new RotateTransform(r));
            transformGroup.Children.Add(new TranslateTransform(x, y));

            Confetti confetti = new Confetti()
            {
                RenderTransformOrigin = new Point(0.5, 0.5),
                RenderTransform = transformGroup,
            };

            CanvasContainer.Children.Add(confetti);

            Duration d = new Duration(TimeSpan.FromSeconds(_random.Next(1, 4)));

            int endY = canvasBottom;
            DoubleAnimation ay = new DoubleAnimation {From = y, To = endY, Duration = d};
            Storyboard.SetTarget(ay, confetti);
            Storyboard.SetTargetProperty(ay, new PropertyPath("(RenderTransform).Children[2].(TranslateTransform.Y)"));

            int endR = r + _random.Next(90, 360);
            DoubleAnimation ar = new DoubleAnimation {From = r, To = endR, Duration = d};
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
                    try
                    {
                        ContentPresenter c =
                            (ContentPresenter) GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(0);
                        TextBox textBox = (c.ContentTemplate.FindName("textbox", c) as TextBox);
                        if (textBox != null)
                        {
                            textBox.Focus();
                        }
                    }
                    catch
                    {
                        //
                    }
                }));
            });
        }

        public void SelectFirstEmptyTextBox(object sender, EventArgs e)
        {
            if (!String.IsNullOrWhiteSpace((sender as TextBox).Text))
            {
                return;
            }


            for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
            {
                if (Convert.ToChar(GuessTextBoxes.Items[i]) == '\0')
                {
                    ContentPresenter c = (ContentPresenter) GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                    TextBox tb = c.ContentTemplate.FindName("textbox", c) as TextBox;

                    Action focusAction = () => tb.Focus();
                    Dispatcher.BeginInvoke(focusAction, DispatcherPriority.Render);

                    return;
                }
            }
        }
    }
}