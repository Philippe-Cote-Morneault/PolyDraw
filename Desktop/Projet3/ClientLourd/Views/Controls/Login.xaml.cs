using System;
using System.Windows;
using System.Windows.Controls;
using ClientLourd.Services.CredentialsService;
using ClientLourd.Utilities.Constants;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Login.xaml
    /// </summary>
    public partial class Login : UserControl
    {
        public bool IsBearerActive { get; set; }
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
    }
}