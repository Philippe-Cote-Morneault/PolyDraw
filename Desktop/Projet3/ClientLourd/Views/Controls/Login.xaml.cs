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
        private string _oldUsername;
        private bool _isBearerActive;
        private bool _passwordChanged;
        private bool _usernameChanged;

        public bool IsBearerActive
        {
            get => _isBearerActive && !_passwordChanged && !_usernameChanged;
        }

        public Login()
        {
            InitializeComponent();
            AfterLogout();
            Loaded += Load;
        }

        public void AfterLogout()
        {
            _isBearerActive = false;
            PasswordBox.Clear();
            ((LoginViewModel) DataContext).AfterLogOut();
            var cred = CredentialManager.ReadCredential(ApplicationInformations.Name);
            if (cred != null)
            {
                NameTextBox.Text = cred.UserName;
                _oldUsername = cred.UserName;
                ((LoginViewModel) DataContext).Tokens.Bearer = cred.Password;
                PasswordBox.Password = "MyJunkPassword";
                _isBearerActive = true;
                RememberMeCheckBox.IsChecked = true;
            }

            _passwordChanged = false;
            _usernameChanged = false;
            OnPropertyChanged(nameof(IsBearerActive));
        }

        public void Load(object sender, RoutedEventArgs e)
        {
            NameTextBox.Focus();
            object[] param = new object[4];
            param[0] = NameTextBox.Text;
            param[1] = PasswordBox;
            param[2] = RememberMeCheckBox.IsChecked;
            param[3] = Control.IsBearerActive;
            if (ViewModel.LoginCommand.CanExecute(param))
            {
                ViewModel.LoginCommand.Execute(param);
            }
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
            _passwordChanged = true;
            OnPropertyChanged(nameof(IsBearerActive));
        }

        public event PropertyChangedEventHandler PropertyChanged;

        [NotifyPropertyChangedInvocator]
        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

        private void NameTextBox_OnTextChanged(object sender, TextChangedEventArgs e)
        {
            _usernameChanged = NameTextBox.Text != _oldUsername;
            OnPropertyChanged(nameof(IsBearerActive));
        }
    }
}