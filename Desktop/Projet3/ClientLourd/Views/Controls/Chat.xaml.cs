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
    }
}