using System;
using System.Collections.Generic;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Markup;
using System.Xml;
using System.Xml.Linq;
using ClientLourd.ViewModels;
using Microsoft.Win32;
using Svg;

namespace ClientLourd.Views.Dialogs
{
    public partial class GameCreationDialog : UserControl
    {
        public GameCreationDialog()
        {
            InitializeComponent();
        }

        private void UploadImageClick(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            //TODO update the filter
            openFileDialog.Filter =  "Image files (*.jpg, *.jpeg, *.jpe, *.jfif, *.png *.bmp) | *.jpg; *.jpeg; *.jpe; *.jfif; *.png; *.bmp";
            if (openFileDialog.ShowDialog() == true)
            { 
                ((GameCreationViewModel) DataContext).UploadImageCommand.Execute(openFileDialog.FileName);
            }
        }

        private void DropUpload(object sender, DragEventArgs e)
        {
              if (e.Data.GetDataPresent(DataFormats.FileDrop))
              {
                  string[] files = (string[])e.Data.GetData(DataFormats.FileDrop);
                  if (files != null) ((GameCreationViewModel) DataContext).UploadImageCommand.Execute(files[0]);
              }
        }

        public void GenerateSVG(object sender, EventArgs e)
        {
            var svg = new SvgDocument();
            var colorServer = new SvgColourServer(System.Drawing.Color.Black);
            var group = new SvgGroup { Fill = colorServer, Stroke = colorServer };
            svg.Children.Add(group);

            foreach (var stroke in EditorView.surfaceDessin.Strokes)
            {
                var geometry = stroke.GetGeometry(stroke.DrawingAttributes).GetOutlinedPath‌​Geometry();

                var s = XamlWriter.Save(geometry);

                if (!String.IsNullOrEmpty(s))
                {
                    var element = XElement.Parse(s);

                    var data = element.Attribute("Figures")?.Value;

                    if (!String.IsNullOrEmpty(data))
                    {
                        var svgPath = new SvgPath 
                        {
                            PathData = SvgPathBuilder.Parse(data),
                            Fill = new SvgColourServer(System.Drawing.Color.Blue),
                            Stroke = new SvgColourServer(System.Drawing.Color.Blue),
                            ID = Guid.NewGuid().ToString()
                        };
                        svgPath.CustomAttributes.Add("dd", "dd");
                        group.Children.Add(svgPath);
                    }
                }
            }


            var memoryStream = new MemoryStream();
            svg.Write(memoryStream);

            memoryStream.Seek(0, SeekOrigin.Begin);

            var xmlDocument = new XmlDocument();
            xmlDocument.Load(memoryStream);
            var x = 1;
        }
    }
}