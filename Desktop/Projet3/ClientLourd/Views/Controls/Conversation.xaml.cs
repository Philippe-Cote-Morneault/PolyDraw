using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

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
    }
}