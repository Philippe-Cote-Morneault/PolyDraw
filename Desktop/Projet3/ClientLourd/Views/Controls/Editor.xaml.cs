using System;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;
using System.Windows.Input;
using System.Windows.Markup;
using System.Windows.Media;
using System.Xml;
using System.Xml.Linq;
using System.Windows.Shapes;
using ClientLourd.ViewModels;
using Svg;
using System.Text;
using System.Windows.Ink;
using ClientLourd.Utilities.Extensions;
using ClientLourd.Utilities.Constants;

namespace ClientLourd.Views.Controls
{
    
    



    /// <summary>
    /// Interaction logic for Editor.xaml
    /// </summary>
    public partial class Editor : UserControl
    {
        DateTime _strokeStart;
        private Cursor _pointEraser = CursorHelper.FromByteArray(Properties.Resources.PointEraser);

        public Editor()
        {
            InitializeComponent();
            DataContext = new EditorViewModel();
            Loaded += OnLoad;
        }

        private void OnLoad(object sender, RoutedEventArgs e)
        {
            // Bubble inkCanvas event so we can capture it
            surfaceDessin.AddHandler(InkCanvas.MouseDownEvent, new MouseButtonEventHandler(SaveDrawDebutTime), true);
        }

        private Button _selectedColor;

        // Pour la gToolsList_OnSelectionChangedition du pointeur.
        private void surfaceDessin_MouseLeave(object sender, MouseEventArgs e) => textBlockPosition.Text = "";

        public void SaveDrawDebutTime(object sender, MouseEventArgs e)
        {
            _strokeStart = DateTime.Now;
        }

        public void OnStrokeAdded(object sender, InkCanvasStrokeCollectedEventArgs e)
        {
            AddAttributesToStroke(e.Stroke);
        }

        private void AddAttributesToStroke(Stroke stroke)
        {
            DateTime strokeEnd = DateTime.Now;
            var editorVM = DataContext as EditorViewModel;
            double millisecondsTakenToDraw = (strokeEnd - _strokeStart).TotalMilliseconds;
            if (millisecondsTakenToDraw > 5000)
                millisecondsTakenToDraw = 5000;

            stroke.AddPropertyData(GUIDs.time, millisecondsTakenToDraw);
            stroke.AddPropertyData(GUIDs.brushSize, editorVM.TailleTrait);
            stroke.AddPropertyData(GUIDs.brushType, editorVM.PointeSelectionnee);
            stroke.AddPropertyData(GUIDs.eraser, (editorVM.OutilSelectionne == "efface_segment").ToString());
            // TODO: Add color property
        }

        private int ColorToNumber()
        {
            // TODO
            return 0;
        }

        public void OnStrokeErase(object sender, InkCanvasStrokeErasingEventArgs e)
        {
            if (e.Stroke.GetPropertyData(GUIDs.eraser) as string == "True")
            {
                e.Cancel = true;
            }
        }


        private void surfaceDessin_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(surfaceDessin);
            textBlockPosition.Text = Math.Round(p.X) + ", " + Math.Round(p.Y) + "px";
        }


        private void ToolsList_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            string tool = (ToolsList.SelectedItem as ListBoxItem)?.Tag as string;
            

            if ((DataContext as EditorViewModel) != null)
            {
                if (tool == "efface_segment")
                {
                    surfaceDessin.UseCustomCursor = true;
                    surfaceDessin.Cursor = _pointEraser;
                    (DataContext as EditorViewModel).CouleurSelectionnee = "#FFFFFFFF";

                }
                else
                {
                    surfaceDessin.UseCustomCursor = false;
                    ((EditorViewModel)DataContext).CouleurSelectionnee = (_selectedColor != null) ? ((Ellipse)_selectedColor.Content).Fill.ToString(): "#FF000000";
                }
            }
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