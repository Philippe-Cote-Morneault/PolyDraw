using System.Net.Sockets;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Services.Rest;
using ClientLourd.Utilities.Commands;
using ClientLourd.Services.Rest;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Exceptions.Rest;
using ClientLourd.Utilities.ValidationRules;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.ModelViews
{
    public class LoginViewModel : ViewModelBase
    {
        public LoginViewModel()
        {
            Init();
        }

        public override void Init()
        {
            IsLoggedIn = false;
        }
        
        public RestClient RestClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }
        public SocketClient SocketClient
        {
            get { return (((MainWindow) Application.Current.MainWindow)?.DataContext as MainViewModel)?.SocketClient; }
        }
        
        RelayCommand<object[]> _loginCommand;
        bool _isLoggedIn;
        public bool IsLoggedIn
        {
            get
            {
                return _isLoggedIn;
            }

            set
            {
                if (value != _isLoggedIn)
                {
                    _isLoggedIn = value;
                    NotifyPropertyChanged();
                }
            }
        }
        
        public ICommand LoginCommand
        {
            get
            {
                return _loginCommand ?? (_loginCommand = new RelayCommand<object[]>(param => Authentify(param) ,param => CredentialsValid(param)));
            }
        }

        void Authentify(object[] param) {
            string username = (string)param[0];
            string password = (param[1] as PasswordBox).Password;
            try
            {
                string token = RestClient.Login(username, password);
                SocketClient.InitializeConnection(token);
                IsLoggedIn = true;
            }
            catch (RestException e)
            {
                DialogHost.Show(new ClosableErrorDialog(e));
                IsLoggedIn = false;
            }
        }

        bool CredentialsValid(object[] param)
        {
            if (param == null)
            {
                return false;
            }
            string username = (string)param[0];
            string password = (param[1] as PasswordBox).Password;

            LoginInputRules loginInputValidator = new LoginInputRules();

            return (loginInputValidator.LengthIsOk(username) && loginInputValidator.LengthIsOk(password) &&
                    !loginInputValidator.StringIsEmpty(username) && !loginInputValidator.StringIsEmpty(password));
        }
    }
}