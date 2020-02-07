using System;
using System.Windows.Controls;

namespace ClientLourd.Views.Dialogs
{
    public partial class ClosableErrorDialog : UserControl
    {
        public ClosableErrorDialog(Exception e)
        {
            InitializeComponent();
            DockPanel.Children.Add(new ErrorDialog(e));
        }

        public ClosableErrorDialog(string message)
        {
            InitializeComponent();
            DockPanel.Children.Add(new ErrorDialog(message));
        }
    }
}