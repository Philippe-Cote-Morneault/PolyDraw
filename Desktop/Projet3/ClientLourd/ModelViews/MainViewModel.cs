using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Controls;
using System.Windows.Input;
using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.ValidationRules;


namespace ClientLourd.ModelViews
{
    class MainViewModel: ViewModelBase
    {

        RelayCommand<object[]> _loginCommand;
        bool _isLoggedIn;
        string _username;
        string _password;

        public MainViewModel()
        {
            _isLoggedIn = false;
            _username = "";
            _password = "";
        }


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

        public string Username
        {
            get
            {
                return _username;
            }

            set
            {
                if (value != _username)
                {
                    _username = value;
                    NotifyPropertyChanged();
                }
            }
        }

        public string Password
        {
            get
            {
                return _password;
            }

            set
            {
                if (value != _password)
                {
                    _password = value;
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
                    !loginInputValidator.stringIsEmpty(username) && !loginInputValidator.stringIsEmpty(password));
        }

    }

    
}
