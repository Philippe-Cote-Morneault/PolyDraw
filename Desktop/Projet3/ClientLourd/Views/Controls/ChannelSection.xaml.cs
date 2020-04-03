using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
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
            var result = await DialogHost.Show(new ConfirmationDialog($"{CurrentDictionary["Warning"]}", $"{CurrentDictionary["WarningLeaveChannel"]} {channel.Name}?"));
            if (bool.Parse(result.ToString()))
            {
                ((ChatViewModel) DataContext).LeaveChannelCommand.Execute(channel);
            }
        }
        public ResourceDictionary CurrentDictionary
        {
            get => (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.CurrentDictionary;
        }
        private async void MainTree_OnMouseUp(object sender, MouseButtonEventArgs e)
        {
            try
            {
                var tree = (TreeView) sender;
                var channel = (Channel) tree.SelectedItem;
                if (channel != null)
                {
                    if (AvailableTree.Items.Contains(channel))
                    {
                        var result = await DialogHost.Show(new ConfirmationDialog($"{CurrentDictionary["Warning"]}", $"{CurrentDictionary["JoinChannelWarning1"]} {channel.Name} {CurrentDictionary["JoinChannelWarning2"]}"));
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
            var result = await DialogHost.Show(new ConfirmationDialog($"{CurrentDictionary["Warning"]}", $"{CurrentDictionary["WarningDeleteChannel"]} {channel.Name}?"));
            if (bool.Parse(result.ToString()))
            {
                ((ChatViewModel) DataContext).DeleteChannelCommand.Execute(channel);
            }
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            
            Grid grid = (Grid)((Button) sender).Tag;
            grid.ContextMenu.PlacementTarget = sender as UIElement;
            grid.ContextMenu.IsOpen = true;
        }


    }
}