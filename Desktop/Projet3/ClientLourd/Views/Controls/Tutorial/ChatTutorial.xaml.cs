using System.Windows;
using System.Windows.Controls;

namespace ClientLourd.Views.Controls.Tutorial
{
    public partial class ChatTutorial : UserControl
    {
        public ChatTutorial()
        {
            InitializeComponent();
        }
        
        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            
            Grid grid = (Grid)((Button) sender).Tag;
            grid.ContextMenu.PlacementTarget = sender as UIElement;
            grid.ContextMenu.IsOpen = true;
        }
        
    }
}