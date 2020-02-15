using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Runtime.CompilerServices;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using ClientLourd.Annotations;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Views.Dialogs
{
    public partial class AvatarSelectionDialog : UserControl, INotifyPropertyChanged
    {
        public AvatarSelectionDialog()
        {
            InitializeComponent();
            GetAllAvatars();
            SelectedButton = DefaultButton;

        }

        private void GetAllAvatars()
        {
            /*
            for (int i = 1; i <= 16; i++)
            {
                Avatars.Add(new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/{i}.jpg", UriKind.Relative)));
            }
            OnPropertyChanged(nameof(Avatars));
            */
            //Beau code
            image1.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/1.jpg", UriKind.Relative));
            image2.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/2.jpg", UriKind.Relative));
            image3.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/3.jpg", UriKind.Relative));
            image4.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/4.jpg", UriKind.Relative));
            image5.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/5.jpg", UriKind.Relative));
            image6.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/6.jpg", UriKind.Relative));
            image7.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/7.jpg", UriKind.Relative));
            image8.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/8.jpg", UriKind.Relative));
            image9.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/9.jpg", UriKind.Relative));
            image10.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/10.jpg", UriKind.Relative));
            image11.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/11.jpg", UriKind.Relative));
            image12.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/12.jpg", UriKind.Relative));
            image13.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/13.jpg", UriKind.Relative));
            image14.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/14.jpg", UriKind.Relative));
            image15.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/15.jpg", UriKind.Relative));
            image16.Source = new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/16.jpg", UriKind.Relative));
        }

        private Button _selectedButton;

        public Button SelectedButton
        {
            get { return _selectedButton; }
            set
            {
                if (SelectedButton != null)
                {
                    SelectedButton.Background = Brushes.Transparent;
                }

                _selectedButton = value;
                _selectedButton.Background =(Brush)Application.Current.Resources["PrimaryHueLightBrush"];
            }
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            SelectedButton = ((Button) sender);
        }

        private void QuitClick(object sender, RoutedEventArgs e)
        {
            Card card = (Card) SelectedButton.Content;
            Image image = (Image) card.Content;
            DialogHost.CloseDialogCommand.Execute(image.Source,this);
        }

        public event PropertyChangedEventHandler PropertyChanged;

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}