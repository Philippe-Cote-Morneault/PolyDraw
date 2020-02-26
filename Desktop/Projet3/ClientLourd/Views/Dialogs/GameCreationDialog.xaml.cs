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
using System.Text;
using ClientLourd.Services.SocketService;
using ClientLourd.Models.NonBindable;
using ClientLourd.Utilities.Enums;
using ClientLourd.Models.Bindable;
using System.Windows.Input;
using ClientLourd.Utilities.Extensions;

namespace ClientLourd.Views.Dialogs
{
    public partial class GameCreationDialog : UserControl
    {
        public GameCreationDialog()
        {
            InitializeComponent();
            SocketClient.DrawingPreviewResponse += SocketClientOnDrawingPreviewResponse;
            SocketClient.ServerStartsDrawing += SocketClientOnServerStartsDrawing;
            SocketClient.ServerEndsDrawing += SocketClientOnServerEndsDrawing;
        }

        public SocketClient SocketClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }

        public SessionInformations SessionInformations
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.SessionInformations; }
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
            // Bug here: if we use the erase by point eraser, there will be white strokes in the canvas.
            if (EditorView.Canvas.Strokes.Count > 0)
            {
                CreateSVGFile(GenerateXMLDoc());
            }
            
            ViewModel.UploadImageCommand.Execute(null);

        }

        /// <summary>
        /// Creates an SVG file from an XMLDocument in AppDomain.CurrentDomain.BaseDirectory (ClientLourd\Bin\Debug)
        /// TODO: Check if this causes a problem while running from exe
        /// </summary>
        private void CreateSVGFile(XmlDocument xmlDoc)
        {
            
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Encoding = new UTF8Encoding(false); // The false means, do not emit the BOM.
            ViewModel.DrawnImagePath = $"{Path.GetTempFileName()}.svg";
            using (XmlWriter w = XmlWriter.Create(ViewModel.DrawnImagePath, settings))
            {
                xmlDoc.Save(w);
            }
        }

        private XmlDocument GenerateXMLDoc()
        {
            var svg = new SvgDocument();
            
            // Add polydram namespace
            svg.CustomAttributes.Add("xmlns:polydraw", "http://example.org/polydraw");

            var colorServer = new SvgColourServer(System.Drawing.Color.Black);
            var group = new SvgGroup { Fill = colorServer, Stroke = colorServer };
            
            svg.Children.Add(group);

            for (int i = 0; i < EditorView.Canvas.Strokes.Count; i++)
            {
                var stroke = EditorView.Canvas.Strokes[i];
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

        // TODO: Delete this
        private StrokeInfo GetMockStrokeMessage() 
        {
            byte[] firstByte = new byte[] { 135 };
            byte[] strokeUid = Encoding.ASCII.GetBytes("12345678-9012-34");
            byte[] userUid = Encoding.ASCII.GetBytes("12345678-9012-34");
            byte[] twoBytesPadding = new byte[2];
            byte[] oneBytePadding = new byte[1] {11};
            byte[] points = new byte[200];
            for (byte i = 0; i < 200; i +=2)
            {
                points[i] = 0;
                points[i + 1] = i;
            }

            var mockData = new byte[firstByte.Length + strokeUid.Length + userUid.Length + twoBytesPadding.Length + twoBytesPadding.Length + oneBytePadding.Length + points.Length];

            firstByte.CopyTo(mockData, 0);
            strokeUid.CopyTo(mockData, firstByte.Length);
            userUid.CopyTo(mockData, strokeUid.Length + firstByte.Length);
            twoBytesPadding.CopyTo(mockData, firstByte.Length + strokeUid.Length + userUid.Length);
            twoBytesPadding.CopyTo(mockData, firstByte.Length + strokeUid.Length + userUid.Length + twoBytesPadding.Length);
            oneBytePadding.CopyTo(mockData, firstByte.Length + strokeUid.Length + userUid.Length + twoBytesPadding.Length + twoBytesPadding.Length);
            points.CopyTo(mockData, firstByte.Length + strokeUid.Length + userUid.Length + twoBytesPadding.Length + twoBytesPadding.Length + oneBytePadding.Length);

            return new StrokeInfo(mockData);
        }

        public void PlayPreview(object sender, EventArgs e)
        {

            StrokeInfo mock = GetMockStrokeMessage();
            //StrokeInfo mock2;
            PreviewCanvas.AddStroke(mock);
            PreviewCanvas.AddStroke(mock);
            //SocketClient.SendMessage(new Tlv(SocketMessageTypes.DrawingPreviewRequest, SessionInformations.User.ID));   
        }

        private void SocketClientOnDrawingPreviewResponse(object source, EventArgs args)
        {
            // If 0x00
            // Dialog with error
        }

        private void SocketClientOnServerStartsDrawing(object source, EventArgs args)
        {
            ViewModel.PreviewGUIEnabled = false;
            
        }

        private void SocketClientOnServerEndsDrawing(object source, EventArgs args)
        {
            ViewModel.PreviewGUIEnabled = true;
        }

        private void AddPoints()
        {
            if (PreviewCanvas.Strokes.Count == 0)
            {
                StylusPointCollection spCol = new StylusPointCollection();
                for (int i = 0; i < 100; i++)
                {
                    spCol.Add(new StylusPoint(i, i));
                    spCol.Add(new StylusPoint(i + 1, i + 1));
                }
                Stroke newStroke = new Stroke(spCol);
                PreviewCanvas.Strokes.Add(newStroke);
            }
            else
            {
                
            }
            
            //PreviewCanvas.Str
        }


    }
}