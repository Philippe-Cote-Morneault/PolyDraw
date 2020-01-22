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
using ClientLourd.Services.Network;

namespace ClientLourd.ModelViews
{
    class MainViewModel: ViewModelBase
    {

        RelayCommand<object[]> _loginCommand;
        bool _isLoggedIn;
        string _username;
        RestClient _restClient;

        public MainViewModel()
        {
            _isLoggedIn = false;
            _username = "";
            _restClient = new RestClient();

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


        public ICommand LoginCommand
        {
            get
            {
                return _loginCommand ?? (_loginCommand = new RelayCommand<object[]>(param => Authentify(param) ,param => CredentialsValid(param)));
            }
        }

        void Authentify(object[] param) {
            IsLoggedIn = true;
            _restClient.Login("yo", "yo");

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
