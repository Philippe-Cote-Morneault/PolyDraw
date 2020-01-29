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
        }

        private void MenuItem_OnClick(object sender, RoutedEventArgs e)
        {
            SimpleWindow chatWindow = new SimpleWindow();
            RightDrawerContent.Children.Clear();
            chatWindow.MainStackPanel.Children.Add(ChatBox);
            chatWindow.Show();
        }
    }
}
