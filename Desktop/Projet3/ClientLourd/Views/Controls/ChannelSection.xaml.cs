using System.Windows;
using System.Windows.Controls;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    public partial class ChannelSection : UserControl
    {
        public ChannelSection()
        {
            InitializeComponent();
        }

        private void JoinChannelClick(object sender, RoutedEventArgs e)
        {
            ((ChatViewModel)DataContext).JoinChannelCommand.Execute(((MenuItem)sender).Tag);
        }
        private void LeaveChannelClick(object sender, RoutedEventArgs e)
        {
            ((ChatViewModel)DataContext).LeaveChannelCommand.Execute(((MenuItem)sender).Tag);
        }
    }
}