using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.ValidationRules;

namespace ClientLourd.ModelViews
{
    public class LoginViewModel : ViewModelBase
    {
        public LoginViewModel()
        {
            _isLoggedIn = false;
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
            IsLoggedIn = true;
        }

        bool CredentialsValid(object[] param)
        {
            
            string username = (string)param[0];
            string password = (param[1] as PasswordBox).Password;

            LoginInputRules loginInputValidator = new LoginInputRules();

            return (loginInputValidator.LengthIsOk(username) && loginInputValidator.LengthIsOk(password) &&
                    !loginInputValidator.StringIsEmpty(username) && !loginInputValidator.StringIsEmpty(password));
        }
    }
}