using System;
using System.Collections.Generic;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Markup;
using System.Xml;
using System.Xml.Linq;
using ClientLourd.ViewModels;
using MaterialDesignThemes.Wpf.Transitions;
using Microsoft.Win32;
using Svg;
using ClientLourd.Utilities.Constants;
using System.Windows.Ink;

namespace ClientLourd.Views.Dialogs
{
    public partial class GameCreationDialog : UserControl
    {
        public GameCreationDialog()
        {
            InitializeComponent();
        }

        private GameCreationViewModel ViewModel
        {
            get { return (GameCreationViewModel) DataContext; }
        }

        private void AddImageClick(object sender, RoutedEventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            //TODO update the filter
            openFileDialog.Filter =  "Image files (*.jpg, *.jpeg, *.jpe, *.jfif, *.png *.bmp) | *.jpg; *.jpeg; *.jpe; *.jfif; *.png; *.bmp";
            if (openFileDialog.ShowDialog() == true)
            { 
                ViewModel.AddImageCommand.Execute(openFileDialog.FileName);
            }
        }

        private void DropFile(object sender, DragEventArgs e)
        {
              if (e.Data.GetDataPresent(DataFormats.FileDrop))
              {
                  string[] files = (string[])e.Data.GetData(DataFormats.FileDrop);
                  if (files != null) ViewModel.AddImageCommand.Execute(files[0]);
              }
        }

        private void ValidateTheGame(object sender, RoutedEventArgs e)
        {
            ViewModel.ValidateGameCommand.Execute(null);
        }

        private void UploadImageClick(object sender, RoutedEventArgs e)
        {
            

            ViewModel.UploadImageCommand.Execute(null);

        }
        public void GenerateXMLDoc(object sender, EventArgs e)
        {
            var svg = new SvgDocument();
            
            // Add polydram namespace
            svg.CustomAttributes.Add("xmlns:polydraw", "http://polydraw");

            var colorServer = new SvgColourServer(System.Drawing.Color.Black);
            var group = new SvgGroup { Fill = colorServer, Stroke = colorServer };
            
            svg.Children.Add(group);

            for (int i = 0; i < EditorView.surfaceDessin.Strokes.Count; i++)
            {
                var stroke = EditorView.surfaceDessin.Strokes[i];
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

        
    }
}