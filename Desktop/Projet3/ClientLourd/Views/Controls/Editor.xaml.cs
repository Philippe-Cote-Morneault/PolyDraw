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
using ClientLourd.ViewModels;
using Svg;

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

        public void Test(object sender, InkCanvasStrokeCollectedEventArgs e)
        {            

            var svg = new SvgDocument();
            var colorServer = new SvgColourServer(System.Drawing.Color.Black);

            var group = new SvgGroup { Fill = colorServer, Stroke = colorServer };
            svg.Children.Add(group);

            var geometry = e.Stroke.GetGeometry(e.Stroke.DrawingAttributes).GetOutlinedPathGeometry();
            string s = XamlWriter.Save(geometry);
            if (!String.IsNullOrEmpty(s))
            {
                var element = XElement.Parse(s);
                var data = element.Attribute("Figures")?.Value;
                if (!String.IsNullOrEmpty(data))
                {
                    group.Children.Add(new SvgPath
                    {
                        PathData = SvgPathBuilder.Parse(data),
                        Fill = colorServer,
                        Stroke = colorServer,
                        ID = "1",
                    }) ;
                }

                var memoryStream = new MemoryStream();
                svg.Write(memoryStream);

                memoryStream.Seek(0, SeekOrigin.Begin);

                var xmlDocument = new XmlDocument();
                xmlDocument.Load(memoryStream);
                var x = 1;
                //SvgVisualElement sview = new SvgVisualElement();
            }

            /*foreach (var stroke in InkCanvas.Strokes)
            {
                var geometry = stroke.GetGeometry(stroke.DrawingAttributes).GetOutlinedPath‌​Geometry();

                var s = XamlWriter.Save(geometry);

                if (s.IsNotNullOrEmpty())
                {
                    var element = XElement.Parse(s);

                    var data = element.Attribute("Figures")?.Value;

                    if (data.IsNotNullOrEmpty())
                    {
                        group.Children.Add(new SvgPath
                        {
                            PathData = SvgPathBuilder.Parse(data),
                            Fill = colorServer,
                            Stroke = colorServer
                        });
                    }
                }
            }*/


        }

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