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
            this.Loaded += Load;
        }

        public void Init()
        {
            PasswordBox.Clear();
            ((LoginViewModel) DataContext).Init();
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
    }
}