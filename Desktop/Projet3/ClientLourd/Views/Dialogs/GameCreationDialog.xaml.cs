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
using ClientLourd.Services.RestService;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Views.Dialogs
{
    public partial class GameCreationDialog : UserControl
    {
        public GameCreationDialog()
        {
            InitializeComponent();
            Loaded += (sender, args) => { ViewModel.CurrentCanvas = PreviewCanvas; };
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

        private void UploadImageClick(object sender, RoutedEventArgs e)
        {
            // Bug here: if we use the erase by point eraser, there will be white strokes in the canvas.
            if (EditorView.Canvas.Strokes.Count > 0)
            {
                try
                {
                    CreateSVGFile(EditorView.GenerateXMLDoc());
                }
                catch(Exception ex)
                {
                    DialogHost.Show(new ClosableErrorDialog(ex), "Dialog");
                }
            }
            ViewModel.UploadImageCommand.Execute(null);
        }

        private void CreateSVGFile(XmlDocument xmlDoc)
        {
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Encoding = new UTF8Encoding(false); // The false means do not emit the BOM.
            ViewModel.DrawnImagePath = $"{Path.GetTempFileName()}.svg";
            using (XmlWriter w = XmlWriter.Create(ViewModel.DrawnImagePath, settings))
            {
                xmlDoc.Save(w);
            }
        }

        public void ClearPreviewCanvas(object sender,EventArgs arg)
        {
            PreviewCanvas.Strokes.Clear();
        }

    }
}