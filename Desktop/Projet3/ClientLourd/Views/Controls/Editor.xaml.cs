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
using ClientLourd.Services.SocketService;
using System.Windows.Media.Animation;

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
            Canvas.AddHandler(InkCanvas.MouseLeftButtonDownEvent, new MouseButtonEventHandler(SaveDrawDebutTime), true);
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

            stroke.AddPropertyData(GUIDs.time, (int)millisecondsTakenToDraw);
            stroke.AddPropertyData(GUIDs.brushSize, ViewModel.EditorInformation.BrushSize.ToString());
            stroke.AddPropertyData(GUIDs.brushType, (ViewModel.EditorInformation.SelectedTip.ToString() == "Ellipse") ? "circle": "square");
            stroke.AddPropertyData(GUIDs.eraser, (ViewModel.EditorInformation.SelectedTool == InkCanvasEditingMode.EraseByPoint).ToString());
            stroke.AddPropertyData(GUIDs.color, ColorToNumber(stroke.DrawingAttributes.Color.ToString()).ToString());
            OnStrokeAdded(stroke);
        }

        public delegate void EditEventHandler(object sender, EventArgs args);

        public event EditEventHandler StrokeDeleted;
        public event EditEventHandler StrokedAdded;
        
        protected virtual void OnStrokeDeleted(object sender)
        {
            StrokeDeleted?.Invoke(sender, EventArgs.Empty);
        }
        
        protected virtual void OnStrokeAdded(object sender)
        {
            StrokedAdded?.Invoke(sender, EventArgs.Empty);
        }

        public void OnStrokeErase(object sender, InkCanvasStrokeErasingEventArgs e)
        {
            // Dont erase white eraser strokes
            if (e.Stroke.GetPropertyData(GUIDs.eraser) as string == "True")
            {
                e.Cancel = true;
            }
            else
            {
                OnStrokeDeleted(e.Stroke);
            }
        }


        private void surfaceDessin_MouseMove(object sender, MouseEventArgs e)
        {
            Point p = e.GetPosition(Canvas);
            textBlockPosition.Text = Math.Round(p.X) + ", " + Math.Round(p.Y) + "px";
        }


        public void ToolsList_OnSelectionChanged(object sender, SelectionChangedEventArgs e)
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
                            }
                            else
                            {
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
                            ViewModel.ChangeToolCommand.Execute(tool);
                        }
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
        
        
        public XmlDocument GenerateXMLDoc()
        {
            var svg = new SvgDocument();
            
            // Add polydram namespace
            svg.CustomAttributes.Add("xmlns:polydraw", "http://example.org/polydraw");

            var colorServer = new SvgColourServer(System.Drawing.Color.Black);
            var group = new SvgGroup { Fill = colorServer, Stroke = colorServer };
            
            svg.Children.Add(group);

            for (int i = 0; i < Canvas.Strokes.Count; i++)
            {
                var stroke = Canvas.Strokes[i].Clone();
                stroke.DrawingAttributes.Width = 1;
                stroke.DrawingAttributes.Height = 1;
                var geometry = stroke.GetGeometry(stroke.DrawingAttributes).GetOutlinedPath‌​Geometry();

                var s = XamlWriter.Save(geometry);

                if (!String.IsNullOrEmpty(s))
                {
                    var element = XElement.Parse(s);

                    var data = element.Attribute("Figures")?.Value;

                    if (!String.IsNullOrEmpty(data))
                    {
                        // Remove the close path attribute (z)
                        if (data[data.Length - 1] == 'z')
                        {
                            data = data.Remove(data.Length - 1);
                        }
                        

                        group.Children.Add(GenerateSVGPath(stroke, data, i));
                    }
                }
            }

            var memoryStream = new MemoryStream();
            svg.Write(memoryStream);

            memoryStream.Seek(0, SeekOrigin.Begin);

            var xmlDocument = new XmlDocument();
            xmlDocument.Load(memoryStream);
          
            return xmlDocument;
        }
        
        /// <summary>
        /// Generates a <path/> from a stroke and data (d). 
        /// </summary>
        /// <param name="stroke"></param>
        /// <param name="data"></param>
        /// <param name="order"></param>
        /// <returns></returns>
        private SvgPath GenerateSVGPath(Stroke stroke, string data, int order)
        {
            var svgPath = new SvgPath
            {
                PathData = SvgPathBuilder.Parse(data),
                Fill = new SvgColourServer(System.Drawing.Color.FromArgb(stroke.DrawingAttributes.Color.A, stroke.DrawingAttributes.Color.R, stroke.DrawingAttributes.Color.G, stroke.DrawingAttributes.Color.B)),
                Stroke = new SvgColourServer(System.Drawing.Color.FromArgb(stroke.DrawingAttributes.Color.A, stroke.DrawingAttributes.Color.R, stroke.DrawingAttributes.Color.G, stroke.DrawingAttributes.Color.B)),
                ID = Guid.NewGuid().ToString()
            };


            svgPath.CustomAttributes.Add("polydraw:time", stroke.GetPropertyData(GUIDs.time).ToString());
            svgPath.CustomAttributes.Add("polydraw:order", order.ToString());
            svgPath.CustomAttributes.Add("polydraw:color", stroke.GetPropertyData(GUIDs.color).ToString());
            svgPath.CustomAttributes.Add("polydraw:eraser", stroke.GetPropertyData(GUIDs.eraser).ToString());
            svgPath.CustomAttributes.Add("polydraw:brush", stroke.GetPropertyData(GUIDs.brushType).ToString());
            svgPath.CustomAttributes.Add("polydraw:brushsize", stroke.GetPropertyData(GUIDs.brushSize).ToString());

            return svgPath;
        }


        private void Canvas_OnPreviewMouseMove(object sender, MouseEventArgs e)
        {
            var p = e.GetPosition(Canvas);
            if (e.LeftButton == MouseButtonState.Pressed)
            {
                if (p.X < 0 || p.X > Canvas.Width || p.Y < 0|| p.Y > Canvas.Height)
                {
                    e.Handled = true;
                }
            }
            System.Windows.Controls.Canvas.SetTop(EraserIndicator,p.Y - BrushSizeSlider.Value/2);
            System.Windows.Controls.Canvas.SetLeft(EraserIndicator,p.X - BrushSizeSlider.Value/2);
 
            
        }

        private void Editor_OnMouseWheel(object sender, MouseWheelEventArgs e)
        {
            if (e.Delta > 0 && BrushSizeSlider.Value + 5 < BrushSizeSlider.Maximum)
                BrushSizeSlider.Value += 5;
            else if(e.Delta < 0 && BrushSizeSlider.Value -5 > BrushSizeSlider.Minimum)
                BrushSizeSlider.Value -= 5;
        }
    }
}