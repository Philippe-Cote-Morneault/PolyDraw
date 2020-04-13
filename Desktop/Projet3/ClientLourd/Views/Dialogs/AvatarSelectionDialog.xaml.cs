using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.IO;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Timers;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using ClientLourd.Annotations;
using ClientLourd.Utilities.Commands;
using GongSolutions.Wpf.DragDrop.Utilities;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Views.Dialogs
{
    public partial class AvatarSelectionDialog : UserControl, INotifyPropertyChanged
    {
        public AvatarSelectionDialog()
        {
            _avatars = new ObservableCollection<BitmapImage>();
            InitializeComponent();
            GetAllAvatars();
        }

        private ObservableCollection<BitmapImage> _avatars;

        public ObservableCollection<BitmapImage> Avatars
        {
            get { return _avatars; }
            set
            {
                if (value != _avatars)
                {
                    _avatars = value;
                    OnPropertyChanged();
                }
            }
        }

        private void GetAllAvatars()
        {
            for (int i = 1; i <= 16; i++)
            {
                Avatars.Add(new BitmapImage(new Uri($"/ClientLourd;component/Resources/Avatar/{i}.jpg",
                    UriKind.Relative)));
            }

            OnPropertyChanged(nameof(Avatars));
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
                _selectedButton.Background = (Brush) Application.Current.Resources["PrimaryHueLightBrush"];
            }
        }

        private void ButtonBase_OnClick(object sender, RoutedEventArgs e)
        {
            SelectedButton = ((Button) sender);
        }

        RelayCommand<object> _quitCommand;

        public ICommand QuitCommand
        {
            get
            {
                return _quitCommand ??
                       (_quitCommand = new RelayCommand<object>(channel => Quit(), o => SelectedButton != null));
            }
        }

        private void Quit()
        {
            Card card = (Card) SelectedButton.Content;
            Image image = (Image) card.Content;
            DialogHost.CloseDialogCommand.Execute(image.Source, this);
        }

        public event PropertyChangedEventHandler PropertyChanged;

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}