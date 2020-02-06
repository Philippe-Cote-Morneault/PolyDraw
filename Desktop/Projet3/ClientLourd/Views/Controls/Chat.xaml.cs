using System;
using System.Windows;
using System.Windows.Controls;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    public partial class Chat : UserControl
    {
        public Chat()
        {
            InitializeComponent();
            DataContext = new ChatViewModel();
            (DataContext as ChatViewModel).ChatOpen += OnChatOpen;

        }

        private void OnChatOpen(object source, EventArgs args)
        {
            MessageTextBox.Focus();
        }


        public void Init()
        {
            ((ChatViewModel) DataContext).Init();
            MessageTextBox.Clear();
        }
    }
}