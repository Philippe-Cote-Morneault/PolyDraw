using System;
using System.Linq;
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
        }


        public static readonly DependencyProperty IsWaitingProperty =
            DependencyProperty.Register("IsWaiting", typeof(Boolean), typeof(Chat), new PropertyMetadata(false));


        public bool IsWaiting
        {
            get { return (bool) GetValue(IsWaitingProperty); }
            set { SetValue(IsWaitingProperty, value); }
        }

        public void OnChatToggle(bool isOpen)
        {
            ((ChatViewModel) DataContext).OnChatToggle(isOpen);
            if (isOpen)
            {
                MessageTextBox.Focus();
            }
        }


        public void AfterLogout()
        {
            ((ChatViewModel) DataContext).AfterLogOut();
            MessageTextBox.Clear();
        }

        public void AfterLogin()
        {
            ((ChatViewModel) DataContext).AfterLogin();
        }

        public void OnFocusLost(object sender, EventArgs e)
        {
            (DataContext as ChatViewModel).ChatFocused = false;
        }

        public void OnFocus(object sender, EventArgs e)
        {
            (DataContext as ChatViewModel).ChatFocused = true;

        }
    }
}