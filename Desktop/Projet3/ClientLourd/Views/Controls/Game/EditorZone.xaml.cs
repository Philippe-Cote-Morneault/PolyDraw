using System;
using System.Collections.Generic;
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



        public EditorZone()
        {
            InitializeComponent();
            Loaded += OnLoaded;
            SocketClient.GuessResponse += SocketClientOnGuessResponse;
            SocketClient.MatchTimesUp += SocketClientOnMatchTimesUp;
            
            SocketClient.NewPlayerIsDrawing += SocketClientNewDrawer;
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
            get => (GameViewModel) DataContext;
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

            Application.Current.Dispatcher.Invoke(() =>
            {
                StartConfetti();
            });

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
                System.Threading.Thread.Sleep(200);
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
            
            Application.Current.Dispatcher.Invoke(() =>
            {
                Storyboard sb = (Storyboard)FindResource("NextRoundBegin");
                sb.Begin();
            });
            
        }

        private void SocketClientNewDrawer(object sender, EventArgs args)
        {
            var e = (MatchEventArgs)args;
            
            Application.Current.Dispatcher.Invoke(() =>
            {
                //TODO: Uncomment if statement when Martin fixed this
                //if ((DataContext as GameViewModel).Round > 1)
                //{
                    Storyboard sb = (Storyboard)FindResource("NextRoundEnd");
                    sb.Begin();
                //}                
            });
            
        }

        Random _random = new Random((int)DateTime.Now.Ticks);

        private void StartConfetti()
        {
            var t = new DispatcherTimer { Interval = TimeSpan.FromMilliseconds(10) };
            t.Tick += (s, arg) => Confetti();
            t.Start();
        }
       

        private void Confetti() 
        {
            var x = _random.Next(-500, (int)CanvasContainer.ActualWidth - 100);
            //var y = -100;
            //TODO change to -100
            var y = -100;
            var s = _random.Next(5, 15) * .1;
            var r = _random.Next(0, 270);

            var transformGroup = new TransformGroup();
            transformGroup.Children.Add(new ScaleTransform(s, s));
            transformGroup.Children.Add(new RotateTransform(r));
            transformGroup.Children.Add(new TranslateTransform(x,y));

            var flake = new Confetti()
            {
                RenderTransform = transformGroup,
            };

            CanvasContainer.Children.Add(flake);

            var d = TimeSpan.FromSeconds(_random.Next(1, 4));
            var story = new Storyboard();
            x += _random.Next(100, 500);
            var ax = new DoubleAnimation { To = x, Duration = d };
            Storyboard.SetTarget(ax, flake.RenderTransform);
            //Storyboard.SetTargetProperty(ax, new PropertyPath("(RenderTransform).Children[2].(TranslateTransform.X)"));
            Storyboard.SetTargetProperty(ax, new PropertyPath("(TranslateTransform.X)"));

            y += (int)(CanvasContainer.ActualHeight + 200);
            var ay = new DoubleAnimation { To = y, Duration = d };
            Storyboard.SetTarget(ax, flake.RenderTransform);
            //Storyboard.SetTargetProperty(ax, new PropertyPath("(RenderTransform).Children[2].(TranslateTransform.Y)"));
            Storyboard.SetTargetProperty(ax, new PropertyPath("(TranslateTransform.Y)"));

            r += _random.Next(90, 360);
            var ar = new DoubleAnimation { To = r, Duration = d };
            Storyboard.SetTarget(ar, flake.RenderTransform);
           // Storyboard.SetTargetProperty(ar, new PropertyPath("RenderTransform.Children[1].Angle"));
            //Storyboard.SetTargetProperty(ar, new PropertyPath("(RenderTransform).Children[1].(RotateTransform.Angle)"));
            //Storyboard.SetTargetProperty(ar, new PropertyPath("(UIElement.RenderTransform).(TransformGroup.Children)[1].(RotateTransform.Angle)"));

            // Storyboard.SetTargetProperty(ar, new PropertyPath("(UIElement.RenderTransform).Children[1].(RotateTransform.Angle)"));
             Storyboard.SetTargetProperty(ar, new PropertyPath("(RotateTransform.Angle)"));

            //(UIElement.RenderTransform).Children[1].(RotateTransform.Angle)
            //(RenderTransform).Children[0].(ScaleTransform.ScaleX)

            story.Children.Add(ax);
            story.Children.Add(ay);
            story.Children.Add(ar);
            story.Begin();
        }



    }
}