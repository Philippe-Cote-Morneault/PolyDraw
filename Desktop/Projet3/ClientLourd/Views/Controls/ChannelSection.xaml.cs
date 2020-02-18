using System;
using System.Windows;
using System.Windows.Controls;
using ClientLourd.Models.Bindable;
using ClientLourd.ViewModels;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;

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
            ((ChatViewModel) DataContext).JoinChannelCommand.Execute(((MenuItem) sender).Tag);
        }

        private async void LeaveChannelClick(object sender, RoutedEventArgs e)
        {

            Channel channel = (Channel)((MenuItem) sender).Tag;
            var result = await DialogHost.Show(new ConfirmationDialog("Warning", $"Are you sure you want to leave {channel.Name}"));
            if (bool.Parse(result.ToString()))
            {
                ((ChatViewModel) DataContext).LeaveChannelCommand.Execute(channel);
            }
        }

        private async void MainTree_OnSelectedItemChanged(object sender, RoutedPropertyChangedEventArgs<object> e)
        {
            //TODO check if the channel is available or joined
            try
            {
                var tree = (TreeView) sender;
                var channel = (Channel) tree.SelectedItem;
                if (channel != null)
                {
                    if (AvailableTree.Items.Contains(channel))
                    {
                        var result = await DialogHost.Show(new ConfirmationDialog("Warning", $"You have to join the {channel.Name} first !"));
                        if (bool.Parse(result.ToString()))
                        {
                            ((ChatViewModel) DataContext).JoinChannelCommand.Execute(channel);
                        }
                    }
                    else
                    {
                        ((ChatViewModel) DataContext).ChangeChannelCommand.Execute(channel);
                    }
                }
            }
            catch
            {
                //The treeViewItem is not a channel 
            }
        }

        private async void DeleteChannelClick(object sender, RoutedEventArgs e)
        {
            Channel channel = (Channel)((MenuItem) sender).Tag;
            var result = await DialogHost.Show(new ConfirmationDialog("Warning", $"Are you sure you want to delete {channel.Name}"));
            if (bool.Parse(result.ToString()))
            {
                ((ChatViewModel) DataContext).DeleteChannelCommand.Execute(channel);
            }
        }
    }
}