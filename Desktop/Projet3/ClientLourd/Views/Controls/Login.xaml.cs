using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows;
using System.Windows.Controls;
using ClientLourd.Annotations;
using ClientLourd.Services.CredentialsService;
using ClientLourd.Utilities.Constants;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Login.xaml
    /// </summary>
    public partial class Login : UserControl, INotifyPropertyChanged
    {

        private bool _isBearerActive;

        public bool IsBearerActive
        {
            get => _isBearerActive;
            set
            {
                if (_isBearerActive != value)
                {
                    _isBearerActive = value;
                    OnPropertyChanged();
                }
            }
        }
        public Login()
        {
            InitializeComponent();
            AfterLogout();
            Loaded += Load;
                
        }

        public void AfterLogout()
        {
            PasswordBox.Clear();
            ((LoginViewModel) DataContext).AfterLogOut();
            var cred = CredentialManager.ReadCredential(ApplicationInformations.Name);
            if (cred != null)
            {
                NameTextBox.Text = cred.UserName;
                ((LoginViewModel) DataContext).Tokens.Bearer = cred.Password;
                PasswordBox.Password = "MyJunkPassword";
                IsBearerActive = true;
                RememberMeCheckBox.IsChecked = true;
            }
        }

        public void Load(object sender, RoutedEventArgs e)
        {
            NameTextBox.Focus();
        }

        public static readonly DependencyProperty IsWaitingProperty =
            DependencyProperty.Register("IsWaiting", typeof(Boolean), typeof(Login), new PropertyMetadata(false));


        public bool IsWaiting
        {
            get { return (bool) GetValue(IsWaitingProperty); }
            set { SetValue(IsWaitingProperty, value); }
        }

        public void AfterLogin()
        {
            ((ViewModelBase) DataContext).AfterLogin();
        }

        private void PasswordBox_OnPasswordChanged(object sender, RoutedEventArgs e)
        {
            IsBearerActive = false;
        }

        public event PropertyChangedEventHandler PropertyChanged;

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}