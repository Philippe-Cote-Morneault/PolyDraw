using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using ClientLourd.Services.ServerStrokeDrawerService;
using ClientLourd.Services.SocketService;
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

            for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
            {
                ContentPresenter c = (ContentPresenter)GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                TextBox tb;

                Application.Current.Dispatcher.Invoke(() =>
                {
                    tb = c.ContentTemplate.FindName("textbox", c) as TextBox;
                    Storyboard sb = (Storyboard)FindResource("StoryBoard");

                    for (int j = 0; j < sb.Children.Count; j++)
                    {
                         Storyboard.SetTarget(sb.Children[j], tb);   
                    }
                    
                    /*DoubleAnimation anim1 = new DoubleAnimation(0, 10, new Duration(TimeSpan.FromSeconds(2)));
                    sb.Children.Add(anim1);
                    Storyboard.SetTarget(anim1, tb);
                    Storyboard.SetTargetProperty(anim1, new PropertyPath(TextBox.HeightProperty));*/

                    ColorAnimation colorAnimation = new ColorAnimation(Colors.Black, Colors.Green, new Duration(TimeSpan.FromSeconds(1)));

                    //sb.Children.Add(colorAnimation);
                    //Storyboard.SetTarget(colorAnimation, tb);
                    //Storyboard.SetTarget(sb.Children[0] as DoubleAnimation, tb);
                    //Storyboard.SetTargetProperty(colorAnimation, new PropertyPath(TextBox.ForegroundProperty));

                    sb.Begin();
                });
                //ContentPresenter c = (ContentPresenter)GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                //TextBox tb;
                //Application.Current.Dispatcher.Invoke(() => 
                //{
                //var sb = (Storyboard)FindResource("StoryBoard");
                //sb.Begin();

                //tb = c.ContentTemplate.FindName("textbox", c) as TextBox;

                // Color anim

                //ColorAnimation colorAnimation = new ColorAnimation(Colors.Black, Colors.Green, new Duration(TimeSpan.FromSeconds(1)));
                //tb.Foreground = new SolidColorBrush(Colors.Black);                    
                //tb.Foreground.BeginAnimation(SolidColorBrush.ColorProperty, colorAnimation);

                //DoubleAnimation translateAnimation = new DoubleAnimation(0, 360, new Duration(TimeSpan.FromSeconds(1)));
                //var rt = (tb.RenderTransform as TranslateTransform);
                //rt.BeginAnimation(TranslateTransform.XProperty, translateAnimation);


                // Rotate anim 
                /*DoubleAnimation rotateAnimation = new DoubleAnimation(0, 360, new Duration(TimeSpan.FromSeconds(1)));
                var rt = (tb.RenderTransform as RotateTransform);
                rt = new RotateTransform(0);
                rt.BeginAnimation(RotateTransform.AngleProperty, rotateAnimation);*/



                //(tb.RenderTransform as RotateTransform).BeginAnimation(RotateTransform.AngleProperty, rotateAnimation);


                // Shake anim 
                //DoubleAnimation shakeAnimation = new DoubleAnimation(0, 30, new Duration(TimeSpan.FromSeconds(1)));
                //var rt = (tb.RenderTransform as RotateTransform);
                //rt = new RotateTransform();
                //rt.BeginAnimation(RotateTransform.AngleProperty, shakeAnimation);*/



                //var sb = new Storyboard();
                //var animation1 = new DoubleAnimation(200, -10, new Duration(new TimeSpan(0, 0, 0, 1, 0)));
                //Storyboard.SetTargetName(animation1, "translate");
                //Storyboard.SetTargetProperty(animation1, new PropertyPath(TranslateTransform.XProperty));
                //sb.Children.Add(animation1);

                //var animation2 = new DoubleAnimation(100, 0, new Duration(new TimeSpan(0, 0, 0, 1, 0)));
                //Storyboard.SetTargetName(animation2, "translate");
                //Storyboard.SetTargetProperty(animation2, new PropertyPath(TranslateTransform.YProperty));
                //sb.Children.Add(animation2);

                //sb.Begin();*/




                //DoubleAnimation da = new DoubleAnimation(1, 0, new Duration(TimeSpan.FromSeconds(1)));
                //tb = c.ContentTemplate.FindName("textbox", c) as TextBox;
                //tb.BeginAnimation(OpacityProperty, da);
                //var x = tb.Text;

                // });

            }
            
            

            if (e.Valid)
            {
                

                //ShowCanvasMessage($"+ {e.Points}");
                //Players.First(p => p.User.ID == SessionInformations.User.ID).Score = e.PointsTotal;
                Application.Current.Dispatcher.Invoke(() =>
                {
                  //  SoundService.PlayWordGuessedRight();
                });
            }
            else
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    //SoundService.PlayWordGuessedWrong();
                });
            }
        }

    }
}