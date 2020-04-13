using System;
using System.Collections.Generic;
using System.Linq;
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
using ClientLourd.ViewModels;
using MaterialDesignThemes.Wpf;

namespace ClientLourd.Views.Dialogs
{
    public partial class PublicProfileDialog : UserControl
    {
        public PublicProfileDialog(User user)
        {
            InitializeComponent();
            PublicProfileViewModel.User = user;
            (((MainWindow) Application.Current.MainWindow).MainWindowDialogHost as DialogHost).CloseOnClickAway = true;
        }

        private PublicProfileViewModel PublicProfileViewModel
        {
            get => DataContext as PublicProfileViewModel;
        }
    }
}