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
using ClientLourd.Utilities.Window;
using ClientLourd.Views;
using MaterialDesignThemes.Wpf;
using ClientLourd.ModelViews;
namespace ClientLourd
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            ((MainViewModel)DataContext).UserLogout += OnUserLogout;
        }

        private void OnUserLogout(object source, EventArgs args)
        {
            Init();
            ChatBox.Init();
            LoginScreen.Init();
        }

        private void Init()
        {
            ((ViewModelBase)DataContext).Init();
            MenuToggleButton.IsChecked = false;
        }

        private void MenuItem_OnClick(object sender, RoutedEventArgs e)
        {
            SimpleWindow chatWindow = new SimpleWindow(ChatBox);
            RightDrawerContent.Children.Clear();
            chatWindow.MainStackPanel.Children.Add(ChatBox);

            //chatWindow.DataContext = this.DataContext;
            ChatToggleButton.IsEnabled = false;
            chatWindow.Owner = this;
            chatWindow.WindowStartupLocation = WindowStartupLocation.CenterScreen;
            chatWindow.Closed += (o, args) => { ChatToggleButton.IsEnabled = true; };
            chatWindow.Show();
        }

        private void ChatToggleButton_OnChecked(object sender, RoutedEventArgs e)
        {
            //Clear the notification when chatToggleButton is checked or unchecked
            ((ChatViewModel)ChatBox.DataContext).ClearNotificationCommand.Execute(null);
        }
    }
}
