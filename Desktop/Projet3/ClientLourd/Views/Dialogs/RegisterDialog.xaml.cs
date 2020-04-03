using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Text.RegularExpressions;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using ClientLourd.Annotations;
using ClientLourd.Models.Bindable;
using ClientLourd.Utilities.Commands;
using ClientLourd.ViewModels;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Views.Dialogs
{
    public partial class RegisterDialog : UserControl, INotifyPropertyChanged
    {
        public User User { get; set; }
        public RegisterDialog(User user)
        {
            User = user;
            User.PictureID = 1;
            InitializeComponent();
        }

        public ResourceDictionary CurrentDictionary
        {
            get => (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.CurrentDictionary;
        }
        
        public bool AreFieldsEmpty
        {
            get
            {
                if (String.IsNullOrWhiteSpace(PasswordField1.Password))
                    return true;
                if (String.IsNullOrWhiteSpace(PasswordField2.Password))
                    return true;
                if(String.IsNullOrWhiteSpace(FirstNameField.Text))
                    return true;
                if(String.IsNullOrWhiteSpace(LastNameField.Text))
                    return true;
                if(String.IsNullOrWhiteSpace(EmailField.Text))
                    return true;
                if(String.IsNullOrWhiteSpace(UsernameField.Text))
                    return true;
                return false;
                
            }
        }


        public event PropertyChangedEventHandler PropertyChanged;

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private void OnFieldChanged(object sender, RoutedEventArgs e)
        {
            OnPropertyChanged(nameof(AreFieldsEmpty));
        }

        RelayCommand<Channel> _changeAvatarCommand;

        public ICommand ChangeAvatarCommand
        {
            get
            {
                return _changeAvatarCommand ??
                       (_changeAvatarCommand = new RelayCommand<Channel>(channel => ChangeAvatar()));
            }
        }

        private async void ChangeAvatar()
        {
            var result = await DialogHost.Show(new AvatarSelectionDialog(), "RegisterDialogHost");
            User.Avatar = (BitmapImage) result;
        }

        private async void Register(object sender, RoutedEventArgs e)
        {
            if (PasswordField1.Password != PasswordField2.Password)
            {
                await DialogHost.Show(new ClosableErrorDialog((string) CurrentDictionary["InvalidPassword"]), "RegisterDialogHost");
            }
            else if (PasswordField1.Password.Length < 8)
            {
                await DialogHost.Show(new ClosableErrorDialog((string) CurrentDictionary["InvalidLenghtPassword"]), "RegisterDialogHost");
            }
            else
            {
                DialogHost.CloseDialogCommand.Execute(true, this);
            }
        }
    }
}