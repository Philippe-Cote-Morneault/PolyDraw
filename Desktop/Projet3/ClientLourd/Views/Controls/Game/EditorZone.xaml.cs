using System;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
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

            
            if (e.Valid)
            {
                Task.Run(() =>
                {
                    for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
                    {
                        ContentPresenter c = (ContentPresenter)GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                        TextBox tb;

                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            tb = c.ContentTemplate.FindName("textbox", c) as TextBox;
                            Storyboard sb = (Storyboard)FindResource("GuessRight");

                            for (int j = 0; j < sb.Children.Count; j++)
                            {
                                Storyboard.SetTarget(sb.Children[j], tb);
                            }


                            sb.Begin();
                        });
                        System.Threading.Thread.Sleep(200);
                    }
                });
            }
            else
            {
                Task.Run(() =>
                {
                    for (int i = 0; i < GuessTextBoxes.Items.Count; i++)
                    {
                        ContentPresenter c = (ContentPresenter)GuessTextBoxes.ItemContainerGenerator.ContainerFromIndex(i);
                        TextBox tb;

                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            tb = c.ContentTemplate.FindName("textbox", c) as TextBox;
                            Storyboard sb = (Storyboard)FindResource("GuessWrong");

                            for (int j = 0; j < sb.Children.Count; j++)
                            {
                                Storyboard.SetTarget(sb.Children[j], tb);
                            }

                            sb.Begin();
                        });
                    }
                });
            }
        }

    }
}