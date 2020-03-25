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
using ClientLourd.Services.ServerStrokeDrawerService;

namespace ClientLourd.Views.Dialogs
{
    public partial class GameCreationDialog : UserControl
    {
        public GameCreationDialog()
        {
            InitializeComponent();
            Loaded += (sender, args) => {
                ViewModel.AddSocketListeners();
                ViewModel.CurrentCanvas = PreviewCanvas;
                ViewModel.StrokeDrawerService = new ServerStrokeDrawerService(PreviewCanvas, true);
                ViewModel.StrokeDrawerService.PreviewDrawingDoneEvent += ViewModel.OnPreviewDrawingDone;
            };
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
            if(ViewModel.IsUploadModeSelected && !ViewModel.IsImageUpload)
            {
                DialogHost.Show(new ClosableErrorDialog("Upload an image first"), "Dialog");
                return;
            }
            
            if (!ViewModel.IsUploadModeSelected)
            {
                if (CanvasIsEmpty())
                {
                    DialogHost.Show(new ClosableErrorDialog("The canvas cannot be empty"), "Dialog");
                    return;
                }
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

        private bool CanvasIsEmpty() 
        {
            foreach(Stroke stroke in EditorView.Canvas.Strokes)
            {
                if ((stroke.GetPropertyData(GUIDs.eraser) as string) == "False")
                {
                    return false;
                }
            }

            return true;
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
        
        
        public void OnClose(object sender, EventArgs arg)
        {
            if (ViewModel.CancelGame())
            {
                // Make sure its stopped although it should already be.
                ViewModel.StrokeDrawerService.Close();
                ViewModel.RemoveSocketListeners();
                DialogHost.CloseDialogCommand.Execute(null, null);
            }
        }

        private void UIElement_OnKeyDown(object sender, KeyEventArgs e)
        {
            if (e.Key == Key.Enter)
            {
                var tb = (TextBox) sender;
                var request = new TraversalRequest(FocusNavigationDirection.Next);
                request.Wrapped = true;
                tb.MoveFocus(request);
            }
        }
        /// <summary>
        /// Clear the canvas after the first next
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ClearCanvasClick(object sender, RoutedEventArgs e)
        {
            EditorView.Canvas.Strokes.Clear();
        }
        /// <summary>
        /// Call when the previous button is use
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void CancelCurrentGame(object sender, RoutedEventArgs e)
        {
            ViewModel.CancelGame();
        }
    }
}