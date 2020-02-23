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

namespace ClientLourd.Views.Controls
{
    
    



    /// <summary>
    /// Interaction logic for Editor.xaml
    /// </summary>
    public partial class Editor : UserControl
    {
        DateTime _strokeStart;
        private Cursor _pointEraser = CursorHelper.FromByteArray(Properties.Resources.PointEraser);

        // Stroke Custom Attributes
        public static readonly Guid time = new Guid("12345678-9012-3456-7890-123456789012");
        public static readonly Guid brushSize = new Guid("12345678-9012-3456-7890-123456789333");
        public static readonly Guid brushType = new Guid("12345678-9012-3456-7890-123456789444");
        public static readonly Guid brushColor = new Guid("12345678-9012-3456-7890-123456789555");
        public static readonly Guid eraser = new Guid("12345678-9012-3456-7890-123456789666");
        
        public Editor()
        {
            InitializeComponent();
            DataContext = new EditorViewModel();
            Loaded += OnLoad;
        }

        private void OnLoad(object sender, RoutedEventArgs e)
        {
            // Bubble inkCanvas event so we can capture it
            surfaceDessin.AddHandler(InkCanvas.MouseDownEvent, new MouseButtonEventHandler(StartTimer), true);
        }

        private Button _selectedColor;

        // Pour la gToolsList_OnSelectionChangedition du pointeur.
        private void surfaceDessin_MouseLeave(object sender, MouseEventArgs e) => textBlockPosition.Text = "";

        public void StartTimer(object sender, MouseEventArgs e)
        {
            _strokeStart = DateTime.Now;
        }

        public void Test(object sender, InkCanvasStrokeCollectedEventArgs e)
        {
            DateTime strokeEnd = DateTime.Now;
            var editorVM = DataContext as EditorViewModel;
            double millisecondsTakenToDraw = (strokeEnd - _strokeStart).TotalMilliseconds;
            if (millisecondsTakenToDraw > 5000)
                millisecondsTakenToDraw = 5000;
                
            e.Stroke.AddPropertyData(time, millisecondsTakenToDraw);
            e.Stroke.AddPropertyData(brushSize, editorVM.TailleTrait);
            e.Stroke.AddPropertyData(brushType, editorVM.PointeSelectionnee);
            e.Stroke.AddPropertyData(eraser, (editorVM.OutilSelectionne == "efface_segment").ToString());
        }

        public void Test2(object sender, InkCanvasStrokeErasingEventArgs e)
        {
            if (e.Stroke.GetPropertyData(eraser) as string == "True")
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