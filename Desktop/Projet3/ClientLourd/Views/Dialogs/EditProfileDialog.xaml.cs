using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using ClientLourd.Models.Bindable;
using ClientLourd.Services.RestService;
using ClientLourd.Utilities.Commands;
using ClientLourd.ViewModels;

namespace ClientLourd.Views.Dialogs
{
    /// <summary>
    /// Interaction logic for EditProfileDialog.xaml
    /// </summary>
    public partial class EditProfileDialog : UserControl, INotifyPropertyChanged
    {
        PrivateProfileInfo _pvInfo;

        public EditProfileDialog(PrivateProfileInfo pvInfo)
        {

            // DataContext = this;
            InitializeComponent();
            DataContext = this;
            PrivateProfileInfo = pvInfo;
        

        }

        public RestClient RestClient
        {
            get { return (((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel)?.RestClient; }
        }

        private RelayCommand<object> _editProfileCommand;

        public ICommand EditProfileCommand
        {
            get { return _editProfileCommand ?? (_editProfileCommand = new RelayCommand<object>(obj => EditProfile(obj))); }
        }

        private async Task EditProfile(object obj)
        {
            //TODO POST here
            
            //(((MainWindow)Application.Current.MainWindow)?.DataContext as MainViewModel).ContainedView = Enums.Views.Editor.ToString();
        }


        public PrivateProfileInfo PrivateProfileInfo
        {
            get { return _pvInfo; }
            set
            {
                
                    _pvInfo = value;
                    NotifyPropertyChanged();
                
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void NotifyPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }

    }
}
