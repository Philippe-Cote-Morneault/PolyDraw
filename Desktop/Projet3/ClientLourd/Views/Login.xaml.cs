using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.UI.WebControls;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using ClientLourd.ModelViews;

namespace ClientLourd.Views
{
    /// <summary>
    /// Interaction logic for Login.xaml
    /// </summary>
    public partial class Login : UserControl
    {
        public Login()
        {
            InitializeComponent();
            this.Loaded += Load;
        }

        public void Init()
        {
            PasswordBox.Clear();
            ((LoginViewModel)DataContext).Init();
        }

        public void Load(object sender, RoutedEventArgs e)
        {
           NameTextBox.Focus();

        }
    }
}
