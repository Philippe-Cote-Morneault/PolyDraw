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
using ClientLourd.Models.Bindable;
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
            Loaded += OnLoad;
        }

        private EditorViewModel ViewModel
        {
            get => DataContext as EditorViewModel;
        }


        private void OnLoad(object sender, RoutedEventArgs e)
        {
            // Bubble inkCanvas event so we can capture it
            Canvas.AddHandler(InkCanvas.MouseDownEvent, new MouseButtonEventHandler(SaveDrawDebutTime), true);
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
            double millisecondsTakenToDraw = (strokeEnd - _strokeStart).TotalMilliseconds;
            if (millisecondsTakenToDraw > 5000)
                millisecondsTakenToDraw = 5000;

            stroke.AddPropertyData(GUIDs.time, millisecondsTakenToDraw);
            stroke.AddPropertyData(GUIDs.brushSize, ViewModel.EditorInformation.BrushSize.ToString());
            stroke.AddPropertyData(GUIDs.brushType, ViewModel.EditorInformation.SelectedTip.ToString());
            stroke.AddPropertyData(GUIDs.eraser, (ViewModel.EditorInformation.SelectedTool == InkCanvasEditingMode.EraseByPoint).ToString());
            stroke.AddPropertyData(GUIDs.color, ColorToNumber(stroke.DrawingAttributes.Color.ToString()).ToString());
        }

        public void OnStrokeErase(object sender, InkCanvasStrokeErasingEventArgs e)
        {
            // Dont erase white eraser strokes
            if (e.Stroke.GetPropertyData(GUIDs.eraser) as string == "True")
            {
                e.Cancel = true;
            }
        }


        private void surfaceDessin_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(Canvas);
            textBlockPosition.Text = Math.Round(p.X) + ", " + Math.Round(p.Y) + "px";
        }


        private void ToolsList_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
                if(Canvas != null)
                {
                    var tag = (ToolsList.SelectedItem as ListBoxItem)?.Tag;
                    if (tag != null)
                    {
                        InkCanvasEditingMode tool = tag is InkCanvasEditingMode ? (InkCanvasEditingMode) tag : InkCanvasEditingMode.None;

                        if (ViewModel != null)
                        {
                            if (tool == InkCanvasEditingMode.EraseByPoint)
                            {
                                Canvas.UseCustomCursor = true;
                                Canvas.Cursor = _pointEraser;
                                ViewModel.EditorInformation.SelectedColor = Colors.White;
                                ViewModel?.ChangeToolCommand.Execute(InkCanvasEditingMode.Ink);
                                ViewModel.EditorInformation.IsAnEraser = true;
                                return;
                        }
                            else
                            {
                                ViewModel.EditorInformation.IsAnEraser = false;
                                Canvas.UseCustomCursor = false;
                                if (_selectedColor != null)
                                {
                                    Color c = (Color)((Ellipse) _selectedColor.Content).Tag;
                                    ViewModel.EditorInformation.SelectedColor = c;
                                }
                                else
                                {
                                    ViewModel.EditorInformation.SelectedColor = Colors.Black;
                                }
                            }
                        }
                        ViewModel?.ChangeToolCommand.Execute(tool);
                    }
                }
        }

        private void TipsList_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            var tag = (TipsList.SelectedItem as ListBoxItem)?.Tag;
            if (tag != null)
            {
                var tip = tag is StylusTip ? (StylusTip) tag : StylusTip.Rectangle;
                ViewModel?.ChangeTipCommand.Execute(tip);
            }
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            if (ViewModel.EditorInformation.IsAnEraser)
            {
                return;
            }

            if (_selectedColor != null)
            {
                _selectedColor.Background = Brushes.Transparent;
            }
            var button = (Button) sender;
            _selectedColor = button;
            _selectedColor.Background = Brushes.Gray;
            Color c = (Color)((Ellipse) _selectedColor.Content).Tag;
            ViewModel?.ChangeColorCommand.Execute(c);
        }

        private int ColorToNumber(string colorHex)
        {
            if (Colors.Black.ToString() == colorHex) return 0;

            if (Colors.White.ToString() == colorHex) return 1;

            if (Colors.Red.ToString() == colorHex) return 2;

            if (Colors.Green.ToString() == colorHex) return 3;

            if (Colors.Blue.ToString() == colorHex) return 4;

            if (Colors.Yellow.ToString() == colorHex) return 5;

            if (Colors.Cyan.ToString() == colorHex) return 6;

            if (Colors.Magenta.ToString() == colorHex) return 7;

            return -1;
        }
    }
}