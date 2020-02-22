using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Shapes;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Editor.xaml
    /// </summary>
    public partial class Editor : UserControl
    {
        public Editor()
        {
            InitializeComponent();
            DataContext = new EditorViewModel();
        }

        private Button _selectedColor;

        // Pour la gToolsList_OnSelectionChangedition du pointeur.
        private void surfaceDessin_MouseLeave(object sender, MouseEventArgs e) => textBlockPosition.Text = "";

        private void surfaceDessin_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(surfaceDessin);
            textBlockPosition.Text = Math.Round(p.X) + ", " + Math.Round(p.Y) + "px";
        }


        private void ToolsList_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            string tool = (ToolsList.SelectedItem as ListBoxItem)?.Tag as string;
            (DataContext as EditorViewModel)?.ChoisirOutil.Execute(tool);
        }

        private void TipsList_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            string tip = (TipsList.SelectedItem as ListBoxItem)?.Tag as string;
            (DataContext as EditorViewModel)?.ChoisirPointe.Execute(tip);
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            if (_selectedColor != null)
            {
                _selectedColor.Background = Brushes.Transparent;
            }
            var button = (Button) sender;
            _selectedColor = button;
            _selectedColor.Background = Brushes.Gray;
            ((EditorViewModel) DataContext).CouleurSelectionnee = ((Ellipse) _selectedColor.Content).Fill.ToString();
        }
    }
}