using System;
using System.Windows;
using System.Windows.Controls;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Login.xaml
    /// </summary>
    public partial class Login : UserControl
    {
        public Login()
        {
            InitializeComponent();
            Loaded += Load;
        }

        public void AfterLogout()
        {
            PasswordBox.Clear();
            ((LoginViewModel) DataContext).AfterLogOut();

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
            ((ViewModelBase)DataContext).AfterLogin();
        }
    }
}