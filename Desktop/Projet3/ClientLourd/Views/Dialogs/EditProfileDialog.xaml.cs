using System;
using System.ComponentModel;
using System.Dynamic;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media.Imaging;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.CredentialsService;
using ClientLourd.Services.RestService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Constants;
using ClientLourd.Utilities.ValidationRules;
using ClientLourd.ViewModels;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Views.Dialogs
{
    /// <summary>
    /// Interaction logic for EditProfileDialog.xaml
    /// </summary>
    public partial class EditProfileDialog : UserControl, INotifyPropertyChanged
    {
        private const string JUNK = "$#%@!&*)";

        User _userClone;
        string _passwordJunk;

        public EditProfileDialog()
        {
            PasswordJunk = JUNK;
            NewPassword = JUNK;


            // Info after modif
            UserClone = new User(User);

            InitializeComponent();

            // Password junk
            (PasswordField as PasswordBox).Password = PasswordJunk;
        }

        public string PasswordJunk
        {
            get { return _passwordJunk; }
            set
            {
                _passwordJunk = value;
                NotifyPropertyChanged();
            }
        }


        public RestClient RestClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        private RelayCommand<object> _editProfileCommand;

        public ICommand EditProfileCommand
        {
            get
            {
                return _editProfileCommand ?? (_editProfileCommand =
                    new RelayCommand<object>(obj => EditProfile(obj), obj => CanUpdateProfile(obj)));
            }
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
            var result = await DialogHost.Show(new AvatarSelectionDialog(), "EditProfileHost");
            UserClone.Avatar = (BitmapImage) result;
        }


        private bool CanUpdateProfile(object obj)
        {
            return HasUpdatedProfile() && new LoginInputRules().PasswordLengthIsOk(NewPassword);
        }

        private bool HasUpdatedProfile()
        {
            return (User != UserClone || NewPassword != PasswordJunk);
        }

        private async Task EditProfile(object obj)
        {
            try
            {
                await RestClient.PutProfile(GetModifiedObj());
                
                // Update infos
                User.Username = UserClone.Username;
                User.Avatar = UserClone.Avatar;
                User.FirstName = UserClone.FirstName;
                User.LastName = UserClone.LastName;
                User.Email = UserClone.Email;
                User = UserClone;
                // Update de credentials store
                var cred = CredentialManager.ReadCredential(ApplicationInformations.Name);
                if (cred != null)
                {
                    CredentialManager.WriteCredential(ApplicationInformations.Name, User.Username,cred.Password);
                }

                DialogHost.CloseDialogCommand.Execute(null, null);
            }
            catch (Exception e)
            {
                await EditProfileHost.ShowDialog(new ClosableErrorDialog(e));
            }
        }

        /// <summary>
        /// Returns an object with the modified profile parameters only
        /// </summary>
        /// <returns></returns>
        private object GetModifiedObj()
        {
            dynamic obj = new ExpandoObject();

            if (UsernameHasChanged())
            {
                obj.Username = UserClone.Username;
            }

            if (EmailHasChanged())
            {
                obj.Email = UserClone.Email;
            }

            if (LastNameHasChanged())
            {
                obj.LastName = UserClone.LastName;
            }

            if (FirstNameHasChanged())
            {
                obj.FirstName = UserClone.FirstName;
            }

            if (PasswordHasChanged())
            {
                obj.Password = NewPassword;
            }

            if (AvatarHasChanged())
            {
                obj.PictureID = UserClone.PictureID;
            }

            return obj;
        }


        private RelayCommand<string> _revertToOriginalCommand;

        public ICommand RevertToOriginalCommand
        {
            get
            {
                return _revertToOriginalCommand ?? (_revertToOriginalCommand =
                    new RelayCommand<string>(obj => RevertToOriginalField(obj)));
            }
        }

        private void RevertToOriginalField(string fieldType)
        {
            switch (fieldType)
            {
                case "Username":
                    UserClone.Username = User.Username;
                    break;
                case "Email":
                    UserClone.Email = User.Email;
                    break;
                case "FirstName":
                    UserClone.FirstName = User.FirstName;
                    break;
                case "LastName":
                    UserClone.LastName = User.LastName;
                    break;
                case "Password":
                    (PasswordField as PasswordBox).Password = PasswordJunk;
                    break;
                default:
                    throw new Exception("Input field " + fieldType + " does not exist");
            }
        }

        public User User
        {
            get
            {
                return (((MainWindow) Application.Current.MainWindow).DataContext as MainViewModel).SessionInformations
                    .User;
            }
            set
            {
                (((MainWindow) Application.Current.MainWindow).DataContext as MainViewModel).SessionInformations.User =
                    value;
                NotifyPropertyChanged();
            }
        }


        public User UserClone
        {
            get { return _userClone; }
            set
            {
                if (value != _userClone)
                {
                    _userClone = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private bool UsernameHasChanged()
        {
            return User.Username != UserClone.Username;
        }

        private bool EmailHasChanged()
        {
            return User.Email != UserClone.Email;
        }

        private bool LastNameHasChanged()
        {
            return User.LastName != UserClone.LastName;
        }

        private bool FirstNameHasChanged()
        {
            return User.FirstName != UserClone.FirstName;
        }

        private bool AvatarHasChanged()
        {
            return User.Avatar != UserClone.Avatar;
        }

        private bool PasswordHasChanged()
        {
            return PasswordJunk != NewPassword;
        }

        private string _newPassword;

        public string NewPassword
        {
            get { return _newPassword; }
            set
            {
                if (value != _newPassword)
                {
                    _newPassword = value;
                    NotifyPropertyChanged();
                }
            }
        }


        private void OnPasswordChanged(object sender, RoutedEventArgs e)
        {
            NewPassword = PasswordField.Password;
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void NotifyPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private void PasswordField_OnGotFocus(object sender, RoutedEventArgs e)
        {
            PasswordField.Password = "";
        }

        private void PasswordField_OnLostFocus(object sender, RoutedEventArgs e)
        {
            if (PasswordField.Password == "")
            {
                PasswordField.Password = JUNK;
            }
        }
    }
}