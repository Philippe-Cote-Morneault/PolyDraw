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

        string _username;
        RestClient _restClient;

        public MainViewModel()
        {
            _username = "";
            _restClient = new RestClient();
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



    }

    
}
