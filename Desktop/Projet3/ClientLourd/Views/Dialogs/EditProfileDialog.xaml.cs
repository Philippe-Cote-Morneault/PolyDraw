using System;
using System.ComponentModel;
using System.Dynamic;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.RestService;
using ClientLourd.Utilities.Commands;
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

        PrivateProfileInfo _pvInfo;
        PrivateProfileInfo _pvInfoClone;
        string _passwordJunk;

        public EditProfileDialog(PrivateProfileInfo pvInfo)
        {
            PasswordJunk = JUNK;
            NewPassword = JUNK;

            // Info before modif
            PrivateProfileInfo = pvInfo;

            // Info after modif
            PrivateProfileInfoClone = new PrivateProfileInfo(pvInfo);

            InitializeComponent();
            DataContext = this;

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
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        private RelayCommand<object> _editProfileCommand;

        public ICommand EditProfileCommand
        {
            get { return _editProfileCommand ?? (_editProfileCommand = new RelayCommand<object>(obj => EditProfile(obj), obj => CanUpdateProfile(obj))); }
        }

        private bool CanUpdateProfile(object obj)
        {
            return HasUpdatedProfile() && new LoginInputRules().PasswordLengthIsOk(NewPassword);
        }

        private bool HasUpdatedProfile()
        {
            return (PrivateProfileInfo != PrivateProfileInfoClone || NewPassword != PasswordJunk);
        }

        private async Task EditProfile(object obj)
        {
            try
            {
                await RestClient.PutProfile(GetModifiedObj());
                // Update infos
                PrivateProfileInfo.Username = PrivateProfileInfoClone.Username;
                PrivateProfileInfo.FirstName = PrivateProfileInfoClone.FirstName;
                PrivateProfileInfo.LastName = PrivateProfileInfoClone.LastName;
                PrivateProfileInfo.Email = PrivateProfileInfoClone.Email;
                PrivateProfileInfo = PrivateProfileInfoClone;

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
                obj.Username = PrivateProfileInfoClone.Username;
            }

            if (EmailHasChanged())
            {
                obj.Email = PrivateProfileInfoClone.Email;
            }

            if (LastNameHasChanged())
            {
                obj.LastName = PrivateProfileInfoClone.LastName;
            }

            if (FirstNameHasChanged())
            {
                obj.FirstName = PrivateProfileInfoClone.FirstName;
            }

            if (PasswordHasChanged())
            {
                obj.Password = NewPassword;
            }            

            return obj;
        }



        private RelayCommand<string> _revertToOriginalCommand;

        public ICommand RevertToOriginalCommand
        {
            get { return _revertToOriginalCommand ?? (_revertToOriginalCommand = new RelayCommand<string>(obj => RevertToOriginalField(obj))); }
        }

        private void RevertToOriginalField(string fieldType)
        {
            switch (fieldType)
            {
                case "Username":
                    PrivateProfileInfoClone.Username = PrivateProfileInfo.Username;
                    break;
                case "Email":
                    PrivateProfileInfoClone.Email = PrivateProfileInfo.Email;
                    break;
                case "FirstName":
                    PrivateProfileInfoClone.FirstName = PrivateProfileInfo.FirstName;
                    break;
                case "LastName":
                    PrivateProfileInfoClone.LastName = PrivateProfileInfo.LastName;
                    break;
                case "Password":
                    (PasswordField as PasswordBox).Password = PasswordJunk;
                    break;
                default:
                    throw new Exception("Input field " + fieldType + " does not exist");

            }            
        }

        public PrivateProfileInfo PrivateProfileInfo
        {
            get { return _pvInfo; }
            set
            {
                if (value != _pvInfo)
                {
                    _pvInfo = value;
                    NotifyPropertyChanged();
                }
                    
                
            }
        }


        public PrivateProfileInfo PrivateProfileInfoClone
        {
            get { return _pvInfoClone; }
            set
            {
                if (value != _pvInfoClone) 
                { 
                    _pvInfoClone = value;
                    NotifyPropertyChanged();
                }

            }
        }

        private bool UsernameHasChanged()
        {
            return PrivateProfileInfo.Username != PrivateProfileInfoClone.Username;
        }

        private bool EmailHasChanged()
        {
            return PrivateProfileInfo.Email != PrivateProfileInfoClone.Email;
        }

        private bool LastNameHasChanged()
        {
            return PrivateProfileInfo.LastName != PrivateProfileInfoClone.LastName;
        }

        private bool FirstNameHasChanged()
        {
            return PrivateProfileInfo.FirstName != PrivateProfileInfoClone.FirstName;
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

    }
}
