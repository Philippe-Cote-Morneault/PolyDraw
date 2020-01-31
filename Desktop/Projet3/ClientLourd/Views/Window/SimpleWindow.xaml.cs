using System;
using System.Collections.Generic;
using System.ComponentModel;
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
using System.Windows.Shapes;
using ClientLourd.ModelViews;
using ClientLourd.Views;

namespace ClientLourd.Utilities.Window
{
    /// <summary>
    /// Interaction logic for SimpleWindow.xaml
    /// </summary>
    public partial class SimpleWindow : System.Windows.Window
    {
        ClientLourd.Views.Chat _chatBox;

        public SimpleWindow(ClientLourd.Views.Chat ChatBox)
        {
            _chatBox = ChatBox;

            InitializeComponent();
            Closing += OnWindowClosing;
        }

        public void OnWindowClosing(object sender, CancelEventArgs e)
        {
            MainStackPanel.Children.Clear();
            ((ClientLourd.MainWindow) this.Owner).RightDrawerContent.Children.Add(this._chatBox);
        }
    }
}