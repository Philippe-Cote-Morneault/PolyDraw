using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using ClientLourd.Utilities.Commands;

namespace ClientLourd.ModelViews
{
    class MainViewModel: ViewModelBase
    {

        RelayCommand<string> _loginCommand;
        bool _isLoggedIn;

        public MainViewModel()
        {
            _isLoggedIn = false;
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

        
        public ICommand LoginCommand
        {
            get
            {
                return _loginCommand ?? (_loginCommand = new RelayCommand<string>(param => Authentify(param) ,param => UsernameValid(param)));
            }
        }

        void Authentify(string username) {
            IsLoggedIn = true;


        }

        bool UsernameValid(string username)
        {
            return !String.IsNullOrWhiteSpace(username);
        }

    }
}
