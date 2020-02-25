using System;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Models.Bindable;
using ClientLourd.Models.NonBindable;
using ClientLourd.Services.CredentialsService;
using ClientLourd.Services.RestService;
using ClientLourd.Services.SocketService;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.ValidationRules;
using ClientLourd.Views.Dialogs;
using MaterialDesignThemes.Wpf;
using ClientLourd.Utilities.Constants;

namespace ClientLourd.ViewModels
{
    public class LoginViewModel : ViewModelBase
    {
        public LoginViewModel()
        {
            AfterLogOut();
        }


        public override void AfterLogin()
        {
            IsLoggedIn = true;
        }

        public override void AfterLogOut()
        {
            IsLoggedIn = false;
            User = new User();
            Tokens = new TokenPair();
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
            get { return _isLoggedIn; }

            set
            {
                if (value != _isLoggedIn)
                {
                    _isLoggedIn = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private User _user;

        public User User
        {
            get { return _user; }

            set
            {
                if (value != _user)
                {
                    _user = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private TokenPair _tokens;

        public TokenPair Tokens
        {
            get { return _tokens; }

            set
            {
                if (value != _tokens)
                {
                    _tokens = value;
                    NotifyPropertyChanged();
                }
            }
        }

        private RelayCommand<object> _signUpCommand;

        public ICommand SignUpCommand
        {
            get
            {
                return _signUpCommand ?? (_signUpCommand =
                           new RelayCommand<object>(param => SignUp()));
            }
        }

        private async void SignUp(User user = null)
        {
            if (user == null)
            {
                user = new User();
            }
            var dialog = new RegisterDialog(user);
            var result = await DialogHost.Show(dialog);
            if (bool.Parse(result.ToString()))
            {
                try
                {
                    dynamic data = await RestClient.Register(user, dialog.PasswordField1.Password);
                    StartLogin(user.Username, data, false);
                }
                catch(Exception e)
                {
                    await DialogHost.Show(new ClosableErrorDialog(e), "Default");
                    IsLoggedIn = false;
                    SignUp(user);
                }
            }

        }

        public ICommand LoginCommand
        {
            get
            {
                return _loginCommand ?? (_loginCommand =
                           new RelayCommand<object[]>(param => Authentify(param), param => CredentialsValid(param)));
            }
        }

        private async Task StartLogin(string username, dynamic data, bool rememberMeIsActive)
        {
            Tokens = new TokenPair()
            {
                SessionToken = data["SessionToken"],
                Bearer = data["Bearer"],
            };
            User = new User(username, data["UserID"]);
            await SocketClient.InitializeConnection(Tokens.SessionToken);
            if (rememberMeIsActive)
            {
                CredentialManager.WriteCredential(ApplicationInformations.Name, username, Tokens.Bearer);
            }
            else
            {
                CredentialManager.WriteCredential(ApplicationInformations.Name, "", "");
            }
            OnLogin(this);
        }

        async Task Authentify(object[] param)
        {
            try
            {
                string username = (string) param[0];
                bool rememberMeIsActive = (bool) param[2];
                string password = (param[1] as PasswordBox).Password;
                bool shouldUseBearer = (bool) param[3];
                dynamic data;
                if (shouldUseBearer)
                {
                    data = await RestClient.Bearer(username, Tokens.Bearer);
                }
                else
                {
                    data = await RestClient.Login(username, password);
                }
                await StartLogin(username, data, rememberMeIsActive);
            }
            catch (Exception e)
            {
                await DialogHost.Show(new ClosableErrorDialog(e));
                IsLoggedIn = false;
            }
        }

        bool CredentialsValid(object[] param)
        {
            if (param == null)
            {
                return false;
            }

            string username = (string) param[0];
            string password = (param[1] as PasswordBox).Password;

            LoginInputRules loginInputValidator = new LoginInputRules();

            return (loginInputValidator.UsernameLengthIsOk(username) &&
                    loginInputValidator.PasswordLengthIsOk(password) &&
                    !loginInputValidator.StringIsWhiteSpace(username) &&
                    !loginInputValidator.StringIsWhiteSpace(password));
        }

        public delegate void LoginEventHandler(object source, EventArgs args);

        public event LoginEventHandler LoggedIn;


        protected virtual void OnLogin(object source)
        {
            LoggedIn?.Invoke(source, EventArgs.Empty);
        }
    }
}