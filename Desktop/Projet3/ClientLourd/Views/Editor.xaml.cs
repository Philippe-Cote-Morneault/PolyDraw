using System;
using System.Collections.Generic;
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
using ClientLourd.ModelViews;
using System.Windows.Controls.Primitives;


namespace ClientLourd.Views
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

        private void GlisserCommence(object sender, DragStartedEventArgs e) =>
            (sender as Thumb).Background = Brushes.Black;

        private void GlisserTermine(object sender, DragCompletedEventArgs e) =>
            (sender as Thumb).Background = Brushes.Red;

        private void GlisserMouvementRecu(object sender, DragDeltaEventArgs e)
        {
            String nom = (sender as Thumb).Name;
            if (nom == "horizontal" || nom == "diagonal")
                colonne.Width = new GridLength(Math.Max(32, colonne.Width.Value + e.HorizontalChange));
            if (nom == "vertical" || nom == "diagonal")
                ligne.Height = new GridLength(Math.Max(32, ligne.Height.Value + e.VerticalChange));
        }

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
    }
}