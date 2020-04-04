using System;
using System.ComponentModel;
using System.Windows;
using ClientLourd.ViewModels;
using ClientLourd.Views.Controls;

namespace ClientLourd.Views.Windows
{
    /// <summary>
    /// Interaction logic for SimpleWindow.xaml
    /// </summary>
    public partial class ChatWindow : System.Windows.Window
    {
        Chat _chatBox;

        public ResourceDictionary CurrentDictionary
        {
            get => (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.CurrentDictionary;
        }

        public MainViewModel MainViewModel
        {
            get => (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel);
        }


        public ChatWindow(Chat ChatBox)
        {
            _chatBox = ChatBox;
            InitializeComponent();
            MainPanel.Children.Add(ChatBox);
            Resources.MergedDictionaries.Add(CurrentDictionary);
            MainViewModel.LanguageChangedEvent += OnLangChanged;
            Closing += OnWindowClosing;
        }

        private void OnLangChanged(object source, EventArgs args)
        {
            Resources.MergedDictionaries[0] = CurrentDictionary;
        }

        public void OnWindowClosing(object sender, CancelEventArgs e)
        {
            MainPanel.Children.Clear();
            ((MainWindow) Owner).ReturnTheChat();
        }
    }
}