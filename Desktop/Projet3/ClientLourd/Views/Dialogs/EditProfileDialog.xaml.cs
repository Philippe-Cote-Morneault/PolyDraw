using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Dynamic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.RestService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.ValidationRules;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Dialogs
{
    /// <summary>
    /// Interaction logic for EditProfileDialog.xaml
    /// </summary>
    public partial class EditProfileDialog : UserControl, INotifyPropertyChanged
    {
        PrivateProfileInfo _pvInfo;
        PrivateProfileInfo _pvInfoClone;
        string _passwordJunk;

        public EditProfileDialog(PrivateProfileInfo pvInfo)
        {

            InitializeComponent();
            DataContext = this;

            // Password junk
            PasswordJunk = "$#%@!&*)";


            // Info before modif
            PrivateProfileInfo = new PrivateProfileInfo(pvInfo);

            // Info after modif
            PrivateProfileInfoClone = new PrivateProfileInfo(pvInfo);

            (PasswordField as PasswordBox).Password = PasswordJunk;
            
        }

        public string PasswordJunk
        {
            get { return _passwordJunk; }
            set
            {
                if (value != _passwordJunk)
                {
                    _passwordJunk = value;
                    NotifyPropertyChanged();
                }
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
            return HasUpdatedProfile() && new LoginInputRules().PasswordLengthIsOk(PasswordField.Password);
        }

        private bool HasUpdatedProfile()
        {
            return (PrivateProfileInfo != PrivateProfileInfoClone || PasswordField.Password != PasswordJunk);
        }

        private async Task EditProfile(object obj)
        {
            //TODO POST here
            try
            {
                string isOk = await RestClient.PutProfile(GetModifiedObj());
            }
            catch(Exception e)
            {
                MessageBox.Show(e.Message);
            }

            //(((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).ContainedView = Enums.Views.Editor.ToString();
        }

        private object GetModifiedObj()
        {
            dynamic obj = new ExpandoObject();
            obj.Username = "pipicaca1";
            obj.Password = "Passsssssssss";

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
                
                    _pvInfo = value;
                    NotifyPropertyChanged();
                
            }
        }


        public PrivateProfileInfo PrivateProfileInfoClone
        {
            get { return _pvInfoClone; }
            set
            {

                _pvInfoClone = value;
                NotifyPropertyChanged();

            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void NotifyPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

    }
}
