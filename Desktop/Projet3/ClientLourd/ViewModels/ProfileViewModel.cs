using ClientLourd.Utilities.Commands;
using ClientLourd.Utilities.Constants;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;

namespace ClientLourd.ViewModels
{
    class ProfileViewModel
    {
        private RelayCommand<object> _closeProfileCommand;

        public ICommand CloseProfileCommand
        {
            get { return _closeProfileCommand ?? (_closeProfileCommand = new RelayCommand<object>(obj => CloseProfile(obj))); }
        }

        private void CloseProfile(object obj)
        {
            (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).ContainedView = Enums.Views.Editor.ToString();
        }
    }
}
