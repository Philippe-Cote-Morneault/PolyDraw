using System.Windows;
using System.Windows.Controls;
using ClientLourd.Models.Bindable;
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

        private void MainTree_OnSelectedItemChanged(object sender, RoutedPropertyChangedEventArgs<object> e)
        {
            //TODO check if the channel is available or joined
            var tree = (TreeView) sender;
            var channel = (Channel)tree.SelectedItem;
            if (channel != null)
            {
                ((ChatViewModel)DataContext).ChangeChannelCommand.Execute(channel);
            }
        }
    }
}