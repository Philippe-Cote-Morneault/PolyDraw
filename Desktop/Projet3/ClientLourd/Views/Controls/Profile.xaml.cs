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

namespace ClientLourd.Views.Controls
{
    /// <summary>
    /// Interaction logic for Profile.xaml
    /// </summary>
    public partial class Profile : UserControl
    {
        public Profile()
        {
            InitializeComponent();
        }

        public void UpdateStats()
        {
            ViewModel.GetAllStats();
        }
        
        public void AfterLogin()
        {
            ViewModel.AfterLogin();
        }

        public void AfterLogout()
        {
            ViewModel.AfterLogOut();
        }


    }
}
