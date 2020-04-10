using ClientLourd.Models.Bindable;
using ClientLourd.Services.RestService;
using ClientLourd.ViewModels;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Profile.xaml
    /// </summary>
    public partial class Profile : UserControl
    {
        private int _lastMessageIndex;

        public Profile()
        {
            InitializeComponent();

        }

        public void UpdateStats()
        {
            ViewModel.GetAllStats();
            
            _lastMessageIndex = (DataContext as ProfileViewModel)._end;

            Task.Delay(600).ContinueWith((t) =>
            {
                Application.Current.Dispatcher.Invoke(() =>
                {
                    ScrollViewer sv = FindVisualChild<ScrollViewer>(gamesHistoryListView);

                    // Get scrollviewer
                    if (sv != null)
                    {
                        sv.ScrollToBottom();
                    }
                });
            });
                
        }

        public RestClient RestClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        private async void ScrollViewer_OnScrollChanged(object sender, ScrollChangedEventArgs e)
        {
            ScrollViewer scroll = FindVisualChild<ScrollViewer>(gamesHistoryListView);
            if (scroll == null)
            {
                throw new InvalidOperationException(
                    "The attached AlwaysScrollToEnd property can only be applied to ScrollViewer instances.");
            }

            if (e.ExtentHeightChange == 0 && scroll.VerticalOffset == 0)
            {
                StatsHistory sh = await RestClient.GetStats(_lastMessageIndex, _lastMessageIndex + 20);

                if (sh.MatchesPlayedHistory != null && sh.MatchesPlayedHistory.Count > 0)
                {
                    _lastMessageIndex += 21;
                    (DataContext as ProfileViewModel).AddStatsHistory(sh);
                    scroll.ScrollToVerticalOffset(scroll.ScrollableHeight / 10);
                }
            }
        }

        private childItem FindVisualChild<childItem>(DependencyObject obj)
               where childItem : DependencyObject

        {
            for (int i = 0; i < VisualTreeHelper.GetChildrenCount(obj); i++)
            {
                DependencyObject child = VisualTreeHelper.GetChild(obj, i);

                if (child != null && child is childItem)
                    return (childItem)child;
                else
                {
                    childItem childOfChild = FindVisualChild<childItem>(child);

                    if (childOfChild != null)
                        return childOfChild;
                }
            }

            return null;
        }



        public void AfterLogin()
        {
            ViewModel.AfterLogin();
        }

        public void AfterLogout()
        {
            ViewModel.AfterLogOut();
        }
    }
}