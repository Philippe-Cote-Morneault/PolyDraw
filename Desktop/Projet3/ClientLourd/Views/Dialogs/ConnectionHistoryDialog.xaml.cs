using ClientLourd.Utilities.Commands;
using MaterialDesignThemes.Wpf;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
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
using System.Timers;
using ClientLourd.Models.Bindable;
using ClientLourd.ViewModels;
using ClientLourd.Services.RestService;

namespace ClientLourd.Views.Dialogs
{
    /// <summary>
    /// Interaction logic for ConnectionHistoryDialog.xaml
    /// </summary>
    public partial class ConnectionHistoryDialog : UserControl, INotifyPropertyChanged
    {
        private Timer _scrollToBottomTimer;
        private StatsHistory _statsHistory;
        private int _lastMessageIndex;

        public ConnectionHistoryDialog(StatsHistory statsHistory, int lastMessageIndex)
        {

            (((MainWindow)Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = true;

            StatsHistory = statsHistory;
            _lastMessageIndex = lastMessageIndex;

            InitializeComponent();
            _scrollToBottomTimer = new Timer(800);
            _scrollToBottomTimer.Elapsed += ScrollToBottom;
            _scrollToBottomTimer.Start();
        }

        public RestClient RestClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        public void ScrollToBottom(object sender, ElapsedEventArgs e)
        {
            _scrollToBottomTimer.Stop();
            Application.Current.Dispatcher.InvokeAsync(() =>
            {
                ScrollViewerElement.ScrollToBottom();
            });
            
        }


        public StatsHistory StatsHistory
        {
            get { return _statsHistory; }
            set
            {
                if (value != _statsHistory)
                {
                    _statsHistory = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private async void ScrollViewer_OnScrollChanged(object sender, ScrollChangedEventArgs e)
        {
            ScrollViewer scroll = sender as ScrollViewer;
            if (scroll == null)
            {
                throw new InvalidOperationException(
                    "The attached AlwaysScrollToEnd property can only be applied to ScrollViewer instances.");
            }
            if (e.ExtentHeightChange == 0 && scroll.VerticalOffset == 0)
            {
                //TODO: Dont load messages if messageIndex is greater than the array length
                
                StatsHistory sh = await RestClient.GetStats(_lastMessageIndex, _lastMessageIndex + 20);
                _lastMessageIndex += 20;
                AddStatsHistory(sh);
                scroll.ScrollToVerticalOffset(scroll.ScrollableHeight / 10);
            }

        }



        private void AddStatsHistory(StatsHistory sh)
        {
            foreach (ConnectionDisconnection connectionDisconnection in sh.ConnectionHistory)
            {
                StatsHistory.ConnectionHistory.AddFirst(connectionDisconnection);
            }

            StatsHistory.ConnectionHistory = new LinkedList<ConnectionDisconnection>(StatsHistory.ConnectionHistory);
        }


        private void ScrollViewer_PreviewMouseWheel(object sender, MouseWheelEventArgs e)
        {
            ScrollViewer scrollviewer = sender as ScrollViewer;
            
            
                if (e.Delta > 0)    
                    scrollviewer.PageUp();
                else
                    scrollviewer.PageDown();
                e.Handled = true;
            
        }



        public event PropertyChangedEventHandler PropertyChanged;

        protected void NotifyPropertyChanged([CallerMemberName] String propertyName = "")
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }





    public class Employee
    {
        public string Name { get; set; }

        public int Age { get; set; }

        public string Mail { get; set; }
    }
}