using System.Windows;
using System.Windows.Controls;
using ClientLourd.ViewModels;
using MaterialDesignThemes.Wpf.Transitions;
using Microsoft.Win32;

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
    }
}