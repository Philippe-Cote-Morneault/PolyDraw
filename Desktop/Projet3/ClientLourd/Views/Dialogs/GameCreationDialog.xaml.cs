using System.Windows;
using System.Windows.Controls;
using ClientLourd.ViewModels;
using Microsoft.Win32;

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
    }
}