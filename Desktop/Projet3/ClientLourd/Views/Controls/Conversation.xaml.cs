using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media.Animation;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    public partial class Conversation : UserControl
    {
        public Conversation()
        {
            InitializeComponent();
        }

        private void UIElement_OnPreviewMouseWheel(object sender, MouseWheelEventArgs e)
        {
            if (!e.Handled)
            {
                e.Handled = true;
                var eventArg = new MouseWheelEventArgs(e.MouseDevice, e.Timestamp, e.Delta);
                eventArg.RoutedEvent = MouseWheelEvent;
                eventArg.Source = sender;
                var parent = ((Control) sender).Parent as UIElement;
                parent?.RaiseEvent(eventArg);
            }
        }

        private void ScrollViewer_OnScrollChanged(object sender, ScrollChangedEventArgs e)
        {
            ScrollViewer scroll = sender as ScrollViewer;
            if (scroll == null)
            {
                throw new InvalidOperationException(
                    "The attached AlwaysScrollToEnd property can only be applied to ScrollViewer instances.");
            }
            if (e.ExtentHeightChange == 0 && scroll.VerticalOffset == 0)
            {
                ((ChatViewModel) DataContext).LoadHistoryCommand.Execute(10);
            }
            else if (e.ExtentHeightChange > 0)
            {
                scroll.ScrollToVerticalOffset(scroll.ViewportHeight);
            }
 
        }
    }
}